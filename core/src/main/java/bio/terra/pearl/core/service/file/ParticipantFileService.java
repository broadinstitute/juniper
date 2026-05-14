package bio.terra.pearl.core.service.file;

import bio.terra.pearl.core.dao.file.DownloadRecordDao;
import bio.terra.pearl.core.dao.file.ParticipantFileDao;
import bio.terra.pearl.core.dao.survey.AnswerDao;
import bio.terra.pearl.core.model.file.FileAnswer;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import bio.terra.pearl.core.model.file.DownloadRecord;
import bio.terra.pearl.core.model.file.ParticipantFile;
import bio.terra.pearl.core.model.file.ScannedParticipantFileDto;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.survey.Answer;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.CrudService;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.file.backends.FileStorageBackend;
import bio.terra.pearl.core.service.file.backends.FileStorageBackendProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class ParticipantFileService extends CrudService<ParticipantFile, ParticipantFileDao> {
    private final FileStorageBackend fileStorageBackend;
    private final DownloadRecordDao downloadRecordDao;
    private final AnswerDao answerDao;
    private final ObjectMapper objectMapper;

    public ParticipantFileService(ParticipantFileDao dao, FileStorageBackendProvider fileStorageBackendProvider,
                                  DownloadRecordDao downloadRecordDao, AnswerDao answerDao, ObjectMapper objectMapper) {
        super(dao);
        this.fileStorageBackend = fileStorageBackendProvider.get();
        this.downloadRecordDao = downloadRecordDao;
        this.answerDao = answerDao;
        this.objectMapper = objectMapper;
    }

    public enum AllowedCascades implements CascadeProperty {
        ANSWER
    }

    public ParticipantFile uploadFileAndCreate(ParticipantFile participantFile, InputStream file) {
        UUID fileId = fileStorageBackend.uploadFile(file);
        participantFile.setExternalFileId(fileId);
        try {
            return dao.create(participantFile);
        } catch (UnableToExecuteStatementException e) {
            if (e.getMessage() != null && e.getMessage().contains("uq_participant_file_enrollee_file_name")) {
                throw new IllegalArgumentException(
                        "A file with the name '%s' already exists for this enrollee".formatted(participantFile.getFileName()));
            }
            throw e;
        }
    }

    public List<ParticipantFile> findBySurveyResponseId(UUID surveyResponseId) {
        return dao.findBySurveyResponseId(surveyResponseId);
    }

    public List<ParticipantFile> findByEnrolleeId(UUID enrolleeId) {
        return dao.findByEnrolleeIdWithAnswers(enrolleeId);
    }

    public void deleteByEnrolleeId(UUID enrolleeId) {
        dao.deleteByEnrolleeId(enrolleeId);
    }

    public Optional<ParticipantFile> findByEnrolleeIdAndFileName(UUID enrolleeId, String fileName) {
        return dao.findByEnrolleeIdAndFileName(enrolleeId, fileName);
    }

    public Optional<ParticipantFile> findByEnrolleeAndId(UUID enrolleeId, UUID fileId) {
        return dao.findByEnrolleeIdAndId(enrolleeId, fileId);
    }

    public Optional<ParticipantFile> findWithAnswers(UUID id) {
        return dao.findWithAnswers(id);
    }

    public List<ParticipantFile> attachDownloadRecords(List<ParticipantFile> participantFiles) {
        return dao.attachDownloadRecords(participantFiles);
    }

    @Override
    public void delete(UUID id, Set<CascadeProperty> cascade) {
        ParticipantFile participantFile = findWithAnswers(id).orElseThrow(() -> new NotFoundException("File not found"));
        if (!participantFile.getAssociatedAnswers().isEmpty()) {
            if (cascade.contains(AllowedCascades.ANSWER)) {
                for (Answer answer : participantFile.getAssociatedAnswers()) {
                    removeFileFromAnswer(answer, id);
                }
            } else {
                throw new IllegalArgumentException("This file is being used in a survey response and cannot be deleted. Please remove the file from the survey response first.");
            }
        }
        super.delete(id, cascade);
        fileStorageBackend.deleteFile(participantFile.getExternalFileId());
    }

    private void removeFileFromAnswer(Answer answer, UUID participantFileId) {
        try {
            FileAnswer[] fileAnswers = objectMapper.readValue(answer.getObjectValue(), FileAnswer[].class);
            List<FileAnswer> remaining = Arrays.stream(fileAnswers)
                    .filter(fa -> !participantFileId.equals(fa.getParticipantFileId()))
                    .toList();
            if (remaining.isEmpty()) {
                answerDao.delete(answer.getId());
            } else {
                answer.setObjectValue(objectMapper.writeValueAsString(remaining));
                answerDao.update(answer);
            }
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to parse FILE_UPLOAD answer for answer " + answer.getId(), e);
        }
    }

    public ScannedParticipantFileDto attachVirusScanResult(ParticipantFile participantFile) {
        VirusScanResult scanResult = fileStorageBackend.scanResult(participantFile.getExternalFileId());

        return new ScannedParticipantFileDto(participantFile, scanResult);
    }

    public VirusScanResult getVirusScanResult(UUID fileId) {
        return fileStorageBackend.scanResult(fileId);
    }

    public InputStream downloadFile(ParticipantFile participantFile, ParticipantUser participantUser, Enrollee enrollee) {
        VirusScanResult virusScanResult =
                getVirusScanResult(participantFile.getExternalFileId());

        if (virusScanResult == VirusScanResult.QUARANTINED) {
            throw new IllegalArgumentException("Virus detected in file");
        }

        InputStream fileStream = fileStorageBackend.downloadFile(participantFile.getExternalFileId());
        createDownloadRecord(participantFile, participantUser, enrollee);
        return fileStream;
    }

    public DownloadRecord createDownloadRecord(ParticipantFile participantFile, ParticipantUser participantUser, Enrollee enrollee) {
        return downloadRecordDao.create(
                DownloadRecord.builder()
                        .participantFileId(participantFile.getId())
                        .participantUserId(participantUser.getId())
                        .enrolleeId(enrollee.getId())
                        .build());
    }
}
