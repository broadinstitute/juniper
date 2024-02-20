package bio.terra.pearl.core.service.rule;

import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.Profile;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class EnrolleeRuleData {
    private final Enrollee enrollee;
    private final Profile profile;
}
