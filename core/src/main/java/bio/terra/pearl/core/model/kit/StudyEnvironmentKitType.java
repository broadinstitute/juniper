package bio.terra.pearl.core.model.kit;

import bio.terra.pearl.core.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter @Setter @SuperBuilder
@NoArgsConstructor
public class StudyEnvironmentKitType extends BaseEntity {
    private UUID studyEnvironmentId;
    private UUID kitTypeId;
}
