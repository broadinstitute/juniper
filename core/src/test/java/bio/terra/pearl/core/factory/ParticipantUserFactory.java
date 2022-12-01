package bio.terra.pearl.core.factory;

import bio.terra.pearl.core.model.ParticipantUser;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ParticipantUserFactory {
    @Autowired
    private EnvironmentFactory environmentFactory;
    public ParticipantUser.ParticipantUserBuilder builder(String testName) {
        return ParticipantUser.builder()
                .username(RandomStringUtils.randomAlphabetic(10) + "@test.com");
    }

    public ParticipantUser.ParticipantUserBuilder builderWithDependencies(String testName) {
        return builder(testName)
                .environment(environmentFactory.buildPersisted(testName));
    }
}
