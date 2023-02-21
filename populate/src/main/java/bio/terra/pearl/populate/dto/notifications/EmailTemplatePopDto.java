package bio.terra.pearl.populate.dto.notifications;

import bio.terra.pearl.core.model.notification.EmailTemplate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class EmailTemplatePopDto extends EmailTemplate {
    private String bodyPopulateFile;
}
