package bio.terra.pearl.core.model.participant;

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
public class FamilyMember extends BaseEntity {
    private UUID familyId;
    private UUID enrolleeId;
}
