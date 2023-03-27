package bio.terra.pearl.core.model.publishing;

import bio.terra.pearl.core.model.notification.NotificationConfig;
import java.util.List;

public record PortalEnvironmentChangeRecord(VersionedEntityChangeRecord siteContentChange,
                                            List<ConfigChangeRecord> configChanges,
                                            VersionedEntityChangeRecord preRegSurveyChanges,
                                            ListChangeRecord<NotificationConfig, NotificationConfigChangeRecord> notificationConfigChanges)
{}
