package bio.terra.pearl.populate.dto.notifications;

import bio.terra.pearl.core.model.notification.LocalizedEmailTemplate;
import bio.terra.pearl.populate.dto.FilePopulatable;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LocalizedEmailTemplatePopDto extends LocalizedEmailTemplate implements FilePopulatable {
    private String bodyPopulateFile;
    private String populateFileName;
}
