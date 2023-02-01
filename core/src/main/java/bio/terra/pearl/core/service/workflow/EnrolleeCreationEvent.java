package bio.terra.pearl.core.service.workflow;


import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.rule.EnrolleeRuleData;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * contains data associated with enrollee creation.
 * To avoid requerying data over and over again, the enrollee in the event should be updated in-place as the event
 * propagates.  For example, if a listener creates a task for the enrollee, that task should be added to the Enrollee
 * object attached to the event.  That way later consumers will be able to see the tasks without reloading the DB
 * every time.
 */
@Getter @Setter @Builder
public class EnrolleeCreationEvent {
    private Enrollee enrollee;
    private StudyEnvironment studyEnvironment;
    private PortalParticipantUser portalParticipantUser;
    private EnrolleeRuleData enrolleeRuleData;
}
