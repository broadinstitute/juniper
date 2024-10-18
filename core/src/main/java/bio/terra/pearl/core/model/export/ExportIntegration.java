package bio.terra.pearl.core.model.export;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.study.StudyEnvAttached;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ExportIntegration extends BaseEntity implements StudyEnvAttached {
    private String name;
    private UUID studyEnvironmentId;
    @Builder.Default
    private boolean enabled = true;
    private ExportDestinationType destinationType;
    private String destinationUrl;
    private UUID exportOptionsId;
    private ExportOptions exportOptions;
}
