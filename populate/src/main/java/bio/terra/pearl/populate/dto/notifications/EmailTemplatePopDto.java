package bio.terra.pearl.populate.dto.notifications;

import bio.terra.pearl.core.model.notification.EmailTemplate;
import bio.terra.pearl.populate.dto.FilePopulatable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter @NoArgsConstructor
public class EmailTemplatePopDto extends EmailTemplate {
    private List<LocalizedEmailTemplatePopDto> localizedEmailTemplateDtos = new ArrayList<>();;
}
