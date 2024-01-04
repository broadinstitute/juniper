package bio.terra.pearl.populate.dto.notifications;

import bio.terra.pearl.core.model.notification.TriggeredAction;
import bio.terra.pearl.populate.dto.FilePopulatable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class TriggeredActionPopDto extends TriggeredAction implements FilePopulatable {
    private String emailTemplateStableId;
    private int emailTemplateVersion;
    private String populateFileName;
}
