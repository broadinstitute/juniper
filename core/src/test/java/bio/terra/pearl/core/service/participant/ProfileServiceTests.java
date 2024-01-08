package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.DaoTestUtils;
import bio.terra.pearl.core.model.participant.MailingAddress;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.workflow.DataAuditInfo;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ProfileServiceTests extends BaseSpringBootTest {
    @Autowired
    private ProfileService profileService;

    @Test
    public void testProfileCreatesWithMailingAddress() {
        Profile profile = Profile.builder()
                .familyName("someName" + RandomStringUtils.randomAlphabetic(4)).build();
        Profile savedProfile = profileService.create(profile, DataAuditInfo.builder().build());
        DaoTestUtils.assertGeneratedProperties(savedProfile);
        DaoTestUtils.assertGeneratedProperties(savedProfile.getMailingAddress());
        assertThat(savedProfile.getMailingAddressId(), equalTo(savedProfile.getMailingAddress().getId()));
    }

    @Test
    public void testProfileCreatesWithExistingMailingAddress() {
        Profile profile = Profile.builder()
                .familyName("someName" + RandomStringUtils.randomAlphabetic(4))
                .mailingAddress(MailingAddress.builder()
                        .city("someCity" + RandomStringUtils.randomAlphabetic(4)).build())
                .build();
        Profile savedProfile = profileService.create(profile, DataAuditInfo.builder().build());
        DaoTestUtils.assertGeneratedProperties(savedProfile);
        DaoTestUtils.assertGeneratedProperties(savedProfile.getMailingAddress());
        assertThat(savedProfile.getMailingAddressId(), equalTo(savedProfile.getMailingAddress().getId()));
        assertThat(savedProfile.getMailingAddress().getCity(), equalTo(profile.getMailingAddress().getCity()));
    }
}
