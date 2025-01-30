package bio.terra.pearl.core.service.file;

import bio.terra.pearl.core.dao.file.ParticipantFileDao;
import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.file.ParticipantFile;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.survey.SurveyResponse;
import bio.terra.pearl.core.service.ImmutableEntityService;
import bio.terra.pearl.core.service.file.backends.FileStorageBackend;
import bio.terra.pearl.core.service.file.backends.FileStorageBackendProvider;
import bio.terra.pearl.core.service.survey.SurveyResponseService;
import bio.terra.pearl.core.service.survey.SurveyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class ParticipantFileService extends ImmutableEntityService<ParticipantFile, ParticipantFileDao> {
    private final FileStorageBackend fileStorageBackend;
    private final SurveyResponseService surveyResponseService;

    public ParticipantFileService(ParticipantFileDao dao, FileStorageBackendProvider fileStorageBackendProvider, SurveyService surveyService, SurveyResponseService surveyResponseService) {
        super(dao);

        this.fileStorageBackend = fileStorageBackendProvider.get();
        this.surveyResponseService = surveyResponseService;
    }

    public ParticipantFile uploadFileAndCreate(ParticipantFile participantFile, InputStream file) {
        UUID fileId = fileStorageBackend.uploadFile(file);
        participantFile.setExternalFileId(fileId);
        return dao.create(participantFile);
    }

    public List<ParticipantFile> findBySurveyResponseId(UUID surveyResponseId) {
        return dao.findBySurveyResponseId(surveyResponseId);
    }

    public List<ParticipantFile> findByEnrollee(Enrollee enrollee) {
        List<ParticipantFile> filesForEnrollee = dao.findByEnrolleeId(enrollee.getId());
        List<SurveyResponse> surveyResponses = surveyResponseService.findByEnrolleeIdWithFiles(enrollee.getId());

        for (ParticipantFile file : filesForEnrollee) {
            for (SurveyResponse surveyResponse : surveyResponses) {
                if (surveyResponse.getParticipantFiles().stream().map(BaseEntity::getId).toList().contains(file.getId())) {
                    file.getSurveyResponseIds().add(surveyResponse.getId());
                }
            }
        }

        return filesForEnrollee;
    }

    public void deleteByEnrolleeId(UUID enrolleeId) {
        dao.deleteByEnrolleeId(enrolleeId);
    }

    public Optional<ParticipantFile> findByEnrolleeIdAndFileName(UUID enrolleeId, String fileName) {
        return dao.findByEnrolleeIdAndFileName(enrolleeId, fileName);
    }
}
