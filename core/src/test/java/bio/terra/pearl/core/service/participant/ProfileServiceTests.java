package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.DaoTestUtils;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.address.MailingAddress;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.audit.DataChangeRecord;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.EnrolleeRelation;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.participant.RelationshipType;
import bio.terra.pearl.core.model.workflow.HubResponse;
import bio.terra.pearl.core.service.workflow.DataChangeRecordService;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ProfileServiceTests extends BaseSpringBootTest {
    @Autowired
    private ProfileService profileService;
    @Autowired
    private DataChangeRecordService dataChangeRecordService;
    @Autowired
    private EnrolleeFactory enrolleeFactory;
    @Autowired
    private EnrolleeRelationService enrolleeRelationService;
    @Autowired
    private ParticipantUserService participantUserService;
    @Autowired
    private EnrolleeService enrolleeService;
    @Autowired
    private PortalParticipantUserService portalParticipantUserService;

    @Test
    @Transactional
    public void testProfileCreatesWithMailingAddress() {
        Profile profile = Profile.builder()
                .familyName("someName" + RandomStringUtils.randomAlphabetic(4)).build();
        Profile savedProfile = profileService.create(profile, DataAuditInfo.builder().build());
        DaoTestUtils.assertGeneratedProperties(savedProfile);
        DaoTestUtils.assertGeneratedProperties(savedProfile.getMailingAddress());
        assertThat(savedProfile.getMailingAddressId(), equalTo(savedProfile.getMailingAddress().getId()));
    }

    @Test
    @Transactional
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

    @Test
    @Transactional
    public void testProfileAudit(TestInfo info) {
        EnrolleeFactory.EnrolleeBundle bundle = enrolleeFactory.buildWithPortalUser(getTestName(info));

        Profile profile = bundle.enrollee().getProfile();


        profile.setGivenName("NEW GIVEN NAME");
        profile.setFamilyName("NEW FAMILY NAME");
        profileService.update(profile, DataAuditInfo
                .builder()
                .portalParticipantUserId(bundle.portalParticipantUser().getId())
                .responsibleUserId(bundle.portalParticipantUser().getParticipantUserId())
                .enrolleeId(bundle.enrollee().getId())
                .build());

        List<DataChangeRecord> dataChangeRecords = dataChangeRecordService.findByEnrollee(bundle.enrollee().getId());

        Assertions.assertEquals(1, dataChangeRecords.size());

        DataChangeRecord record = dataChangeRecords.get(0);

        Assertions.assertFalse(record.getOldValue().contains("NEW GIVEN NAME"));
        Assertions.assertFalse(record.getOldValue().contains("NEW FAMILY NAME"));

        Assertions.assertTrue(record.getNewValue().contains("NEW GIVEN NAME"));
        Assertions.assertTrue(record.getNewValue().contains("NEW FAMILY NAME"));
    }

    @Test
    @Transactional
    public void testProfileAuditContainsMailingList(TestInfo info) {
        EnrolleeFactory.EnrolleeBundle bundle = enrolleeFactory.buildWithPortalUser(getTestName(info));

        Profile profile = bundle.enrollee().getProfile();

        String firstStreet1 = RandomStringUtils.randomAlphabetic(20);
        String firstCity = RandomStringUtils.randomAlphabetic(20);

        MailingAddress first = MailingAddress.builder().id(profile.getMailingAddressId()).street1(firstStreet1).city(firstCity).build();

        profile.setMailingAddress(first);
        profile = profileService.updateWithMailingAddress(profile, DataAuditInfo
                .builder()
                .portalParticipantUserId(bundle.portalParticipantUser().getId())
                .responsibleUserId(bundle.portalParticipantUser().getParticipantUserId())
                .enrolleeId(bundle.enrollee().getId())
                .build());

        List<DataChangeRecord> dataChangeRecords = dataChangeRecordService.findByEnrollee(bundle.enrollee().getId());

        Assertions.assertEquals(1, dataChangeRecords.size());

        DataChangeRecord record = dataChangeRecords.get(0);
        Assertions.assertTrue(record.getNewValue().contains(firstStreet1));
        Assertions.assertTrue(record.getNewValue().contains(firstCity));

        String secondStreet1 = RandomStringUtils.randomAlphabetic(20);
        String secondCity = RandomStringUtils.randomAlphabetic(20);


        MailingAddress second = MailingAddress.builder().id(profile.getMailingAddressId()).street1(secondStreet1).city(secondCity).build();

        profile.setMailingAddress(second);
        profileService.updateWithMailingAddress(profile, DataAuditInfo
                .builder()
                .portalParticipantUserId(bundle.portalParticipantUser().getId())
                .responsibleUserId(bundle.portalParticipantUser().getParticipantUserId())
                .enrolleeId(bundle.enrollee().getId())
                .build());

        dataChangeRecords = dataChangeRecordService.findByEnrollee(bundle.enrollee().getId());

        Assertions.assertEquals(2, dataChangeRecords.size());

        // get most recent of the two
        record = (dataChangeRecords.get(0).getCreatedAt().isAfter(dataChangeRecords.get(1).getCreatedAt())
                ? dataChangeRecords.get(0)
                : dataChangeRecords.get(1));

        Assertions.assertTrue(record.getOldValue().contains(firstStreet1));
        Assertions.assertTrue(record.getOldValue().contains(firstCity));

        Assertions.assertTrue(record.getNewValue().contains(secondStreet1));
        Assertions.assertTrue(record.getNewValue().contains(secondCity));

    }

    @Test
    @Transactional
    public void testProfileUpdateWithMailingAddress(TestInfo info) {
        EnrolleeFactory.EnrolleeBundle bundle = enrolleeFactory.buildWithPortalUser(getTestName(info));

        Profile profile = bundle.enrollee().getProfile();

        String newStreet1 = RandomStringUtils.randomAlphabetic(20);
        String newCity = RandomStringUtils.randomAlphabetic(20);

        MailingAddress newMailingAddress = MailingAddress.builder().id(profile.getMailingAddressId()).street1(newStreet1).city(newCity).build();

        profile.setMailingAddress(newMailingAddress);
        profileService.updateWithMailingAddress(profile, DataAuditInfo
                .builder()
                .portalParticipantUserId(bundle.portalParticipantUser().getId())
                .responsibleUserId(bundle.portalParticipantUser().getParticipantUserId())
                .enrolleeId(bundle.enrollee().getId())
                .build());


        Profile updatedProfile = profileService.loadWithMailingAddress(profile.getId()).orElseThrow();

        Assertions.assertEquals(newStreet1, updatedProfile.getMailingAddress().getStreet1());
        Assertions.assertEquals(newCity, updatedProfile.getMailingAddress().getCity());
    }

    @Test
    @Transactional
    public void testGovernedUserProfile(TestInfo testInfo){
        HubResponse hubResponse = enrolleeFactory.buildProxyAndGovernedEnrollee(getTestName(testInfo), getTestName(testInfo));
        Enrollee enrollee = hubResponse.getEnrollee();
        Profile governedUserProfile = profileService.find(enrollee.getProfileId()).get();
        List<EnrolleeRelation> relations = enrolleeRelationService.findByEnrolleeIdAndRelationType(enrollee.getId(), RelationshipType.PROXY);
        Enrollee proxyEnrollee = enrolleeService.find(relations.get(0).getEnrolleeId()).get();
        PortalParticipantUser proxyPpUser = portalParticipantUserService.findByParticipantUserId(proxyEnrollee.getParticipantUserId()).get(0);
        Profile proxyProfile = profileService.find(proxyPpUser.getProfileId()).orElseThrow();
        Assert.assertTrue(StringUtils.isNoneEmpty(governedUserProfile.getContactEmail()));
        Assert.assertEquals(proxyProfile.getContactEmail(), governedUserProfile.getContactEmail());
    }
}
