package bio.terra.pearl.core.model.export.datarepo;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.study.StudyEnvAttached;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter @ToString
@SuperBuilder @NoArgsConstructor
public class DataRepoJob extends BaseEntity implements StudyEnvAttached {
    private UUID studyEnvironmentId;
    private String tdrJobId;
    private String datasetName;
    private UUID datasetId;
    private String status;
    private JobType jobType;
}
