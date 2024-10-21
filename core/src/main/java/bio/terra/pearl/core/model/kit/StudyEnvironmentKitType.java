package bio.terra.pearl.core.model.kit;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.study.StudyEnvAttached;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter @Setter @SuperBuilder
@NoArgsConstructor
public class StudyEnvironmentKitType extends BaseEntity implements StudyEnvAttached {
    private UUID studyEnvironmentId;
    private UUID kitTypeId;
}
