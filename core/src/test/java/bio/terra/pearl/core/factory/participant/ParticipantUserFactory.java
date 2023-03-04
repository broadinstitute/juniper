package bio.terra.pearl.core.factory.participant;

import bio.terra.pearl.core.factory.EnvironmentFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.service.participant.ParticipantUserService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ParticipantUserFactory {
    @Autowired
    private EnvironmentFactory environmentFactory;
    @Autowired
    private ParticipantUserService participantUserService;
    @Autowired
    private PortalParticipantUserService portalParticipantUserService;

    public ParticipantUser.ParticipantUserBuilder builder(String testName) {
        return ParticipantUser.builder()
                .username(RandomStringUtils.randomAlphabetic(10) + "@test.com");
    }

    public ParticipantUser.ParticipantUserBuilder builderWithDependencies(ParticipantUser.ParticipantUserBuilder builder,
                                                                  String testName) {
        if (builder.getEnvironmentName() == null) {
            builder.environment(environmentFactory.buildPersisted(testName));
        }
        return builder;
    }

    public ParticipantUser.ParticipantUserBuilder builderWithDependencies(String testName) {
        return builderWithDependencies(builder(testName), testName);
    }

    public ParticipantUser buildPersisted(ParticipantUser.ParticipantUserBuilder builder, String testName) {
        return participantUserService.create(builder.build());
    }

    public ParticipantUser buildPersisted(EnvironmentName envName, String testName) {
        ParticipantUser.ParticipantUserBuilder userBuilder = builder(testName)
                .environmentName(envName);
        return buildPersisted(userBuilder, testName);
    }

    public ParticipantUserAndPortalUser buildPersisted(PortalEnvironment portalEnv, String testName) {
        ParticipantUser user = buildPersisted(portalEnv.getEnvironmentName(), testName);
        PortalParticipantUser ppUser = PortalParticipantUser.builder()
                .participantUserId(user.getId())
                .portalEnvironmentId(portalEnv.getId()).build();

        // enrollment requires an already-existing portalParticipantUser
        ppUser = portalParticipantUserService.create(ppUser);
        return new ParticipantUserAndPortalUser(user, ppUser);
    }

    public record ParticipantUserAndPortalUser(ParticipantUser user, PortalParticipantUser ppUser) {}
}
