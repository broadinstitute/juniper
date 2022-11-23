package bio.terra.pearl.core.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter @SuperBuilder
@NoArgsConstructor
public class StudyEnvironment extends BaseEntity {
    private EnvironmentName environmentName;
    private UUID studyId;
}
