package bio.terra.pearl.core.service.study;

import bio.terra.pearl.core.dao.study.AttachSurvey;
import bio.terra.pearl.core.dao.study.StudyEnvironmentSurveyDao;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.publishing.ListChange;
import bio.terra.pearl.core.model.publishing.StudyEnvironmentChange;
import bio.terra.pearl.core.model.publishing.VersionedConfigChange;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.survey.SurveyType;
import bio.terra.pearl.core.service.CrudService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import bio.terra.pearl.core.service.publishing.PortalEnvPublishable;
import bio.terra.pearl.core.service.publishing.PublishingUtils;
import bio.terra.pearl.core.service.publishing.StudyEnvPublishable;
import bio.terra.pearl.core.service.survey.SurveyService;
import bio.terra.pearl.core.service.workflow.EventService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudyEnvironmentSurveyService extends CrudService<StudyEnvironmentSurvey, StudyEnvironmentSurveyDao> implements StudyEnvPublishable{
    private final SurveyService surveyService;
    private final EventService eventService;
    public StudyEnvironmentSurveyService(StudyEnvironmentSurveyDao dao, SurveyService surveyService, EventService eventService) {
        super(dao);
        this.surveyService = surveyService;
        this.eventService = eventService;
    }

    public List<StudyEnvironmentSurvey> findAllByStudyEnvId(UUID studyEnvId, Boolean active) {
        return dao.findAll(List.of(studyEnvId), null, active);
    }


    public List<StudyEnvironmentSurvey> findAllByStudyEnvIdWithSurvey(UUID studyEnvId) {
        return dao.findAllWithSurvey(studyEnvId, true);
    }

    public List<StudyEnvironmentSurvey> findAllByStudyEnvIdWithSurvey(UUID studyEnvId, Boolean active) {
        return dao.findAllWithSurvey(studyEnvId, active);
    }

    public List<StudyEnvironmentSurvey> findAllByStudyEnvIdWithSurveyNoContent(UUID studyEnvId, Boolean active) {
        return dao.findAllWithSurveyNoContent(List.of(studyEnvId), null, active);
    }

    public Optional<StudyEnvironmentSurvey> findActiveBySurvey(UUID studyEnvId, UUID surveyId) {
        return dao.findActiveBySurvey(studyEnvId, surveyId);
    }

    @Transactional
    public StudyEnvironmentSurvey deactivate(UUID id) {
        StudyEnvironmentSurvey ses = dao.find(id).get();
        ses.setActive(false);
        return dao.update(ses);
    }

    @Transactional
    @Override
    public StudyEnvironmentSurvey create(StudyEnvironmentSurvey studyEnvSurvey) {
        validateSurveyNotAlreadyActive(studyEnvSurvey);
        return super.create(studyEnvSurvey);
    }

    @Transactional
    @Override
    public StudyEnvironmentSurvey update(StudyEnvironmentSurvey studyEnvSurvey) {
        validateSurveyNotAlreadyActive(studyEnvSurvey);
        return super.update(studyEnvSurvey);
    }

    public void validateSurveyNotAlreadyActive(StudyEnvironmentSurvey studyEnvSurvey) {
        if (studyEnvSurvey.isActive() &&
            dao.isSurveyActiveInEnv(studyEnvSurvey.getSurveyId(), studyEnvSurvey.getStudyEnvironmentId(), studyEnvSurvey.getId())
        ) {
            throw new IllegalArgumentException("Cannot save -- another version of the survey is already active, likely due to multiple saves overlapping.  Confirm no one else is working on the survey and then retry.");
        }
    }

    public List<StudyEnvironmentSurvey> findActiveBySurvey(UUID studyEnvId, String stableId) {
        return dao.findActiveBySurvey(studyEnvId, stableId);
    }

    public List<StudyEnvironmentSurvey> findBySurveyId(UUID surveyId) {
        return dao.findBySurveyId(surveyId);
    }

    public List<StudyEnvironmentSurvey> findAllWithSurveyNoContent(List<UUID> studyEnvIds, String stableId, Boolean active) {
        return dao.findAllWithSurveyNoContent(studyEnvIds, stableId, active);
    }

    public List<StudyEnvironmentSurvey> findAllByType(List<UUID> studyEnvIds, SurveyType type, Boolean active, AttachSurvey attachSurvey) {
        return dao.findAllByType(studyEnvIds, type, active, attachSurvey);
    }

    public void deleteBySurveyId(UUID surveyId) {
        dao.deleteBySurveyId(surveyId);
    }

    @Override
    public void loadForPublishing(StudyEnvironment studyEnv) {
        studyEnv.setConfiguredSurveys(dao.findAllWithSurvey(studyEnv.getId(), true));
    }

    @Override
    public void updateDiff(StudyEnvironmentChange change, StudyEnvironment sourceEnv, StudyEnvironment destEnv) {
        ListChange<StudyEnvironmentSurvey, VersionedConfigChange<Survey>> surveyChanges = PortalEnvPublishable.diffConfigLists(
                sourceEnv.getConfiguredSurveys(),
                destEnv.getConfiguredSurveys(),
                getPublishIgnoreProps());
        change.setSurveyChanges(surveyChanges);
    }

    @Override
    @Transactional
    public void applyDiff(StudyEnvironmentChange change, StudyEnvironment destEnv, PortalEnvironment destPortalEnv) {
        ListChange<StudyEnvironmentSurvey, VersionedConfigChange<Survey>> listChange = change.getSurveyChanges();
        for (StudyEnvironmentSurvey config : listChange.addedItems()) {
            config.setStudyEnvironmentId(destEnv.getId());
            StudyEnvironmentSurvey newConfig = create(config.cleanForCopying());
            newConfig.setSurvey(config.getSurvey());
            destEnv.getConfiguredSurveys().add(newConfig);
            PublishingUtils.assignPublishedVersionIfNeeded(destEnv.getEnvironmentName(), newConfig, surveyService);
            eventService.publishSurveyPublishedEvent(destPortalEnv.getId(), destEnv.getId(), newConfig.getSurvey());
        }
        for (StudyEnvironmentSurvey config : listChange.removedItems()) {
            deactivate(config.getId());
            destEnv.getConfiguredSurveys().remove(config);
        }
        for (VersionedConfigChange<Survey> configChange : listChange.changedItems()) {
            PublishingUtils.applyChangesToVersionedConfig(configChange, this, surveyService, destEnv.getEnvironmentName(), destPortalEnv.getPortalId());
            if (configChange.documentChange().isChanged()) {
                // if this is a change of version (as opposed to a reordering), then publish an event
                Survey survey = surveyService.findByStableId(
                        configChange.documentChange().newStableId(), configChange.documentChange().newVersion(), destPortalEnv.getPortalId()
                ).orElseThrow();
                eventService.publishSurveyPublishedEvent(destPortalEnv.getId(), destEnv.getId(), survey);
            }
        }
    }

    @Override
    public List<String> getAdditionalPublishIgnoreProps() {
        return List.of( "surveyId", "survey", "versionedEntity");
    }
}
