package bio.terra.pearl.core.model.export;

import bio.terra.pearl.core.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ExportIntegration extends BaseEntity {
    private String name;
    private UUID studyEnvironmentId;
    private boolean enabled = true;
    private ExportDestinationType destinationType;
    private String destinationUrl;
    private UUID exportOptionsId;
    private ExportOptions exportOptions;
}
