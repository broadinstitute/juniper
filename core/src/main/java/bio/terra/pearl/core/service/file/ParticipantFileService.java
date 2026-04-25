package bio.terra.pearl.core.service.file;

import bio.terra.pearl.core.dao.file.DownloadRecordDao;
import bio.terra.pearl.core.dao.file.ParticipantFileDao;
import bio.terra.pearl.core.dao.survey.SurveyResponseDao;
import bio.terra.pearl.core.model.file.DownloadRecord;
import bio.terra.pearl.core.model.file.ParticipantFile;
import bio.terra.pearl.core.model.file.ScannedParticipantFileDto;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.survey.Answer;
import bio.terra.pearl.core.model.survey.AnswerFormat;
import bio.terra.pearl.core.model.survey.AnswerType;
import bio.terra.pearl.core.model.survey.SurveyResponse;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.CrudService;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.file.backends.FileStorageBackend;
import bio.terra.pearl.core.service.file.backends.FileStorageBackendProvider;
import bio.terra.pearl.core.service.survey.AnswerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class ParticipantFileService extends CrudService<ParticipantFile, ParticipantFileDao> {
    private final FileStorageBackend fileStorageBackend;
    private final DownloadRecordDao downloadRecordDao;
    private final AnswerService answerService;
    private final SurveyResponseDao surveyResponseDao;

    public ParticipantFileService(ParticipantFileDao dao, FileStorageBackendProvider fileStorageBackendProvider, DownloadRecordDao downloadRecordDao, @Lazy AnswerService answerService, @Lazy SurveyResponseDao surveyResponseDao) {
        super(dao);

        this.fileStorageBackend = fileStorageBackendProvider.get();
        this.downloadRecordDao = downloadRecordDao;
        this.answerService = answerService;
        this.surveyResponseDao = surveyResponseDao;
    }

    public ParticipantFile uploadFileAndCreate(ParticipantFile participantFile, InputStream file) {
        UUID fileId = fileStorageBackend.uploadFile(file);
        participantFile.setExternalFileId(fileId);
        return dao.create(participantFile);
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

    public Optional<ParticipantFile> findWithAnswers(UUID id) {
        return dao.findWithAnswers(id);
    }

    @Override
    public void delete(UUID id, Set<CascadeProperty> cascade) {
        ParticipantFile participantFile = findWithAnswers(id).orElseThrow(() -> new NotFoundException("File not found"));
        if(!participantFile.getAssociatedAnswers().isEmpty()) {
            throw new IllegalArgumentException("This file is being used in a survey response and cannot be deleted. Please remove the file from the survey response first.");
        }
        super.delete(id, cascade);
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

    /**
     * creates the download record and also an answer
     * this is public so that it can be easily accessed by EnrolleePopulator.
     * it should not be accessed independently otherwise
     */
    public DownloadRecord createDownloadRecord(ParticipantFile participantFile, ParticipantUser participantUser, Enrollee enrollee) {
        DownloadRecord record = downloadRecordDao.create(
                DownloadRecord.builder()
                        .participantFileId(participantFile.getId())
                        .participantUserId(participantUser.getId())
                        .enrolleeId(enrollee.getId())
                        .build());

        ParticipantFile fileWithAnswers = findWithAnswers(participantFile.getId()).orElse(participantFile);
        fileWithAnswers.getAssociatedAnswers().stream().findFirst().ifPresent(associatedAnswer -> {
            SurveyResponse existingResponse = surveyResponseDao.find(associatedAnswer.getSurveyResponseId()).orElseThrow();
            SurveyResponse newResponse = surveyResponseDao.create(
                    SurveyResponse.builder()
                            .enrolleeId(enrollee.getId())
                            .surveyId(existingResponse.getSurveyId())
                            .creatingParticipantUserId(participantUser.getId())
                            .build());
            answerService.create(
                    Answer.builder()
                            .surveyResponseId(newResponse.getId())
                            .enrolleeId(enrollee.getId())
                            // file upload answers are postfixed by [] to enable multi-file answers.  We just want the raw stableId
                            .questionStableId(associatedAnswer.getQuestionStableId().replaceAll("\\[.*]$", ""))
                            .surveyStableId(associatedAnswer.getSurveyStableId())
                            .surveyVersion(associatedAnswer.getSurveyVersion())
                            .answerType(AnswerType.STRING)
                            .format(AnswerFormat.VIEW)
                            .stringValue("yes")
                            .creatingParticipantUserId(participantUser.getId())
                            .build());
        });
        return record;
    }
}
