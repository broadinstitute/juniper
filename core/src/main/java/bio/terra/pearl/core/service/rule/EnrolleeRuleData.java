package bio.terra.pearl.core.service.rule;

import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.Profile;

public record EnrolleeRuleData(Enrollee enrollee, Profile profile) {}
