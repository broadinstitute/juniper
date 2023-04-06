package bio.terra.pearl.core.factory.participant;

import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EnrolleeFactory {
    @Autowired
    private EnrolleeService enrolleeService;
    @Autowired
    private ParticipantUserFactory participantUserFactory;
    @Autowired
    private StudyEnvironmentFactory studyEnvironmentFactory;
    @Autowired
    private PortalEnvironmentFactory portalEnvironmentFactory;
    @Autowired
    private PortalParticipantUserService portalParticipantUserService;

    public Enrollee.EnrolleeBuilder builder(String testName) {
        return Enrollee.builder()
                .withdrawn(false);
    }

    public Enrollee.EnrolleeBuilder builderWithDependencies(String testName) {
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(testName);
        ParticipantUser participantUser = participantUserFactory.buildPersisted(
                participantUserFactory.builder(testName)
                        .environmentName(studyEnv.getEnvironmentName()),
                testName
        );
        return builder(testName)
                .participantUserId(participantUser.getId())
                .studyEnvironmentId(studyEnv.getId());
    }

    public Enrollee buildPersisted(String testName) {
        return enrolleeService.create(builderWithDependencies(testName).build());
    }

    public Enrollee buildPersisted(String testName, UUID studyEnvironmentId, UUID participantUserId) {
        Enrollee enrollee = Enrollee.builder()
                .studyEnvironmentId(studyEnvironmentId)
                .participantUserId(participantUserId)
                .build();
        return enrolleeService.create(enrollee);
    }

    public EnrolleeBundle buildWithPortalUser(String testName) {
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(testName);
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, testName);
        return buildWithPortalUser(testName, portalEnv, studyEnv);
    }

    public EnrolleeBundle buildWithPortalUser(String testName, PortalEnvironment portalEnv, StudyEnvironment studyEnv) {
        ParticipantUser user = participantUserFactory.buildPersisted(studyEnv.getEnvironmentName(), testName);
        PortalParticipantUser ppUser = PortalParticipantUser.builder()
                .participantUserId(user.getId())
                .portalEnvironmentId(portalEnv.getId()).build();
        ppUser = portalParticipantUserService.create(ppUser);
        Enrollee enrollee = buildPersisted(testName, studyEnv.getId(), user.getId());
        return new EnrolleeBundle(enrollee, ppUser, portalEnv.getPortalId());
    }


    public record EnrolleeBundle(Enrollee enrollee, PortalParticipantUser portalParticipantUser, UUID portalId) {}
}
