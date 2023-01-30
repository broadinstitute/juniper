package bio.terra.pearl.core.service.study;


import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantTask;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * contains data associated with enrollee creation.  We use fluent accessors because @Builder won't work with
 * extending ApplicationEvent since it does not have a no-arg constructor.
 */
@Getter @Setter @Builder
public class EnrolleeCreationEvent {
    private Enrollee enrollee;
    private StudyEnvironment studyEnvironment;
    private PortalParticipantUser portalParticipantUser;
    @Builder.Default
    private List<ParticipantTask> newParticipantTasks = new ArrayList<>();
}
