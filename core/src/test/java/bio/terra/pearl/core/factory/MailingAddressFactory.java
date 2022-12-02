package bio.terra.pearl.core.factory;

import bio.terra.pearl.core.model.participant.MailingAddress;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

@Component
public class MailingAddressFactory {
    public MailingAddress.MailingAddressBuilder builder(String testName) {
        return MailingAddress.builder()
                .street1(RandomStringUtils.randomAlphabetic(10) + " street")
                .city(testName + " city")
                .state("MA")
                .country("US")
                .zip(RandomStringUtils.randomNumeric(5));
    }
}
