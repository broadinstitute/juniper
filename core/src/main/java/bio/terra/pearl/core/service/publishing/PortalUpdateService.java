package bio.terra.pearl.core.service.publishing;

import bio.terra.pearl.core.dao.publishing.PortalEnvironmentChangeRecordDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.notification.NotificationConfig;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.portal.PortalEnvironmentConfig;
import bio.terra.pearl.core.model.publishing.*;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.notification.EmailTemplateService;
import bio.terra.pearl.core.service.notification.NotificationConfigService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentConfigService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.site.SiteContentService;
import bio.terra.pearl.core.service.survey.SurveyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** dedicated service for applying deltas to portal environments */
@Service
public class PortalUpdateService {
    private PortalDiffService portalDiffService;
    private PortalEnvironmentService portalEnvironmentService;
    private PortalEnvironmentConfigService portalEnvironmentConfigService;
    private PortalEnvironmentChangeRecordDao portalEnvironmentChangeRecordDao;
    private NotificationConfigService notificationConfigService;
    private SurveyService surveyService;
    private EmailTemplateService emailTemplateService;
    private SiteContentService siteContentService;
    private ObjectMapper objectMapper;

    public PortalUpdateService(PortalDiffService portalDiffService,
                               PortalEnvironmentService portalEnvironmentService,
                               PortalEnvironmentConfigService portalEnvironmentConfigService,
                               PortalEnvironmentChangeRecordDao portalEnvironmentChangeRecordDao,
                               NotificationConfigService notificationConfigService, SurveyService surveyService,
                               EmailTemplateService emailTemplateService, SiteContentService siteContentService,
                               ObjectMapper objectMapper) {
        this.portalDiffService = portalDiffService;
        this.portalEnvironmentService = portalEnvironmentService;
        this.portalEnvironmentConfigService = portalEnvironmentConfigService;
        this.portalEnvironmentChangeRecordDao = portalEnvironmentChangeRecordDao;
        this.notificationConfigService = notificationConfigService;
        this.surveyService = surveyService;
        this.emailTemplateService = emailTemplateService;
        this.siteContentService = siteContentService;
        this.objectMapper = objectMapper;
    }


    /** does a full update of all properties from the source to the dest */
    @Transactional
    public PortalEnvironment updateAll(String shortcode, EnvironmentName source, EnvironmentName dest, AdminUser user) throws Exception {
        PortalEnvironment destEnv = portalDiffService.loadPortalEnvForProcessing(shortcode, dest);
        PortalEnvironment sourceEnv = portalDiffService.loadPortalEnvForProcessing(shortcode, source);
        var changes = portalDiffService.diffPortalEnvs(sourceEnv, destEnv);
        return applyUpdate(destEnv, changes, user);
    }

    /** updates the dest environment with the given changes */
    @Transactional
    public PortalEnvironment applyChanges(String shortcode, EnvironmentName dest, PortalEnvironmentChange change, AdminUser user) throws Exception {
        PortalEnvironment destEnv = portalDiffService.loadPortalEnvForProcessing(shortcode, dest);
        return applyUpdate(destEnv, change, user);
    }

    protected PortalEnvironment applyUpdate(PortalEnvironment destEnv, PortalEnvironmentChange envChanges, AdminUser user) throws Exception {
        applyChangesToEnvConfig(destEnv.getPortalEnvironmentConfig(), envChanges.configChanges());
        portalEnvironmentConfigService.update(destEnv.getPortalEnvironmentConfig());
        applyChangesToPreRegSurvey(destEnv, envChanges.preRegSurveyChanges());
        applyChangesToSiteContent(destEnv, envChanges.siteContentChange());
        applyChangesToNotificationConfigs(destEnv, envChanges.notificationConfigChanges());

        var changeRecord = PortalEnvironmentChangeRecord.builder()
                .adminUserId(user.getId())
                .portalEnvironmentChange(objectMapper.writeValueAsString(envChanges))
                .build();
        portalEnvironmentChangeRecordDao.create(changeRecord);
        return destEnv;
    }

    /** updates the passed-in config with the given changes.  Returns the updated config */
    protected PortalEnvironmentConfig applyChangesToEnvConfig(PortalEnvironmentConfig config, List<ConfigChange> changes) throws Exception {
        for (ConfigChange change : changes) {
            PropertyUtils.setProperty(config, change.propertyName(), change.newValue());
        }
        return config;
    }

    protected void applyChangesToPreRegSurvey(PortalEnvironment portalEnv, VersionedEntityChange change) throws Exception {
        if (change.isChanged()) {
            UUID newStableId = null;
            if (change.newStableId() != null) {
                newStableId = surveyService.findByStableId(change.newStableId(), change.newVersion()).get().getId();
            }
            portalEnv.setPreRegSurveyId(newStableId);
            portalEnvironmentService.update(portalEnv);
        }
    }

    protected void applyChangesToSiteContent(PortalEnvironment portalEnv, VersionedEntityChange change) throws Exception {
        if (change.isChanged()) {
            UUID newDocumentId = null;
            if (change.newStableId() != null) {
                newDocumentId = siteContentService.findByStableId(change.newStableId(), change.newVersion()).get().getId();
            }
            portalEnv.setSiteContentId(newDocumentId);
            portalEnvironmentService.update(portalEnv);
        }
    }

    protected void applyChangesToNotificationConfigs(PortalEnvironment portalEnv, ListChange<NotificationConfig,
            VersionedConfigChange> listChange) throws Exception {
        for(NotificationConfig config : listChange.addedItems()) {
            config.setPortalEnvironmentId(portalEnv.getId());
            notificationConfigService.create(config);
        }
        for(NotificationConfig config : listChange.removedItems()) {
            notificationConfigService.delete(config.getId(), CascadeProperty.EMPTY_SET);
        }
        for(VersionedConfigChange change : listChange.changedItems()) {
            applyChangesToNotificationConfig(change);
        }
    }

    protected void applyChangesToNotificationConfig(VersionedConfigChange versionedConfigChange) throws Exception {
        NotificationConfig destConfig = notificationConfigService.find(versionedConfigChange.destId()).get();
        for (ConfigChange change : versionedConfigChange.configChanges()) {
            PropertyUtils.setProperty(destConfig, change.propertyName(), change.newValue());
        }
        if (versionedConfigChange.documentChange().isChanged()) {
            VersionedEntityChange docChange = versionedConfigChange.documentChange();
            UUID newDocumentId = null;
            if (docChange.newStableId() != null) {
                newDocumentId = emailTemplateService.findByStableId(docChange.newStableId(), docChange.newVersion()).get().getId();
            }
            destConfig.setEmailTemplateId(newDocumentId);
        }
        notificationConfigService.update(destConfig);
    }

}
