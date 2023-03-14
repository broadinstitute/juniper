package bio.terra.pearl.core.factory;

import bio.terra.pearl.core.model.participant.Profile;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

@Component
public class ProfileFactory {
    public Profile.ProfileBuilder builder(String testName) {
        return Profile.builder()
                .contactEmail(RandomStringUtils.randomAlphabetic(10) + "@test.com")
                .givenName("given" + RandomStringUtils.randomAlphabetic(3))
                .familyName("family" + RandomStringUtils.randomAlphabetic(2));
    }
}
