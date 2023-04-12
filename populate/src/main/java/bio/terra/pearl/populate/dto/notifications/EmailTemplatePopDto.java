package bio.terra.pearl.populate.dto.notifications;

import bio.terra.pearl.core.model.notification.EmailTemplate;
import bio.terra.pearl.populate.dto.FilePopulatable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class EmailTemplatePopDto extends EmailTemplate implements FilePopulatable {
    private String bodyPopulateFile;
    private String populateFileName;
}
