package bio.terra.pearl.core.service.workflow;

import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.service.rule.EnrolleeRuleData;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public abstract class EnrolleeEvent implements BaseEvent {
    private Enrollee enrollee;
    private PortalParticipantUser portalParticipantUser;
    private EnrolleeRuleData enrolleeRuleData;
}
