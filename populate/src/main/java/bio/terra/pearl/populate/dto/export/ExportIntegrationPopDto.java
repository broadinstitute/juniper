package bio.terra.pearl.populate.dto.export;

import bio.terra.pearl.core.model.export.ExportIntegration;
import bio.terra.pearl.core.service.export.ExportOptions;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class ExportIntegrationPopDto extends ExportIntegration {
    private ExportOptions exportOptionsObj;
}
