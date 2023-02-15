package bio.terra.pearl.populate.dto.notifications;

import bio.terra.pearl.core.model.notification.NotificationConfig;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class NotificationConfigPopDto extends NotificationConfig {
    private EmailTemplatePopDto emailTemplate;
}
