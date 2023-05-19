package bio.terra.pearl.core.factory.participant;

import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PortalParticipantUserFactory {
    @Autowired
    PortalEnvironmentFactory portalEnvironmentFactory;
    @Autowired
    PortalParticipantUserService portalParticipantUserService;
    @Autowired
    StudyEnvironmentService studyEnvironmentService;
    @Autowired
    EnrolleeService enrolleeService;

    /** creates a PortalParticipantUser for the given enrollee.  This will create a new portal and portal environment */
    public PortalParticipantUser buildPersisted(String testName, UUID enrolleeId) {
        Enrollee enrollee = enrolleeService.find(enrolleeId).get();
        StudyEnvironment studyEnvironment = studyEnvironmentService.find(enrollee.getStudyEnvironmentId()).get();
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(testName, studyEnvironment.getEnvironmentName());
        PortalParticipantUser ppUser = PortalParticipantUser.builder()
                .participantUserId(enrollee.getParticipantUserId())
                .portalEnvironmentId(portalEnv.getId()).build();
        return portalParticipantUserService.create(ppUser);
    }

    /** creates a PortalParticipantUser for the given enrollee in the portalEnv */
    public PortalParticipantUser buildPersisted(String testName, Enrollee enrollee, PortalEnvironment portalEnv) {
        PortalParticipantUser ppUser = PortalParticipantUser.builder()
            .participantUserId(enrollee.getParticipantUserId())
            .portalEnvironmentId(portalEnv.getId()).build();
        return portalParticipantUserService.create(ppUser);
    }

}
