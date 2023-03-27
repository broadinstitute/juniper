package bio.terra.pearl.core.factory.portal;

import bio.terra.pearl.core.model.portal.MailingListContact;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

@Component
public class MailingListContactFactory {
    public MailingListContact.MailingListContactBuilder builder(String testName) {
        String seed = testName + " " + RandomStringUtils.randomAlphabetic(4);
        return MailingListContact.builder()
                .name("contact " + seed)
                .email(seed + "@test.com");
    }
}
