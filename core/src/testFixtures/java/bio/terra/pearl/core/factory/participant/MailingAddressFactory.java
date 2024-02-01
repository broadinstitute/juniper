package bio.terra.pearl.core.factory.participant;

import bio.terra.pearl.core.model.address.MailingAddress;
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
                .postalCode(RandomStringUtils.randomNumeric(5));
    }
}
