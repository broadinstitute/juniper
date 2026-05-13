package bio.terra.pearl.core.service.file;

import bio.terra.pearl.core.dao.file.DownloadRecordDao;
import bio.terra.pearl.core.dao.file.ParticipantFileDao;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import bio.terra.pearl.core.model.file.DownloadRecord;
import bio.terra.pearl.core.model.file.ParticipantFile;
import bio.terra.pearl.core.model.file.ScannedParticipantFileDto;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.CrudService;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.file.backends.FileStorageBackend;
import bio.terra.pearl.core.service.file.backends.FileStorageBackendProvider;
import lombok.extern.slf4j.Slf4j;
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

    public ParticipantFileService(ParticipantFileDao dao, FileStorageBackendProvider fileStorageBackendProvider, DownloadRecordDao downloadRecordDao) {
        super(dao);

        this.fileStorageBackend = fileStorageBackendProvider.get();
        this.downloadRecordDao = downloadRecordDao;
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

    public Optional<ParticipantFile> findWithAnswers(UUID id) {
        return dao.findWithAnswers(id);
    }

    public List<ParticipantFile> attachDownloadRecords(List<ParticipantFile> participantFiles) {
        return dao.attachDownloadRecords(participantFiles);
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

    public DownloadRecord createDownloadRecord(ParticipantFile participantFile, ParticipantUser participantUser, Enrollee enrollee) {
        return downloadRecordDao.create(
                DownloadRecord.builder()
                        .participantFileId(participantFile.getId())
                        .participantUserId(participantUser.getId())
                        .enrolleeId(enrollee.getId())
                        .build());
    }
}
