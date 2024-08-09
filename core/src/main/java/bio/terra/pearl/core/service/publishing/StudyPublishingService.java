package bio.terra.pearl.core.service.publishing;

import bio.terra.pearl.core.model.kit.KitType;
import bio.terra.pearl.core.model.kit.StudyEnvironmentKitType;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.publishing.ConfigChange;
import bio.terra.pearl.core.model.publishing.ListChange;
import bio.terra.pearl.core.model.publishing.StudyEnvironmentChange;
import bio.terra.pearl.core.model.publishing.VersionedConfigChange;
import bio.terra.pearl.core.model.publishing.VersionedEntityChange;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.exception.internal.InternalServerException;
import bio.terra.pearl.core.service.kit.StudyEnvironmentKitTypeService;
import bio.terra.pearl.core.service.notification.TriggerService;
import bio.terra.pearl.core.service.notification.email.EmailTemplateService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.study.StudyEnvironmentConfigService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.study.StudyEnvironmentSurveyService;
import bio.terra.pearl.core.service.survey.SurveyService;
import bio.terra.pearl.core.service.workflow.EventService;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class StudyPublishingService {
    private final StudyEnvironmentKitTypeService studyEnvironmentKitTypeService;
    private StudyEnvironmentConfigService studyEnvironmentConfigService;
    private StudyEnvironmentService studyEnvironmentService;
    private SurveyService surveyService;
    private StudyEnvironmentSurveyService studyEnvironmentSurveyService;
    private TriggerService triggerService;
    private EmailTemplateService emailTemplateService;
    private EventService eventService;
    private PortalEnvironmentService portalEnvironmentService;

    public StudyPublishingService(StudyEnvironmentConfigService studyEnvironmentConfigService,
                                  StudyEnvironmentService studyEnvironmentService, SurveyService surveyService,
                                  StudyEnvironmentSurveyService studyEnvironmentSurveyService,
                                  TriggerService triggerService,
                                  EmailTemplateService emailTemplateService, EventService eventService, PortalEnvironmentService portalEnvironmentService, StudyEnvironmentKitTypeService studyEnvironmentKitTypeService) {
        this.studyEnvironmentConfigService = studyEnvironmentConfigService;
        this.studyEnvironmentService = studyEnvironmentService;
        this.surveyService = surveyService;
        this.studyEnvironmentSurveyService = studyEnvironmentSurveyService;
        this.triggerService = triggerService;
        this.emailTemplateService = emailTemplateService;
        this.eventService = eventService;
        this.portalEnvironmentService = portalEnvironmentService;
        this.studyEnvironmentKitTypeService = studyEnvironmentKitTypeService;
    }

    /**
     * the study environment must be fully hydrated by a call to loadStudyEnvForProcessing prior to passing in
     */
    @Transactional
    public StudyEnvironment applyChanges(StudyEnvironment destEnv, StudyEnvironmentChange envChange,
                                         PortalEnvironment destPortalEnv) {
        applyChangesToStudyEnvConfig(destEnv, envChange.getConfigChanges());
        applyChangesToPreEnrollSurvey(destEnv, envChange.getPreEnrollSurveyChanges(), destPortalEnv.getPortalId());
        applyChangesToSurveys(destEnv, envChange.getSurveyChanges(), destPortalEnv.getId(), destPortalEnv.getPortalId());
        applyChangesToKitTypes(destEnv, envChange.getKitTypeChanges());
        triggerService.applyDiff(envChange, destEnv, destPortalEnv);
        return destEnv;
    }

    protected StudyEnvironmentConfig applyChangesToStudyEnvConfig(StudyEnvironment destEnv, List<ConfigChange> configChanges) {
        if (configChanges.isEmpty()) {
            return destEnv.getStudyEnvironmentConfig();
        }
        try {
            for (ConfigChange change : configChanges) {
                PropertyUtils.setProperty(destEnv.getStudyEnvironmentConfig(), change.propertyName(), change.newValue());
            }
        } catch (Exception e) {
            throw new InternalServerException("Error setting property during publish", e);
        }
        return studyEnvironmentConfigService.update(destEnv.getStudyEnvironmentConfig());
    }


    protected StudyEnvironment applyChangesToPreEnrollSurvey(StudyEnvironment destEnv, VersionedEntityChange<Survey> change, UUID destPortalId) {
        if (!change.isChanged()) {
            return destEnv;
        }
        UUID newSurveyId = null;
        if (change.newStableId() != null) {
            newSurveyId = surveyService.findByStableId(change.newStableId(), change.newVersion(), destPortalId).get().getId();
        }
        destEnv.setPreEnrollSurveyId(newSurveyId);
        PublishingUtils.assignPublishedVersionIfNeeded(destEnv.getEnvironmentName(), destPortalId, change, surveyService);
        return studyEnvironmentService.update(destEnv);
    }

    private List<StudyEnvironmentSurvey> applyChangesToSurveys(StudyEnvironment destEnv,
                                                               ListChange<StudyEnvironmentSurvey, VersionedConfigChange<Survey>> listChange,
                                                               UUID destPortalEnvId,
                                                               UUID destPortalId) {
        for (StudyEnvironmentSurvey config : listChange.addedItems()) {
            config.setStudyEnvironmentId(destEnv.getId());
            StudyEnvironmentSurvey newConfig = studyEnvironmentSurveyService.create(config.cleanForCopying());
            newConfig.setSurvey(config.getSurvey());
            destEnv.getConfiguredSurveys().add(newConfig);
            PublishingUtils.assignPublishedVersionIfNeeded(destEnv.getEnvironmentName(), newConfig, surveyService);
            eventService.publishSurveyPublishedEvent(destPortalEnvId, destEnv.getId(), newConfig.getSurvey());
        }
        for (StudyEnvironmentSurvey config : listChange.removedItems()) {
            studyEnvironmentSurveyService.deactivate(config.getId());
            destEnv.getConfiguredSurveys().remove(config);
        }
        for (VersionedConfigChange change : listChange.changedItems()) {
            PublishingUtils.applyChangesToVersionedConfig(change, studyEnvironmentSurveyService, surveyService, destEnv.getEnvironmentName(), destPortalId);
            if (change.documentChange().isChanged()) {
                // if this is a change of version (as opposed to a reordering), then publish an event
                Survey survey = surveyService.findByStableId(
                        change.documentChange().newStableId(), change.documentChange().newVersion(), destPortalId
                ).orElseThrow();

                eventService.publishSurveyPublishedEvent(destPortalEnvId, destEnv.getId(), survey);
            }
        }
        return destEnv.getConfiguredSurveys();
    }

    protected void applyChangesToKitTypes(StudyEnvironment destEnv, ListChange<KitType, KitType> kitTypeChanges) {
        for (KitType kitType : kitTypeChanges.addedItems()) {
            StudyEnvironmentKitType studyEnvKitType = new StudyEnvironmentKitType();
            studyEnvKitType.setKitTypeId(kitType.getId());
            studyEnvKitType.setStudyEnvironmentId(destEnv.getId());

            studyEnvironmentKitTypeService.create(studyEnvKitType);
        }
        for (KitType kitType : kitTypeChanges.removedItems()) {
            studyEnvironmentKitTypeService.deleteByKitTypeIdAndStudyEnvironmentId(kitType.getId(), destEnv.getId());
        }
    }

}
