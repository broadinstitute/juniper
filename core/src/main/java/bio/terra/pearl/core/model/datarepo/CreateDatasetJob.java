package bio.terra.pearl.core.model.datarepo;

import bio.terra.pearl.core.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter @ToString
@SuperBuilder @NoArgsConstructor
public class CreateDatasetJob extends BaseEntity {
    private UUID studyId;
    private UUID studyEnvironmentId;
    private String tdrJobId;
    private String datasetName;
    private String status;
}
