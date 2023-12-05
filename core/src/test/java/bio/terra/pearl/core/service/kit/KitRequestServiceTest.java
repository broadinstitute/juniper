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
import bio.terra.pearl.core.service.kit.pepper.*;
import bio.terra.pearl.core.service.participant.ProfileService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
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
    public void testRequestKitAssemble(TestInfo testInfo) throws Exception {
        var adminUser = adminUserFactory.buildPersisted(getTestName(testInfo));
        var kitType = kitTypeFactory.buildPersisted(getTestName(testInfo));
        var enrolleeBundle = enrolleeFactory.buildWithPortalUser(getTestName(testInfo));
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

        var sampleKit = kitRequestService.assemble(adminUser, enrollee, expectedSentToAddress, "SALIVA");

        assertThat(sampleKit.getCreatingAdminUserId(), equalTo(adminUser.getId()));
        assertThat(sampleKit.getEnrolleeId(), equalTo(enrollee.getId()));
        assertThat(objectMapper.readValue(sampleKit.getSentToAddress(), PepperKitAddress.class),
                equalTo(expectedSentToAddress));
        assertThat(sampleKit.getStatus(), equalTo(KitRequestStatus.CREATED));
    }

    @Transactional
    @Test
    public void testRequestKitError(TestInfo testInfo) {
        var adminUser = adminUserFactory.buildPersisted(getTestName(testInfo));
        var kitType = kitTypeFactory.buildPersisted(getTestName(testInfo));
        var enrolleeBundle = enrolleeFactory.buildWithPortalUser(getTestName(testInfo));
        var enrollee = enrolleeBundle.enrollee();
        var profile = enrollee.getProfile();
        profile.setGivenName("Alex");
        profile.setFamilyName("Tester");
        profile.setPhoneNumber("111-222-3333");
        profile.getMailingAddress().setStreet1("123 Fake Street");
        profileService.updateWithMailingAddress(profile);

        when(mockPepperDSMClient.sendKitRequest(any(), any(), any(), any())).thenAnswer(invocation -> {
            var kitRequest = (KitRequest) invocation.getArguments()[2];
            throw new PepperApiException("Error from Pepper with unexpected format: boom",
                    PepperErrorResponse.builder()
                            .juniperKitId(kitRequest.getId().toString())
                            .build(), HttpStatus.BAD_REQUEST);
        });


        assertThrows(PepperApiException.class, () ->
                kitRequestService.requestKit(adminUser, "testStudy" , enrollee, kitType.getName())
        );
    }

    @Transactional
    @Test
    public void testUpdateKitStatus(TestInfo testInfo) throws Exception {
        // Arrange
        var adminUser = adminUserFactory.buildPersisted(getTestName(testInfo));
        var enrollee = enrolleeFactory.buildPersisted(getTestName(testInfo));
        var kitType = kitTypeFactory.buildPersisted(getTestName(testInfo));
        var kitRequest = kitRequestFactory.buildPersisted(getTestName(testInfo),
            enrollee.getId(), kitType.getId(), adminUser.getId());

        var response = PepperKit.builder()
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
    public void testGetSampleKitsForStudyEnvironment(TestInfo testInfo) throws Exception {
        // Arrange:
        //   2 kits, one with bogus status JSON
        String testName = getTestName(testInfo);
        var adminUser = adminUserFactory.buildPersisted(testName);
        var studyEnvironment = studyEnvironmentFactory.buildPersisted(testName);
        var enrollee = enrolleeFactory.buildPersisted(testName, studyEnvironment);
        var kitType = kitTypeFactory.buildPersisted(testName);
        var kitRequest1 = kitRequestFactory.buildPersisted(testName,
                enrollee.getId(), kitType.getId(), adminUser.getId());
        var kitRequest2 = kitRequestDao.create(kitRequestFactory.builder(testName)
                .creatingAdminUserId(adminUser.getId())
                .enrolleeId(enrollee.getId())
                .kitTypeId(kitType.getId())
                .externalKit("BOOM!")
                .build());

        // Act
        var kits = kitRequestService.getSampleKitsByStudyEnvironment(studyEnvironment);

        // Assert
        assertThat(kits, contains(kitRequest1, kitRequest2));
    }

    @Transactional
    @Test
    public void testSyncAllKitStatusesFromPepper(TestInfo testInfo) throws Exception {
        /*
         * Arrange:
         *  - an admin user
         *  - a study environment
         *  - a kit type
         *  - two enrollees, 1a and 1b, in the study environment, each with a sample kit
         *  - another enrollee in another study, also with a sample kit
         */

        String testName = getTestName(testInfo);
        var kitType = kitTypeFactory.buildPersisted(testName);
        var adminUser = adminUserFactory.buildPersisted(testName);
        var studyEnvironment = studyEnvironmentFactory.buildPersisted(testName);
        kitTypeFactory.attachTypeToEnvironment(kitType.getId(), studyEnvironment.getId());
        var study = studyDao.find(studyEnvironment.getStudyId()).get();
        var enrollee1a = enrolleeFactory.buildPersisted(testName, studyEnvironment);
        var enrollee1b = enrolleeFactory.buildPersisted(testName, studyEnvironment);
        var kitRequest1a = kitRequestFactory.buildPersisted(testName,
            enrollee1a.getId(), kitType.getId(), adminUser.getId());
        var kitRequest1b = kitRequestFactory.buildPersisted(testName,
            enrollee1b.getId(), kitType.getId(), adminUser.getId());

        var studyEnvironment2 = studyEnvironmentFactory.buildPersisted(testName);
        kitTypeFactory.attachTypeToEnvironment(kitType.getId(), studyEnvironment2.getId());
        var study2 = studyDao.find(studyEnvironment2.getStudyId()).get();
        var enrollee2 = enrolleeFactory.buildPersisted(testName, studyEnvironment2);
        var kitRequest2 = kitRequestFactory.buildPersisted(testName,
            enrollee2.getId(), kitType.getId(), adminUser.getId());

        /*
         * Mock DSM to return kits by study:
         *  - the first study has two kits, one in flight and one complete
         *  - the second study has one kit with an error
         */
        var kitStatus1a = PepperKit.builder()
                .juniperKitId(kitRequest1a.getId().toString())
                .currentStatus(PepperKitStatus.SENT.pepperString)
                .build();
        var kitStatus1b = PepperKit.builder()
                .juniperKitId(kitRequest1b.getId().toString())
                .currentStatus(PepperKitStatus.RECEIVED.pepperString)
                .build();
        var kitStatus2 = PepperKit.builder()
                .juniperKitId(kitRequest2.getId().toString())
                .currentStatus(PepperKitStatus.ERRORED.pepperString)
                .errorMessage("Something went wrong")
                .errorDate(DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.systemDefault()).format(Instant.now()))
                .build();
        when(mockPepperDSMClient.fetchKitStatusByStudy(study.getShortcode()))
                .thenReturn(List.of(kitStatus1a, kitStatus1b));
        when(mockPepperDSMClient.fetchKitStatusByStudy(study2.getShortcode()))
                .thenReturn(List.of(kitStatus2));

        /* Finally, sync the kit statuses  */
        kitRequestService.syncAllKitStatusesFromPepper();

        /* Load and verify each kit */
        verifyKit(kitRequest1a, kitStatus1a, KitRequestStatus.SENT);
        verifyKit(kitRequest1b, kitStatus1b, KitRequestStatus.RECEIVED);
        verifyKit(kitRequest2, kitStatus2, KitRequestStatus.ERRORED);
    }

    @Transactional
    @Test
    public void testSyncAllKitStatusesSkipsWithNoKitTypes(TestInfo testInfo) {
        String testName = getTestName(testInfo);
        var adminUser = adminUserFactory.buildPersisted(testName);
        var studyEnvironment = studyEnvironmentFactory.buildPersisted(testName);

        kitRequestService.syncAllKitStatusesFromPepper();

        /**
         * note that because we're testing that no kits attempt to be synced, this test is sensitive to studies
         * leaking from other tests.  If this test fails, check that other tests are cleaning up after themselves.
         */
        Mockito.verifyNoInteractions(mockPepperDSMClient);
    }

    private void verifyKit(KitRequest kit, PepperKit expectedDSMStatus, KitRequestStatus expectedStatus)
            throws JsonProcessingException {
        var savedKit = kitRequestDao.find(kit.getId()).get();
        assertThat(savedKit.getStatus(), equalTo(expectedStatus));
        assertThat(objectMapper.readValue(savedKit.getExternalKit(), PepperKit.class),
                equalTo(expectedDSMStatus));
    }


    @MockBean
    private PepperDSMClient mockPepperDSMClient;
    @Autowired
    private StubPepperDSMClient stubPepperDSMClient;

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
