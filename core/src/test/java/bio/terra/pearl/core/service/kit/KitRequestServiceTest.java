package bio.terra.pearl.core.service.kit;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.dao.kit.KitRequestDao;
import bio.terra.pearl.core.dao.study.StudyDao;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.admin.AdminUserFactory;
import bio.terra.pearl.core.factory.kit.KitRequestFactory;
import bio.terra.pearl.core.factory.kit.KitTypeFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.kit.KitRequestStatus;
import bio.terra.pearl.core.service.participant.ProfileService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class KitRequestServiceTest extends BaseSpringBootTest {

    @Transactional
    @Test
    public void testRequestKit() throws Exception {
        // Arrange
        var adminUser = adminUserFactory.buildPersisted("testRequestKit");
        var kitType = kitTypeFactory.buildPersisted("testRequestKit");
        var enrolleeBundle = enrolleeFactory.buildWithPortalUser("testRequestKit");
        var enrollee = enrolleeBundle.enrollee();
        var profile = enrollee.getProfile();
        profile.setGivenName("Alex");
        profile.setFamilyName("Tester");
        profile.setPhoneNumber("111-222-3333");
        profile.getMailingAddress().setStreet1("123 Fake Street");
        profileService.updateWithMailingAddress(profile);
        var expectedSentToAddress = PepperKitAddress.builder()
                .firstName("Alex")
                .lastName("Tester")
                .street1("123 Fake Street")
                .phoneNumber("111-222-3333")
                .build();
        var studyName = "testStudy";
        when(mockPepperDSMClient.sendKitRequest(any(), any(), any(), any()))
                .thenReturn("{ \"kits\": [{}] }");

        // Act
        var sampleKit = kitRequestService.requestKit(adminUser, studyName, enrollee, "testRequestKit");

        // Assert
        Mockito.verify(mockPepperDSMClient)
                .sendKitRequest(eq(studyName), eq(enrollee), any(KitRequest.class), any(PepperKitAddress.class));
        assertThat(sampleKit.getCreatingAdminUserId(), equalTo(adminUser.getId()));
        assertThat(sampleKit.getEnrolleeId(), equalTo(enrollee.getId()));
        assertThat(objectMapper.readValue(sampleKit.getSentToAddress(), PepperKitAddress.class),
                equalTo(expectedSentToAddress));
        assertThat(sampleKit.getStatus(), equalTo(KitRequestStatus.CREATED));
    }

    @Transactional
    @Test
    public void testRequestKitError() throws Exception {
        // Arrange
        var adminUser = adminUserFactory.buildPersisted("testRequestKit");
        var kitType = kitTypeFactory.buildPersisted("testRequestKit");
        var enrolleeBundle = enrolleeFactory.buildWithPortalUser("testRequestKit");
        var enrollee = enrolleeBundle.enrollee();
        var profile = enrollee.getProfile();
        profile.setGivenName("Alex");
        profile.setFamilyName("Tester");
        profile.setPhoneNumber("111-222-3333");
        profile.getMailingAddress().setStreet1("123 Fake Street");
        profileService.updateWithMailingAddress(profile);

        when(mockPepperDSMClient.sendKitRequest(any(), any(), any(), any())).thenAnswer(invocation -> {
            var kitRequest = (KitRequest) invocation.getArguments()[2];
            throw new PepperException("boom",
                    PepperErrorResponse.builder()
                            .juniperKitId(kitRequest.getId().toString())
                            .build());
        });

        // "Act"
        Executable act = () -> kitRequestService.requestKit(adminUser, "testStudy" , enrollee, kitType.getName());

        // Assert
        PepperException pepperException = assertThrows(PepperException.class, act);
    }

    @Transactional
    @Test
    public void testUpdateKitStatus() throws Exception {
        // Arrange
        var adminUser = adminUserFactory.buildPersisted("testUpdateKitStatus");
        var enrollee = enrolleeFactory.buildPersisted("testUpdateKitStatus");
        var kitType = kitTypeFactory.buildPersisted("testUpdateKitStatus");
        var kitRequest = kitRequestFactory.buildPersisted("testUpdateKitStatus",
            enrollee.getId(), kitType.getId(), adminUser.getId());

        var response = PepperKitStatus.builder()
                .juniperKitId(kitRequest.getId().toString())
                .currentStatus("SENT")
                .build();
        when(mockPepperDSMClient.fetchKitStatus(kitRequest.getId())).thenReturn(response);

        // Act
        var sampleKitStatus = kitRequestService.syncKitStatusFromPepper(kitRequest.getId());

        // Assert
        assertThat(sampleKitStatus.getCurrentStatus(), equalTo("SENT"));
    }

    @Transactional
    @Test
    public void testGetSampleKitsForStudyEnvironment() throws Exception {
        // Arrange:
        //   2 kits, one with bogus status JSON
        var adminUser = adminUserFactory.buildPersisted("testGetSampleKitsForStudyEnvironment");
        var studyEnvironment = studyEnvironmentFactory.buildPersisted("testGetSampleKitsForStudyEnvironment");
        var enrollee = enrolleeFactory.buildPersisted("testGetSampleKitsForStudyEnvironment", studyEnvironment);
        var kitType = kitTypeFactory.buildPersisted("testGetSampleKitsForStudyEnvironment");
        var kitRequest1 = kitRequestFactory.buildPersisted("testGetSampleKitsForStudyEnvironment",
                enrollee.getId(), kitType.getId(), adminUser.getId());
        var kitRequest2 = kitRequestDao.create(kitRequestFactory.builder("testGetSampleKitsForStudyEnvironment")
                .creatingAdminUserId(adminUser.getId())
                .enrolleeId(enrollee.getId())
                .kitTypeId(kitType.getId())
                .dsmStatus("BOOM!")
                .build());

        // Act
        var kits = kitRequestService.getSampleKitsByStudyEnvironment(studyEnvironment);

        // Assert
        assertThat(kits, contains(kitRequest1, kitRequest2));
    }

    @Transactional
    @Test
    public void testSyncAllKitStatusesFromPepper() throws Exception {
        /*
         * Arrange:
         *  - an admin user
         *  - a study environment
         *  - a kit type
         *  - two enrollees, 1a and 1b, in the study environment, each with a sample kit
         *  - another enrollee in another study, also with a sample kit
         */
        var adminUser = adminUserFactory.buildPersisted("testSyncAllKitStatusesFromPepper");
        var studyEnvironment = studyEnvironmentFactory.buildPersisted("testSyncAllKitStatusesFromPepper");
        var study = studyDao.find(studyEnvironment.getStudyId()).get();
        var kitType = kitTypeFactory.buildPersisted("testSyncAllKitStatusesFromPepper");
        var enrollee1a = enrolleeFactory.buildPersisted("testSyncAllKitStatusesFromPepper", studyEnvironment);
        var enrollee1b = enrolleeFactory.buildPersisted("testSyncAllKitStatusesFromPepper", studyEnvironment);
        var kitRequest1a = kitRequestFactory.buildPersisted("testSyncAllKitStatusesFromPepper",
            enrollee1a.getId(), kitType.getId(), adminUser.getId());
        var kitRequest1b = kitRequestFactory.buildPersisted("testSyncAllKitStatusesFromPepper",
            enrollee1b.getId(), kitType.getId(), adminUser.getId());
        var studyEnvironment2 = studyEnvironmentFactory.buildPersisted("testSyncAllKitStatusesFromPepper2");
        var study2 = studyDao.find(studyEnvironment2.getStudyId()).get();
        var enrollee2 = enrolleeFactory.buildPersisted("testSyncAllKitStatusesFromPepper", studyEnvironment2);
        var kitRequest2 = kitRequestFactory.buildPersisted("testSyncAllKitStatusesFromPepper",
            enrollee2.getId(), kitType.getId(), adminUser.getId());

        /*
         * Mock DSM to return kits by study:
         *  - the first study has two kits, one in flight and one complete
         *  - the second study has one kit with an error
         */
        var kitStatus1a = PepperKitStatus.builder()
                .juniperKitId(kitRequest1a.getId().toString())
                .currentStatus(PepperKitStatus.Status.SENT.currentStatus)
                .build();
        var kitStatus1b = PepperKitStatus.builder()
                .juniperKitId(kitRequest1b.getId().toString())
                .currentStatus(PepperKitStatus.Status.RECEIVED.currentStatus)
                .build();
        var kitStatus2 = PepperKitStatus.builder()
                .juniperKitId(kitRequest2.getId().toString())
                .currentStatus(PepperKitStatus.Status.ERRORED.currentStatus)
                .errorMessage("Something went wrong")
                .errorDate(DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.systemDefault()).format(Instant.now()))
                .build();
        when(mockPepperDSMClient.fetchKitStatusByStudy(study.getShortcode()))
                .thenReturn(List.of(kitStatus1a, kitStatus1b));
        when(mockPepperDSMClient.fetchKitStatusByStudy(study2.getShortcode()))
                .thenReturn(List.of(kitStatus2));

        /* Finally, exercise the unit under test! */
        kitRequestService.syncAllKitStatusesFromPepper();

        /* Load and verify each kit */
        verifyKit(kitRequest1a, kitStatus1a, KitRequestStatus.IN_PROGRESS);
        verifyKit(kitRequest1b, kitStatus1b, KitRequestStatus.COMPLETE);
        verifyKit(kitRequest2, kitStatus2, KitRequestStatus.FAILED);
    }

    private void verifyKit(KitRequest kit, PepperKitStatus expectedDSMStatus, KitRequestStatus expectedStatus)
            throws JsonProcessingException {
        var savedKit = kitRequestDao.find(kit.getId()).get();
        assertThat(savedKit.getStatus(), equalTo(expectedStatus));
        assertThat(objectMapper.readValue(savedKit.getDsmStatus(), PepperKitStatus.class),
                equalTo(expectedDSMStatus));
    }


    @MockBean
    private PepperDSMClient mockPepperDSMClient;

    @Autowired
    private AdminUserFactory adminUserFactory;
    @Autowired
    private EnrolleeFactory enrolleeFactory;
    @Autowired
    private KitRequestDao kitRequestDao;
    @Autowired
    private KitRequestFactory kitRequestFactory;
    @Autowired
    private KitRequestService kitRequestService;
    @Autowired
    private KitTypeFactory kitTypeFactory;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ProfileService profileService;
    @Autowired
    private StudyDao studyDao;
    @Autowired
    private StudyEnvironmentFactory studyEnvironmentFactory;
}
