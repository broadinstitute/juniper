package bio.terra.pearl.core.factory;

import bio.terra.pearl.core.model.ParticipantUser;
import org.apache.commons.lang3.RandomStringUtils;

public class ParticipantUserFactory {
    public static ParticipantUser.ParticipantUserBuilder builder() {
        ParticipantUser.ParticipantUserBuilder builder = ParticipantUser.builder();
        builder.username(RandomStringUtils.randomAlphabetic(10) + "@test.com");
        return builder;
    }

    public static ParticipantUser.ParticipantUserBuilder builderWithDependencies(DaoHolder daoHolder) {
        ParticipantUser.ParticipantUserBuilder builder = builder();
        return builder.environment(EnvironmentFactory.buildPersisted(daoHolder));
    }
}
