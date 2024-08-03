package bio.terra.pearl.core.service.publishing;

import bio.terra.pearl.core.dao.publishing.PortalEnvironmentChangeRecordDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.dashboard.AlertTrigger;
import bio.terra.pearl.core.model.dashboard.AlertType;
import bio.terra.pearl.core.model.dashboard.ParticipantDashboardAlert;
import bio.terra.pearl.core.model.notification.EmailTemplate;
import bio.terra.pearl.core.model.notification.Trigger;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.portal.PortalEnvironmentConfig;
import bio.terra.pearl.core.model.portal.PortalEnvironmentLanguage;
import bio.terra.pearl.core.model.publishing.ConfigChange;
import bio.terra.pearl.core.model.publishing.ListChange;
import bio.terra.pearl.core.model.publishing.ParticipantDashboardAlertChange;
import bio.terra.pearl.core.model.publishing.PortalEnvironmentChange;
import bio.terra.pearl.core.model.publishing.PortalEnvironmentChangeRecord;
import bio.terra.pearl.core.model.publishing.StudyEnvironmentChange;
import bio.terra.pearl.core.model.publishing.VersionedConfigChange;
import bio.terra.pearl.core.model.publishing.VersionedEntityChange;
import bio.terra.pearl.core.model.site.SiteContent;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.exception.internal.InternalServerException;
import bio.terra.pearl.core.service.notification.TriggerService;
import bio.terra.pearl.core.service.notification.email.EmailTemplateService;
import bio.terra.pearl.core.service.portal.PortalDashboardConfigService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentConfigService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentLanguageService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.site.SiteContentService;
import bio.terra.pearl.core.service.survey.SurveyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * dedicated service for applying deltas to portal environments
 */
@Service
public class PortalPublishingService {
    private final PortalDiffService portalDiffService;
    private final PortalEnvironmentService portalEnvironmentService;
    private final PortalEnvironmentConfigService portalEnvironmentConfigService;
    private final PortalEnvironmentChangeRecordDao portalEnvironmentChangeRecordDao;
    private final PortalDashboardConfigService portalDashboardConfigService;
    private final TriggerService triggerService;
    private final SurveyService surveyService;
    private final EmailTemplateService emailTemplateService;
    private final SiteContentService siteContentService;
    private final StudyPublishingService studyPublishingService;
    private final PortalEnvironmentLanguageService portalEnvironmentLanguageService;
    private final ObjectMapper objectMapper;


    public PortalPublishingService(PortalDiffService portalDiffService,
                                   PortalEnvironmentService portalEnvironmentService,
                                   PortalEnvironmentConfigService portalEnvironmentConfigService,
                                   PortalEnvironmentChangeRecordDao portalEnvironmentChangeRecordDao,
                                   PortalDashboardConfigService portalDashboardConfigService,
                                   TriggerService triggerService, SurveyService surveyService,
                                   EmailTemplateService emailTemplateService, SiteContentService siteContentService,
                                   StudyPublishingService studyPublishingService,
                                   PortalEnvironmentLanguageService portalEnvironmentLanguageService, ObjectMapper objectMapper) {
        this.portalDiffService = portalDiffService;
        this.portalEnvironmentService = portalEnvironmentService;
        this.portalEnvironmentConfigService = portalEnvironmentConfigService;
        this.portalEnvironmentChangeRecordDao = portalEnvironmentChangeRecordDao;
        this.portalDashboardConfigService = portalDashboardConfigService;
        this.triggerService = triggerService;
        this.surveyService = surveyService;
        this.emailTemplateService = emailTemplateService;
        this.siteContentService = siteContentService;
        this.studyPublishingService = studyPublishingService;
        this.portalEnvironmentLanguageService = portalEnvironmentLanguageService;
        this.objectMapper = objectMapper;
    }

    /**
     * updates the dest environment with the given changes
     */
    @Transactional
    public PortalEnvironment applyChanges(String shortcode, EnvironmentName dest, PortalEnvironmentChange change, AdminUser operator) {
        PortalEnvironment destEnv = portalDiffService.loadPortalEnvForProcessing(shortcode, dest);
        return applyUpdate(destEnv, change, operator);
    }

    /**
     * applies the given update -- the destEnv provided must already be fully-hydrated from loadPortalEnv
     * returns the updated environment
     */
    protected PortalEnvironment applyUpdate(PortalEnvironment destEnv, PortalEnvironmentChange envChanges, AdminUser operator) {
        applyChangesToEnvConfig(destEnv, envChanges.getConfigChanges());

        applyChangesToPreRegSurvey(destEnv, envChanges.getPreRegSurveyChanges());
        applyChangesToSiteContent(destEnv, envChanges.getSiteContentChange());
        applyChangesToTriggers(destEnv, envChanges.getTriggerChanges());
        applyChangesToParticipantDashboardAlerts(destEnv, envChanges.getParticipantDashboardAlertChanges());
        applyChangesToLanguages(destEnv, envChanges.getLanguageChanges());
        for (StudyEnvironmentChange studyEnvChange : envChanges.getStudyEnvChanges()) {
            StudyEnvironment studyEnv = portalDiffService.loadStudyEnvForProcessing(studyEnvChange.getStudyShortcode(), destEnv.getEnvironmentName());
            studyPublishingService.applyChanges(studyEnv, studyEnvChange, destEnv);
        }
        try {
            PortalEnvironmentChangeRecord changeRecord = PortalEnvironmentChangeRecord.builder()
                    .adminUserId(operator.getId())
                    .portalId(destEnv.getPortalId())
                    .environmentName(destEnv.getEnvironmentName())
                    .portalEnvironmentChange(objectMapper.writeValueAsString(envChanges))
                    .build();
            portalEnvironmentChangeRecordDao.create(changeRecord);
        } catch (Exception e) {
            throw new InternalServerException("error writing publish audit log", e);
        }
        return destEnv;
    }

    /**
     * updates the passed-in config with the given changes.  Returns the updated config
     */
    protected PortalEnvironmentConfig applyChangesToEnvConfig(PortalEnvironment destEnv,
                                                              List<ConfigChange> configChanges) {
        if (configChanges.isEmpty()) {
            return destEnv.getPortalEnvironmentConfig();
        }
        try {
            for (ConfigChange change : configChanges) {
                PropertyUtils.setProperty(destEnv.getPortalEnvironmentConfig(), change.propertyName(), change.newValue());
            }
        } catch (Exception e) {
            throw new InternalServerException("Error copying properties during publish", e);
        }
        return portalEnvironmentConfigService.update(destEnv.getPortalEnvironmentConfig());
    }


    protected PortalEnvironment applyChangesToPreRegSurvey(PortalEnvironment destEnv, VersionedEntityChange<Survey> change) {
        if (!change.isChanged()) {
            return destEnv;
        }
        UUID newSurveyId = null;
        if (change.newStableId() != null) {
            newSurveyId = surveyService.findByStableId(change.newStableId(), change.newVersion(), destEnv.getPortalId()).get().getId();
        }
        destEnv.setPreRegSurveyId(newSurveyId);
        PublishingUtils.assignPublishedVersionIfNeeded(destEnv.getEnvironmentName(), destEnv.getPortalId(), change, surveyService);
        return portalEnvironmentService.update(destEnv);
    }

    protected PortalEnvironment applyChangesToSiteContent(PortalEnvironment destEnv, VersionedEntityChange<SiteContent> change) {
        if (!change.isChanged()) {
            return destEnv;
        }
        UUID newDocumentId = null;
        if (change.newStableId() != null) {
            newDocumentId = siteContentService.findByStableId(change.newStableId(), change.newVersion(), destEnv.getPortalId()).get().getId();
        }
        destEnv.setSiteContentId(newDocumentId);
        PublishingUtils.assignPublishedVersionIfNeeded(destEnv.getEnvironmentName(), destEnv.getPortalId(), change, siteContentService);
        return portalEnvironmentService.update(destEnv);
    }

    protected void applyChangesToTriggers(PortalEnvironment destEnv, ListChange<Trigger,
            VersionedConfigChange<EmailTemplate>> listChange) {
        for (Trigger config : listChange.addedItems()) {
            config.setPortalEnvironmentId(destEnv.getId());
            triggerService.create(config.cleanForCopying());
            destEnv.getTriggers().add(config);
            PublishingUtils.assignPublishedVersionIfNeeded(destEnv.getEnvironmentName(), config, emailTemplateService);
        }
        for (Trigger config : listChange.removedItems()) {
            triggerService.delete(config.getId(), CascadeProperty.EMPTY_SET);
            destEnv.getTriggers().remove(config);
        }
        for (VersionedConfigChange<EmailTemplate> change : listChange.changedItems()) {
            PublishingUtils.applyChangesToVersionedConfig(change, triggerService, emailTemplateService, destEnv.getEnvironmentName(), destEnv.getPortalId());
        }
    }

    protected void applyChangesToParticipantDashboardAlerts(PortalEnvironment destEnv, List<ParticipantDashboardAlertChange> changes) {
        for (ParticipantDashboardAlertChange change : changes) {
            Optional<ParticipantDashboardAlert> destAlert = portalDashboardConfigService.findByPortalEnvIdAndTrigger(destEnv.getId(), change.trigger());
            if (destAlert.isEmpty()) {
                // The alert doesn't exist in the dest env yet, so default all the required fields before
                // applying the changes from the change list
                ParticipantDashboardAlert newAlert = getDefaultDashboardAlert(destEnv, change.trigger());
                applyAlertChanges(newAlert, change.changes());
                portalDashboardConfigService.create(newAlert);
            } else {
                ParticipantDashboardAlert alert = destAlert.get();
                applyAlertChanges(alert, change.changes());
                portalDashboardConfigService.update(alert);
            }
        }
    }

    private ParticipantDashboardAlert getDefaultDashboardAlert(PortalEnvironment destEnv, AlertTrigger trigger) {
        return ParticipantDashboardAlert.builder()
                .portalEnvironmentId(destEnv.getId())
                .alertType(AlertType.PRIMARY)
                .title("")
                .detail("")
                .portalEnvironmentId(destEnv.getId())
                .trigger(trigger)
                .build();
    }

    protected void applyAlertChanges(ParticipantDashboardAlert alert, List<ConfigChange> changes) {
        try {
            for (ConfigChange alertChange : changes) {
                PublishingUtils.setPropertyEnumSafe(alert, alertChange.propertyName(), alertChange.newValue());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error applying changes to alert: " + alert.getId(), e);
        }
    }

    protected void applyChangesToLanguages(PortalEnvironment destEnv, ListChange<PortalEnvironmentLanguage, Object> languageChanges) {
        for (PortalEnvironmentLanguage language : languageChanges.addedItems()) {
            language.cleanForCopying();
            language.setPortalEnvironmentId(destEnv.getId());
            portalEnvironmentLanguageService.create(language);
        }
        for (PortalEnvironmentLanguage language : languageChanges.removedItems()) {
            portalEnvironmentLanguageService.delete(language.getId(), CascadeProperty.EMPTY_SET);
        }
    }

}
