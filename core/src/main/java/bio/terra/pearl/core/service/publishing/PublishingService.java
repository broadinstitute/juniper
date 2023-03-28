package bio.terra.pearl.core.service.publishing;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.notification.NotificationConfig;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.publishing.*;
import bio.terra.pearl.core.service.notification.NotificationConfigService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentConfigService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.site.SiteContentService;
import bio.terra.pearl.core.service.survey.SurveyService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

@Service
public class PublishingService {
    public static final List<String> BASE_IGNORE_PROPS = List.of("id", "createdAt", "lastUpdatedAt", "class");
    public static final List<String> NOTIFICATION_CONFIG_IGNORE_PROPS = Stream
            .of(List.of("studyEnvironmentId", "portalEnvironmentId", "emailTemplateId", "emailTemplate"), BASE_IGNORE_PROPS)
            .flatMap(Collection::stream).toList();
    private PortalEnvironmentService portalEnvService;
    private PortalEnvironmentConfigService portalEnvironmentConfigService;
    private SiteContentService siteContentService;
    private SurveyService surveyService;
    private NotificationConfigService notificationConfigService;

    public PublishingService(PortalEnvironmentService portalEnvService,
                             PortalEnvironmentConfigService portalEnvironmentConfigService,
                             SiteContentService siteContentService, SurveyService surveyService,
                             NotificationConfigService notificationConfigService) {
        this.portalEnvService = portalEnvService;
        this.portalEnvironmentConfigService = portalEnvironmentConfigService;
        this.siteContentService = siteContentService;
        this.surveyService = surveyService;
        this.notificationConfigService = notificationConfigService;
    }

    public PortalEnvironmentChangeRecord diff(String shortcode, EnvironmentName source, EnvironmentName dest) throws Exception {

        PortalEnvironment sourceEnv = loadForDiffing(shortcode, source);
        PortalEnvironment destEnv = loadForDiffing(shortcode, dest);
        var preRegRecord = new VersionedEntityChangeRecord(sourceEnv.getPreRegSurvey(), destEnv.getPreRegSurvey());
        var siteContentRecord = new VersionedEntityChangeRecord(sourceEnv.getSiteContent(), destEnv.getSiteContent());
        var envConfigChanges = ConfigChangeRecord.allChanges(sourceEnv.getPortalEnvironmentConfig(),
                destEnv.getPortalEnvironmentConfig(), BASE_IGNORE_PROPS);
        var notificationConfigChanges = diffNotifications(sourceEnv.getNotificationConfigs(), destEnv.getNotificationConfigs());
        return new PortalEnvironmentChangeRecord(
               siteContentRecord,
               envConfigChanges,
               preRegRecord,
               notificationConfigChanges
        );
    }

    private PortalEnvironment loadForDiffing(String shortcode, EnvironmentName envName) {
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

    public static ListChangeRecord<NotificationConfig, NotificationConfigChangeRecord> diffNotifications(List<NotificationConfig> sourceConfigs,
                                                                                       List<NotificationConfig> destConfigs)
    throws Exception {
        List<NotificationConfigChangeRecord> changedRecords = new ArrayList();
        List<NotificationConfig> addedConfigs = new ArrayList<>();
        for (NotificationConfig sourceConfig : sourceConfigs) {
            var matchedConfig = destConfigs.stream().filter(
                    destConfig -> isNotificationConfigMatch(sourceConfig, destConfig)).findAny().orElse(null);
            if (matchedConfig == null) {
                addedConfigs.add(sourceConfig);
            } else {
                changedRecords.add(new NotificationConfigChangeRecord(sourceConfig, matchedConfig, NOTIFICATION_CONFIG_IGNORE_PROPS));
            }
        }
        List<NotificationConfig> removedConfigs = destConfigs.stream()
                .filter(destConfig -> sourceConfigs.stream().filter(
                        sourceConfig -> isNotificationConfigMatch(sourceConfig, destConfig)).findAny().isEmpty())
                .toList();
        return new ListChangeRecord<>(addedConfigs, removedConfigs, changedRecords);
    }

    /** gets whether to treat the notifications as the same for diffing purposes */
    public static boolean isNotificationConfigMatch(NotificationConfig configA, NotificationConfig configB) {
        if (configA == null || configB == null) {
            return configA == configB;
        }
        return Objects.equals(configA.getNotificationType(), configB.getNotificationType()) &&
                Objects.equals(configA.getDeliveryType(), configB.getDeliveryType()) &&
                Objects.equals(configA.getTaskType(), configB.getTaskType()) &&
                Objects.equals(configA.getEventType(), configB.getEventType());
    }


}
