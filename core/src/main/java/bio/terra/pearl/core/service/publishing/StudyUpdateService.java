package bio.terra.pearl.core.service.publishing;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.consent.StudyEnvironmentConsent;
import bio.terra.pearl.core.model.notification.NotificationConfig;
import bio.terra.pearl.core.model.publishing.*;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.CrudService;
import bio.terra.pearl.core.service.VersionedEntityService;
import bio.terra.pearl.core.service.consent.ConsentFormService;
import bio.terra.pearl.core.service.notification.email.EmailTemplateService;
import bio.terra.pearl.core.service.notification.NotificationConfigService;
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
public class StudyUpdateService {
    private StudyEnvironmentConfigService studyEnvironmentConfigService;
    private StudyEnvironmentService studyEnvironmentService;
    private SurveyService surveyService;
    private ConsentFormService consentFormService;
    private StudyEnvironmentConsentService studyEnvironmentConsentService;
    private StudyEnvironmentSurveyService studyEnvironmentSurveyService;
    private NotificationConfigService notificationConfigService;
    private EmailTemplateService emailTemplateService;

    public StudyUpdateService(StudyEnvironmentConfigService studyEnvironmentConfigService,
                              StudyEnvironmentService studyEnvironmentService, SurveyService surveyService,
                              ConsentFormService consentFormService, StudyEnvironmentConsentService studyEnvironmentConsentService,
                              StudyEnvironmentSurveyService studyEnvironmentSurveyService,
                              NotificationConfigService notificationConfigService,
                              EmailTemplateService emailTemplateService) {
        this.studyEnvironmentConfigService = studyEnvironmentConfigService;
        this.studyEnvironmentService = studyEnvironmentService;
        this.surveyService = surveyService;
        this.consentFormService = consentFormService;
        this.studyEnvironmentConsentService = studyEnvironmentConsentService;
        this.studyEnvironmentSurveyService = studyEnvironmentSurveyService;
        this.notificationConfigService = notificationConfigService;
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

    protected StudyEnvironment applyChangesToPreEnrollSurvey(StudyEnvironment destEnv, VersionedEntityChange change) throws Exception {
        if (!change.isChanged()) {
            return destEnv;
        }
        UUID newSurveyId = null;
        if (change.newStableId() != null) {
            newSurveyId = surveyService.findByStableId(change.newStableId(), change.newVersion()).get().getId();
        }
        destEnv.setPreEnrollSurveyId(newSurveyId);
        return studyEnvironmentService.update(destEnv);
    }

    private List<StudyEnvironmentConsent> applyChangesToConsents(StudyEnvironment destEnv,
                                                                 ListChange<StudyEnvironmentConsent, VersionedConfigChange> listChange) throws Exception {
        for(StudyEnvironmentConsent config : listChange.addedItems()) {
            config.setStudyEnvironmentId(destEnv.getId());
            studyEnvironmentConsentService.create(config.cleanForCopying());
            destEnv.getConfiguredConsents().add(config);
        }
        for(StudyEnvironmentConsent config : listChange.removedItems()) {
            studyEnvironmentConsentService.delete(config.getId(), CascadeProperty.EMPTY_SET);
            destEnv.getConfiguredConsents().remove(config);
        }
        for(VersionedConfigChange change : listChange.changedItems()) {
            applyChangesToVersionedConfig(change, studyEnvironmentConsentService, consentFormService);
        }
        return destEnv.getConfiguredConsents();
    }

    private List<StudyEnvironmentSurvey> applyChangesToSurveys(StudyEnvironment destEnv,
                                                                 ListChange<StudyEnvironmentSurvey, VersionedConfigChange> listChange) throws Exception {
        for(StudyEnvironmentSurvey config : listChange.addedItems()) {
            config.setStudyEnvironmentId(destEnv.getId());
            studyEnvironmentSurveyService.create(config.cleanForCopying());
            destEnv.getConfiguredSurveys().add(config);
        }
        for(StudyEnvironmentSurvey config : listChange.removedItems()) {
            studyEnvironmentSurveyService.delete(config.getId(), CascadeProperty.EMPTY_SET);
            destEnv.getConfiguredSurveys().remove(config);
        }
        for(VersionedConfigChange change : listChange.changedItems()) {
            applyChangesToVersionedConfig(change, studyEnvironmentSurveyService, surveyService);
        }
        return destEnv.getConfiguredSurveys();
    }

    protected void applyChangesToNotificationConfigs(StudyEnvironment destEnv, ListChange<NotificationConfig,
            VersionedConfigChange> listChange, UUID destPortalEnvId) throws Exception {
        for(NotificationConfig config : listChange.addedItems()) {
            config.setStudyEnvironmentId(destEnv.getId());
            config.setPortalEnvironmentId(destPortalEnvId);
            notificationConfigService.create(config.cleanForCopying());
            destEnv.getNotificationConfigs().add(config);
        }
        for(NotificationConfig config : listChange.removedItems()) {
            // don't delete notification configs since they may be referenced by already-sent emails
            config.setActive(false);
            notificationConfigService.update(config);
            destEnv.getNotificationConfigs().remove(config);
        }
        for(VersionedConfigChange change : listChange.changedItems()) {
            applyChangesToVersionedConfig(change, notificationConfigService, emailTemplateService);
        }
    }

    protected static <T extends BaseEntity & VersionedEntityConfig, E extends BaseEntity>
        T applyChangesToVersionedConfig(VersionedConfigChange versionedConfigChange,
                                                 CrudService<T, ?> configService,
                                                 VersionedEntityService<E> documentService) throws Exception {
        T destConfig = configService.find(versionedConfigChange.destId()).get();
        for (ConfigChange change : versionedConfigChange.configChanges()) {
            PropertyUtils.setProperty(destConfig, change.propertyName(), change.newValue());
        }
        if (versionedConfigChange.documentChange().isChanged()) {
            VersionedEntityChange docChange = versionedConfigChange.documentChange();
            UUID newDocumentId = null;
            if (docChange.newStableId() != null) {
                newDocumentId = documentService.findByStableId(docChange.newStableId(), docChange.newVersion()).get().getId();
            }
            destConfig.updateVersionedEntityId(newDocumentId);
        }
        return configService.update(destConfig);
    }

}
