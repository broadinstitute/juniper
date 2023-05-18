package bio.terra.pearl.core.model.datarepo;

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
    private UUID datasetId;
    private String datasetName;
    private String description;
    private Instant lastExported;
}
