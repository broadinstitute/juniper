package bio.terra.pearl.core.service.publishing;

import bio.terra.pearl.core.model.consent.ConsentForm;
import bio.terra.pearl.core.model.consent.StudyEnvironmentConsent;
import bio.terra.pearl.core.model.notification.EmailTemplate;
import bio.terra.pearl.core.model.notification.TriggeredAction;
import bio.terra.pearl.core.model.publishing.*;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.consent.ConsentFormService;
import bio.terra.pearl.core.service.notification.email.EmailTemplateService;
import bio.terra.pearl.core.service.notification.TriggeredActionService;
import bio.terra.pearl.core.service.study.StudyEnvironmentConfigService;
import bio.terra.pearl.core.service.study.StudyEnvironmentConsentService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.study.StudyEnvironmentSurveyService;
import bio.terra.pearl.core.service.survey.SurveyService;
import java.util.List;
import java.util.UUID;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudyPublishingService {
    private StudyEnvironmentConfigService studyEnvironmentConfigService;
    private StudyEnvironmentService studyEnvironmentService;
    private SurveyService surveyService;
    private ConsentFormService consentFormService;
    private StudyEnvironmentConsentService studyEnvironmentConsentService;
    private StudyEnvironmentSurveyService studyEnvironmentSurveyService;
    private TriggeredActionService triggeredActionService;
    private EmailTemplateService emailTemplateService;

    public StudyPublishingService(StudyEnvironmentConfigService studyEnvironmentConfigService,
                                  StudyEnvironmentService studyEnvironmentService, SurveyService surveyService,
                                  ConsentFormService consentFormService, StudyEnvironmentConsentService studyEnvironmentConsentService,
                                  StudyEnvironmentSurveyService studyEnvironmentSurveyService,
                                  TriggeredActionService triggeredActionService,
                                  EmailTemplateService emailTemplateService) {
        this.studyEnvironmentConfigService = studyEnvironmentConfigService;
        this.studyEnvironmentService = studyEnvironmentService;
        this.surveyService = surveyService;
        this.consentFormService = consentFormService;
        this.studyEnvironmentConsentService = studyEnvironmentConsentService;
        this.studyEnvironmentSurveyService = studyEnvironmentSurveyService;
        this.triggeredActionService = triggeredActionService;
        this.emailTemplateService = emailTemplateService;
    }

    /** the study environment must be fully hydrated by a call to loadStudyEnvForProcessing prior to passing in */
    @Transactional
    public StudyEnvironment applyChanges(StudyEnvironment destEnv, StudyEnvironmentChange envChange,
                                         UUID destPortalEnvId) throws Exception {
        applyChangesToStudyEnvConfig(destEnv, envChange.configChanges());
        applyChangesToPreEnrollSurvey(destEnv, envChange.preEnrollSurveyChanges());
        applyChangesToConsents(destEnv, envChange.consentChanges());
        applyChangesToSurveys(destEnv, envChange.surveyChanges());
        applyChangesToNotificationConfigs(destEnv, envChange.notificationConfigChanges(), destPortalEnvId);
        return destEnv;
    }

    protected StudyEnvironmentConfig applyChangesToStudyEnvConfig(StudyEnvironment destEnv, List<ConfigChange> configChanges) throws Exception {
        if (configChanges.isEmpty()) {
            return destEnv.getStudyEnvironmentConfig();
        }
        for (ConfigChange change : configChanges) {
            PropertyUtils.setProperty(destEnv.getStudyEnvironmentConfig(), change.propertyName(), change.newValue());
        }
        return studyEnvironmentConfigService.update(destEnv.getStudyEnvironmentConfig());
    }

    protected StudyEnvironment applyChangesToPreEnrollSurvey(StudyEnvironment destEnv, VersionedEntityChange<Survey> change) throws Exception {
        if (!change.isChanged()) {
            return destEnv;
        }
        UUID newSurveyId = null;
        if (change.newStableId() != null) {
            newSurveyId = surveyService.findByStableId(change.newStableId(), change.newVersion()).get().getId();
        }
        destEnv.setPreEnrollSurveyId(newSurveyId);
        PublishingUtils.assignPublishedVersionIfNeeded(destEnv.getEnvironmentName(), change, surveyService);
        return studyEnvironmentService.update(destEnv);
    }

    private List<StudyEnvironmentConsent> applyChangesToConsents(StudyEnvironment destEnv,
                                                                 ListChange<StudyEnvironmentConsent, VersionedConfigChange<ConsentForm>> listChange) throws Exception {
        for(StudyEnvironmentConsent config : listChange.addedItems()) {
            config.setStudyEnvironmentId(destEnv.getId());
            studyEnvironmentConsentService.create(config.cleanForCopying());
            destEnv.getConfiguredConsents().add(config);
            PublishingUtils.assignPublishedVersionIfNeeded(destEnv.getEnvironmentName(), config, consentFormService);
        }
        for(StudyEnvironmentConsent config : listChange.removedItems()) {
            studyEnvironmentConsentService.delete(config.getId(), CascadeProperty.EMPTY_SET);
            destEnv.getConfiguredConsents().remove(config);
        }
        for(VersionedConfigChange<ConsentForm> change : listChange.changedItems()) {
            PublishingUtils.applyChangesToVersionedConfig(change, studyEnvironmentConsentService, consentFormService, destEnv.getEnvironmentName());
        }
        return destEnv.getConfiguredConsents();
    }

    private List<StudyEnvironmentSurvey> applyChangesToSurveys(StudyEnvironment destEnv,
                                                                 ListChange<StudyEnvironmentSurvey, VersionedConfigChange<Survey>> listChange) throws Exception {
        for(StudyEnvironmentSurvey config : listChange.addedItems()) {
            config.setStudyEnvironmentId(destEnv.getId());
            studyEnvironmentSurveyService.create(config.cleanForCopying());
            destEnv.getConfiguredSurveys().add(config);
            PublishingUtils.assignPublishedVersionIfNeeded(destEnv.getEnvironmentName(), config, surveyService);
        }
        for(StudyEnvironmentSurvey config : listChange.removedItems()) {
            studyEnvironmentSurveyService.deactivate(config.getId());
            destEnv.getConfiguredSurveys().remove(config);
        }
        for(VersionedConfigChange change : listChange.changedItems()) {
            PublishingUtils.applyChangesToVersionedConfig(change, studyEnvironmentSurveyService, surveyService, destEnv.getEnvironmentName());
        }
        return destEnv.getConfiguredSurveys();
    }

    protected void applyChangesToNotificationConfigs(StudyEnvironment destEnv, ListChange<TriggeredAction,
            VersionedConfigChange<EmailTemplate>> listChange, UUID destPortalEnvId) throws Exception {
        for(TriggeredAction config : listChange.addedItems()) {
            config.setStudyEnvironmentId(destEnv.getId());
            config.setPortalEnvironmentId(destPortalEnvId);
            triggeredActionService.create(config.cleanForCopying());
            destEnv.getTriggeredActions().add(config);
            PublishingUtils.assignPublishedVersionIfNeeded(destEnv.getEnvironmentName(), config, emailTemplateService);
        }
        for(TriggeredAction config : listChange.removedItems()) {
            // don't delete notification configs since they may be referenced by already-sent emails
            config.setActive(false);
            triggeredActionService.update(config);
            destEnv.getTriggeredActions().remove(config);
        }
        for(VersionedConfigChange<EmailTemplate> change : listChange.changedItems()) {
            PublishingUtils.applyChangesToVersionedConfig(change, triggeredActionService, emailTemplateService, destEnv.getEnvironmentName());
        }
    }


}
