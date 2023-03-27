package bio.terra.pearl.core.model.publishing;

import bio.terra.pearl.core.model.notification.NotificationConfig;
import java.util.List;

public record NotificationConfigChangeRecord(List<ConfigChangeRecord> configChanges,
                                             VersionedEntityChangeRecord templateChange) {
    public NotificationConfigChangeRecord(NotificationConfig source,NotificationConfig dest, List<String> ignoreProps) throws Exception {
        this(
                ConfigChangeRecord.allChanges(source, dest, ignoreProps),
                new VersionedEntityChangeRecord(source.getEmailTemplate(), dest.getEmailTemplate())
        );
    }

}
