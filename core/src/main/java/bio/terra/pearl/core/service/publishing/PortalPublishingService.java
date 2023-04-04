package bio.terra.pearl.core.service.publishing;

import bio.terra.pearl.core.dao.publishing.PortalEnvironmentChangeRecordDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.portal.PortalEnvironmentConfig;
import bio.terra.pearl.core.model.publishing.*;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.notification.NotificationConfigService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentConfigService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.site.SiteContentService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.survey.SurveyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PortalPublishingService {
    public static final List<String> CONFIG_IGNORE_PROPS = List.of("id", "createdAt", "lastUpdatedAt", "class",
            "studyEnvironmentId", "portalEnvironmentId", "emailTemplateId", "emailTemplate",
            "consentFormId", "consentForm", "surveyId", "survey", "versionedEntity");
    private PortalEnvironmentService portalEnvService;
    private PortalEnvironmentConfigService portalEnvironmentConfigService;
    private SiteContentService siteContentService;
    private SurveyService surveyService;
    private NotificationConfigService notificationConfigService;
    private ObjectMapper objectMapper;
    private PortalEnvironmentChangeRecordDao portalEnvironmentChangeRecordDao;
    private StudyEnvironmentService studyEnvironmentService;

    public PortalPublishingService(PortalEnvironmentService portalEnvService,
                                   PortalEnvironmentConfigService portalEnvironmentConfigService,
                                   SiteContentService siteContentService, SurveyService surveyService,
                                   NotificationConfigService notificationConfigService,
                                   ObjectMapper objectMapper,
                                   PortalEnvironmentChangeRecordDao portalEnvironmentChangeRecordDao,
                                   StudyEnvironmentService studyEnvironmentService) {
        this.portalEnvService = portalEnvService;
        this.portalEnvironmentConfigService = portalEnvironmentConfigService;
        this.siteContentService = siteContentService;
        this.surveyService = surveyService;
        this.notificationConfigService = notificationConfigService;
        this.objectMapper = objectMapper;
        this.portalEnvironmentChangeRecordDao = portalEnvironmentChangeRecordDao;
        this.studyEnvironmentService = studyEnvironmentService;
    }

    /** does a full update of all properties from the source to the dest */
    @Transactional
    public PortalEnvironment updateAll(String shortcode, EnvironmentName source, EnvironmentName dest, AdminUser user) throws Exception {
        PortalEnvironment destEnv = loadPortalEnvForProcessing(shortcode, dest);
        PortalEnvironment sourceEnv = loadPortalEnvForProcessing(shortcode, source);
        var changes = diffPortalEnvs(sourceEnv, destEnv);
        return applyUpdate(destEnv, changes, user);
    }

    /** updates the dest environment with the given changes */
    @Transactional
    public PortalEnvironment applyChanges(String shortcode, EnvironmentName dest, PortalEnvironmentChange change, AdminUser user) throws Exception {
        PortalEnvironment destEnv = loadPortalEnvForProcessing(shortcode, dest);
        return applyUpdate(destEnv, change, user);
    }

    protected PortalEnvironment applyUpdate(PortalEnvironment destEnv, PortalEnvironmentChange envChanges, AdminUser user) throws Exception {
        applyChangesToEnvConfig(destEnv.getPortalEnvironmentConfig(), envChanges.configChanges());
        portalEnvironmentConfigService.update(destEnv.getPortalEnvironmentConfig());
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


    public PortalEnvironmentChange diffPortalEnvs(String shortcode, EnvironmentName source, EnvironmentName dest) throws Exception {
        PortalEnvironment sourceEnv = loadPortalEnvForProcessing(shortcode, source);
        PortalEnvironment destEnv = loadPortalEnvForProcessing(shortcode, dest);
        return diffPortalEnvs(sourceEnv, destEnv);
    }

    public PortalEnvironmentChange diffPortalEnvs(PortalEnvironment sourceEnv, PortalEnvironment destEnv) throws Exception {
        var preRegRecord = new VersionedEntityChange(sourceEnv.getPreRegSurvey(), destEnv.getPreRegSurvey());
        var siteContentRecord = new VersionedEntityChange(sourceEnv.getSiteContent(), destEnv.getSiteContent());
        var envConfigChanges = ConfigChange.allChanges(sourceEnv.getPortalEnvironmentConfig(),
                destEnv.getPortalEnvironmentConfig(), CONFIG_IGNORE_PROPS);
        var notificationConfigChanges = diffConfigLists(sourceEnv.getNotificationConfigs(),
                destEnv.getNotificationConfigs(),
                CONFIG_IGNORE_PROPS);


        return new PortalEnvironmentChange(
                siteContentRecord,
                envConfigChanges,
                preRegRecord,
                notificationConfigChanges,
                null
        );
    }

    private PortalEnvironment loadPortalEnvForProcessing(String shortcode, EnvironmentName envName) {
        PortalEnvironment portalEnv = portalEnvService.findOne(shortcode, envName).get();
        if (portalEnv.getPortalEnvironmentConfigId() != null) {
            portalEnv.setPortalEnvironmentConfig(portalEnvironmentConfigService
                    .find(portalEnv.getPortalEnvironmentConfigId()).get());
        }
        if (portalEnv.getSiteContentId() != null) {
            portalEnv.setSiteContent(siteContentService.find(portalEnv.getSiteContentId()).get());
        }
        if (portalEnv.getPreRegSurveyId() != null) {
            portalEnv.setPreRegSurvey(surveyService.find(portalEnv.getPreRegSurveyId()).get());
        }
        var notificationConfigs = notificationConfigService.findByPortalEnvironmentId(portalEnv.getId());
        notificationConfigService.attachTemplates(notificationConfigs);
        portalEnv.setNotificationConfigs(notificationConfigs);

        return portalEnv;
    }

    public static <C extends VersionedEntityConfig> ListChange<C, VersionedConfigChange> diffConfigLists(
            List<C> sourceConfigs,
            List<C> destConfigs,
            List<String> ignoreProps)
    throws Exception {
        List<C> unmatchedDestConfigs = new ArrayList<>(destConfigs);
        List<VersionedConfigChange> changedRecords = new ArrayList();
        List<C> addedConfigs = new ArrayList<>();
        for (C sourceConfig : sourceConfigs) {
            var matchedConfig = unmatchedDestConfigs.stream().filter(
                    destConfig -> isVersionedConfigMatch(sourceConfig, destConfig))
                    .findAny().orElse(null);
            if (matchedConfig == null) {
                addedConfigs.add(sourceConfig);
            } else {
                // this remove only works if the config has an ID, since that's how BaseEntity equality works
                // that's fine, since we're only working with already-persisted entities in this list.
                unmatchedDestConfigs.remove(matchedConfig);
                var changeRecord = new VersionedConfigChange(
                        sourceConfig.getId(), matchedConfig.getId(),
                        ConfigChange.allChanges(sourceConfig, matchedConfig, ignoreProps),
                        new VersionedEntityChange(sourceConfig.getVersionedEntity(), matchedConfig.getVersionedEntity())
                );
                if (changeRecord.isChanged()) {
                    changedRecords.add(changeRecord);
                }

            }
        }
        return new ListChange<>(addedConfigs, unmatchedDestConfigs, changedRecords);
    }

    /** for now, just checks to see if they reference the same versioned document */
    public static boolean isVersionedConfigMatch(VersionedEntityConfig configA, VersionedEntityConfig configB) {
        if (configA == null || configB == null) {
            return configA == configB;
        }
        if (configA.getVersionedEntity() == null || configB.getVersionedEntity() == null) {
            return false;
        }
        return Objects.equals(configA.getVersionedEntity().getStableId(), configB.getVersionedEntity().getStableId());
    }

    public StudyEnvironmentChange diffStudyEnvs(String studyShortcode, EnvironmentName source, EnvironmentName dest) throws Exception {
        StudyEnvironment sourceEnv = loadStudyEnvForProcessing(studyShortcode, source);
        StudyEnvironment destEnv = loadStudyEnvForProcessing(studyShortcode, dest);
        return diffStudyEnvs(studyShortcode, sourceEnv, destEnv);
    }

    public StudyEnvironmentChange diffStudyEnvs(String studyShortcode, StudyEnvironment sourceEnv, StudyEnvironment destEnv) throws Exception {
        var envConfigChanges = ConfigChange.allChanges(
                sourceEnv.getStudyEnvironmentConfig(),
                destEnv.getStudyEnvironmentConfig(),
                CONFIG_IGNORE_PROPS);
        var preEnrollChange = new VersionedEntityChange(sourceEnv.getPreEnrollSurvey(), destEnv.getPreEnrollSurvey());
        var consentChanges = diffConfigLists(
                sourceEnv.getConfiguredConsents(),
                destEnv.getConfiguredConsents(),
                CONFIG_IGNORE_PROPS);
        var surveyChanges = diffConfigLists(
                sourceEnv.getConfiguredSurveys(),
                destEnv.getConfiguredSurveys(),
                CONFIG_IGNORE_PROPS);
        var notificationConfigChanges = diffConfigLists(
                sourceEnv.getNotificationConfigs(),
                destEnv.getNotificationConfigs(),
                CONFIG_IGNORE_PROPS);

        return new StudyEnvironmentChange(
                studyShortcode,
                envConfigChanges,
                preEnrollChange,
                consentChanges,
                surveyChanges,
                notificationConfigChanges
        );
    }

    private StudyEnvironment loadStudyEnvForProcessing(String shortcode, EnvironmentName envName) {
        StudyEnvironment studyEnvironment = studyEnvironmentService.findByStudy(shortcode, envName).get();
        return studyEnvironmentService.loadWithAllContent(studyEnvironment);
    }


}
