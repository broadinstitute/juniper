package bio.terra.pearl.core.model.publishing;

import bio.terra.pearl.core.model.notification.NotificationConfig;
import java.util.List;

/** for now, you can only include changes to a single study environment */
public record PortalEnvironmentChange(VersionedEntityChange siteContentChange,
                                      List<ConfigChange> configChanges,
                                      VersionedEntityChange preRegSurveyChanges,
                                      ListChange<NotificationConfig, VersionedConfigChange> notificationConfigChanges,
                                      List<StudyEnvironmentChange> studyEnvChanges)
{}
