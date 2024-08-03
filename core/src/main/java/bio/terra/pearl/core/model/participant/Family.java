package bio.terra.pearl.core.model.participant;

import bio.terra.pearl.core.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Family extends BaseEntity {
    private UUID probandEnrolleeId;
    private UUID studyEnvironmentId;
    private String shortcode;

    // optional related entities
    private Enrollee proband;
    private List<Enrollee> members;
    private List<EnrolleeRelation> relations;
}
