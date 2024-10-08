package bio.terra.pearl.core.model.export.datarepo;

import bio.terra.pearl.core.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter @ToString
@SuperBuilder @NoArgsConstructor
public class Dataset extends BaseEntity {
    private UUID studyEnvironmentId;
    private UUID tdrDatasetId;
    private String datasetName;
    private UUID createdBy;
    private String description;
    private DatasetStatus status;
    private Instant lastExported;
}
