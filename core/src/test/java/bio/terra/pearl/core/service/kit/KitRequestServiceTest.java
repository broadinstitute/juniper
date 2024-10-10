package bio.terra.pearl.core.service.kit;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.dao.kit.KitRequestDao;
import bio.terra.pearl.core.dao.study.StudyDao;
import bio.terra.pearl.core.factory.StudyEnvironmentBundle;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.admin.AdminUserFactory;
import bio.terra.pearl.core.factory.kit.KitRequestFactory;
import bio.terra.pearl.core.factory.kit.KitTypeFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeBundle;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.kit.DistributionMethod;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.kit.KitRequestStatus;
import bio.terra.pearl.core.model.kit.KitType;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import bio.terra.pearl.core.service.kit.pepper.*;
import bio.terra.pearl.core.service.participant.ProfileService;
import bio.terra.pearl.core.service.study.StudyEnvironmentConfigService;
import bio.terra.pearl.core.service.workflow.EventService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Assertions;
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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class KitRequestServiceTest extends BaseSpringBootTest {

    @Transactional
    @Test
    public void testRequestKitAssemble(TestInfo testInfo) throws Exception {
        AdminUser adminUser = adminUserFactory.buildPersisted(getTestName(testInfo));
        EnrolleeBundle enrolleeBundle = enrolleeFactory.buildWithPortalUser(getTestName(testInfo));
        Enrollee enrollee = enrolleeBundle.enrollee();
        Profile profile = enrollee.getProfile();
        profile.setGivenName("Alex");
        profile.setFamilyName("Tester");
        profile.setPhoneNumber("111-222-3333");
        profile.getMailingAddress().setStreet1("123 Fake Street");
        profileService.updateWithMailingAddress(profile, DataAuditInfo.builder().build());
        PepperKitAddress expectedSentToAddress = PepperKitAddress.builder()
                .firstName("Alex")
                .lastName("Tester")
                .street1("123 Fake Street")
                .phoneNumber("111-222-3333")
                .build();

        KitRequest sampleKit = kitRequestService.assemble(adminUser, enrollee, expectedSentToAddress, new KitRequestService.KitRequestCreationDto("SALIVA", DistributionMethod.MAILED, null, false));

        assertThat(sampleKit.getCreatingAdminUserId(), equalTo(adminUser.getId()));
        assertThat(sampleKit.getEnrolleeId(), equalTo(enrollee.getId()));
        assertThat(objectMapper.readValue(sampleKit.getSentToAddress(), PepperKitAddress.class),
                equalTo(expectedSentToAddress));
        assertThat(sampleKit.getStatus(), equalTo(KitRequestStatus.CREATED));
    }

    @Transactional
    @Test
    public void testRequestKitError(TestInfo testInfo) {
        AdminUser adminUser = adminUserFactory.buildPersisted(getTestName(testInfo));
        KitType kitType = kitTypeFactory.buildPersisted(getTestName(testInfo));
        EnrolleeBundle enrolleeBundle = enrolleeFactory.buildWithPortalUser(getTestName(testInfo));
        Enrollee enrollee = enrolleeBundle.enrollee();
        Profile profile = enrollee.getProfile();
        profile.setGivenName("Alex");
        profile.setFamilyName("Tester");
        profile.setPhoneNumber("111-222-3333");
        profile.getMailingAddress().setStreet1("123 Fake Street");
        profileService.updateWithMailingAddress(profile, DataAuditInfo.builder().build());

        when(mockPepperDSMClient.sendKitRequest(any(), any(), any(), any(), any())).thenAnswer(invocation -> {
            KitRequest kitRequest = (KitRequest) invocation.getArguments()[3];
            throw new PepperApiException("Error from Pepper with unexpected format: boom",
                    PepperErrorResponse.builder()
                            .juniperKitId(kitRequest.getId().toString())
                            .build(), HttpStatus.BAD_REQUEST);
        });

        assertThrows(PepperApiException.class, () ->
                kitRequestService.requestKit(adminUser, "testStudy" , enrollee, new KitRequestService.KitRequestCreationDto(kitType.getName(), DistributionMethod.MAILED, null, false))
        );
    }

    @Transactional
    @Test
    public void testRequestKitMailed(TestInfo testInfo) throws JsonProcessingException {
        AdminUser adminUser = adminUserFactory.buildPersisted(getTestName(testInfo));
        KitType kitType = kitTypeFactory.buildPersisted(getTestName(testInfo));
        EnrolleeBundle enrolleeBundle = enrolleeFactory.buildWithPortalUser(getTestName(testInfo));
        Enrollee enrollee = enrolleeBundle.enrollee();
        Profile profile = enrollee.getProfile();
        profile.setGivenName("Alex");
        profile.setFamilyName("Tester");
        profile.setPhoneNumber("111-222-3333");
        profile.getMailingAddress().setStreet1("123 Fake Street");
        profileService.updateWithMailingAddress(profile, DataAuditInfo.builder().build());
        PepperKitAddress expectedSentToAddress = PepperKitAddress.builder()
                .firstName("Alex")
                .lastName("Tester")
                .street1("123 Fake Street")
                .phoneNumber("111-222-3333")
                .build();

        KitRequestDto sampleKit = kitRequestService.requestKit(adminUser, "testStudy", enrollee, new KitRequestService.KitRequestCreationDto(kitType.getName(), DistributionMethod.MAILED, null, false));
        KitRequest savedKit = kitRequestDao.find(sampleKit.getId()).get();

        assertThat(savedKit.getCreatingAdminUserId(), equalTo(adminUser.getId()));
        assertThat(savedKit.getStatus(), equalTo(KitRequestStatus.CREATED));
        assertThat(savedKit.getSentToAddress(), equalTo(objectMapper.writeValueAsString(expectedSentToAddress)));
        assertThat(savedKit.getDistributionMethod(), equalTo(DistributionMethod.MAILED));

        verify(mockPepperDSMClient).sendKitRequest(any(), any(), any(), any(), any());
    }

    @Transactional
    @Test
    public void testRequestKitInPerson(TestInfo testInfo) {
        AdminUser adminUser = adminUserFactory.buildPersisted(getTestName(testInfo));
        KitType kitType = kitTypeFactory.buildPersisted(getTestName(testInfo));
        EnrolleeBundle enrolleeBundle = enrolleeFactory.buildWithPortalUser(getTestName(testInfo));
        Enrollee enrollee = enrolleeBundle.enrollee();
        Profile profile = enrollee.getProfile();
        profile.setGivenName("Alex");
        profile.setFamilyName("Tester");
        profile.setPhoneNumber("111-222-3333");
        profile.getMailingAddress().setStreet1("123 Fake Street");
        profileService.updateWithMailingAddress(profile, DataAuditInfo.builder().build());

        KitRequestDto sampleKit = kitRequestService.requestKit(adminUser, "testStudy", enrollee, new KitRequestService.KitRequestCreationDto(kitType.getName(), DistributionMethod.IN_PERSON, null, false));
        KitRequest savedKit = kitRequestDao.find(sampleKit.getId()).get();

        assertThat(savedKit.getCreatingAdminUserId(), equalTo(adminUser.getId()));
        assertThat(savedKit.getCollectingAdminUserId(), equalTo(null));
        assertThat(savedKit.getSentToAddress(), equalTo(null));
        assertThat(savedKit.getStatus(), equalTo(KitRequestStatus.CREATED));
        assertThat(savedKit.getDistributionMethod(), equalTo(DistributionMethod.IN_PERSON));

        // Requesting an IN_PERSON kit does not make any requests to DSM
        Mockito.verifyNoInteractions(mockPepperDSMClient);
    }

    @Transactional
    @Test
    void testUpdateKitStatus(TestInfo testInfo) throws Exception {
        String testName = getTestName(testInfo);
        EnrolleeBundle enrolleeBundle = enrolleeFactory.buildWithPortalUser(testName);
        AdminUser adminUser = adminUserFactory.buildPersisted(testName);
        Enrollee enrollee = enrolleeBundle.enrollee();
        KitType kitType = kitTypeFactory.buildPersisted(testName);
        KitRequest kitRequest = kitRequestFactory.buildPersisted(testName,
                enrollee, PepperKitStatus.CREATED, kitType.getId(), adminUser.getId());

        String sentDate = "2023-11-17T14:57:59.548Z";
        String errorMessage = "Something went wrong";
        PepperKit pepperKit = PepperKit.builder()
                .juniperKitId(kitRequest.getId().toString())
                .currentStatus(PepperKitStatus.SENT.pepperString)
                .scanDate(sentDate)
                .errorMessage(errorMessage)
                .build();
        when(mockPepperDSMClient.fetchKitStatus(any(), eq(kitRequest.getId()))).thenReturn(pepperKit);
        when(mockEventService.publishKitStatusEvent(any(KitRequest.class),
                any(Enrollee.class), any(PortalParticipantUser.class), any(KitRequestStatus.class))).thenReturn(null);

        kitRequestService.syncKitStatusFromPepper(kitRequest.getId());

        KitRequest savedKit = kitRequestDao.find(kitRequest.getId()).get();
        assertThat(savedKit.getStatus(), equalTo(KitRequestStatus.SENT));
        assertThat(savedKit.getErrorMessage(), equalTo(errorMessage));
        assertThat(savedKit.getSentAt(), equalTo(Instant.parse(sentDate)));
    }

    @Transactional
    @Test
    public void testGetKitsByStudyEnvironment(TestInfo testInfo) throws Exception {
        // Arrange:
        //   2 kits, one with bogus status JSON
        String testName = getTestName(testInfo);
        AdminUser adminUser = adminUserFactory.buildPersisted(testName);
        StudyEnvironment studyEnvironment = studyEnvironmentFactory.buildPersisted(testName);
        Enrollee enrollee = enrolleeFactory.buildPersisted(testName, studyEnvironment);
        KitType kitType = kitTypeFactory.buildPersisted(testName);
        KitRequest kitRequest1 = kitRequestFactory.buildPersisted(testName,
                enrollee, PepperKitStatus.CREATED, kitType.getId(), adminUser.getId());
        KitRequest kitRequest2 = kitRequestDao.create(
                kitRequestFactory.builder(testName)
                .creatingAdminUserId(adminUser.getId())
                .enrolleeId(enrollee.getId())
                .kitTypeId(kitType.getId())
                .externalKit("BOOM!")
                .build());

        // Act
        Collection<KitRequestDto> kits = kitRequestService.getKitsByStudyEnvironment(studyEnvironment);

        PepperKit pepperKit = objectMapper.readValue(kitRequest1.getExternalKit(), PepperKit.class);
        // note: this works because the order of insertion in KitRequestDetails is deterministic
        ObjectNode detailsJson = objectMapper.createObjectNode();
        detailsJson.put("requestId", kitRequest1.getId().toString());
        detailsJson.put("shippingId", pepperKit.getDsmShippingLabel());

        for (KitRequestDto kit : kits) {
            if (kit.getId().equals(kitRequest1.getId())) {
                assertThat(kit.getStatus(), equalTo(KitRequestStatus.CREATED));
                assertThat(kit.getEnrolleeShortcode(), equalTo(enrollee.getShortcode()));
                assertThat(kit.getKitType().getName(), equalTo(kitType.getName()));
                assertThat(kit.getDetails(), equalTo(objectMapper.writeValueAsString(detailsJson)));
            } else if (kit.getId().equals(kitRequest2.getId())) {
                assertThat(kit.getStatus(), equalTo(KitRequestStatus.CREATED));
                assertThat(kit.getKitType().getName(), equalTo(kitType.getName()));
                assertThat(kit.getEnrolleeShortcode(), equalTo(enrollee.getShortcode()));
                assertThat(kit.getDetails(), nullValue());
            } else {
                Assertions.fail("Unexpected kit ID: " + kit.getId());
            }
        }
    }

    @Transactional
    @Test
    public void testCantCollectMailedKit(TestInfo testInfo) throws JsonProcessingException {
        String testName = getTestName(testInfo);
        AdminUser adminUser = adminUserFactory.buildPersisted(getTestName(testInfo));
        EnrolleeBundle enrolleeBundle = enrolleeFactory.buildWithPortalUser(getTestName(testInfo));
        Enrollee enrollee = enrolleeBundle.enrollee();
        KitType kitType = kitTypeFactory.buildPersisted(testName);
        KitRequest kitRequest = kitRequestFactory.buildPersisted(testName,
                enrollee, PepperKitStatus.CREATED, kitType.getId(), adminUser.getId());

        assertThrows(IllegalArgumentException.class, () -> {
            kitRequestService.collectKit(adminUser, "somestudy", kitRequest);
        });
    }

    @Transactional
    @Test
    public void testAssignKit(TestInfo testInfo) throws JsonProcessingException {
        String testName = getTestName(testInfo);
        AdminUser adminUser = adminUserFactory.buildPersisted(getTestName(testInfo));
        EnrolleeBundle enrolleeBundle = enrolleeFactory.buildWithPortalUser(getTestName(testInfo));
        Enrollee enrollee = enrolleeBundle.enrollee();
        KitType kitType = kitTypeFactory.buildPersisted(testName);
        KitRequest kitRequest = kitRequestDao.create(
                kitRequestFactory.builder(testName)
                        .creatingAdminUserId(adminUser.getId())
                        .enrolleeId(enrollee.getId())
                        .kitTypeId(kitType.getId())
                        .distributionMethod(DistributionMethod.IN_PERSON)
                        .status(KitRequestStatus.CREATED)
                        .creatingAdminUserId(adminUser.getId())
                        .build());

        assertThat(kitRequest.getStatus(), equalTo(KitRequestStatus.CREATED));
        assertThat(kitRequest.getCreatingAdminUserId(), equalTo(adminUser.getId()));
        assertThat(kitRequest.getCollectingAdminUserId(), equalTo(null));
    }

    @Transactional
    @Test
    public void testCollectInPersonKit(TestInfo testInfo) throws JsonProcessingException {
        String testName = getTestName(testInfo);
        AdminUser adminUser = adminUserFactory.buildPersisted(getTestName(testInfo));
        EnrolleeBundle enrolleeBundle = enrolleeFactory.buildWithPortalUser(getTestName(testInfo));
        Enrollee enrollee = enrolleeBundle.enrollee();
        KitType kitType = kitTypeFactory.buildPersisted(testName);
        KitRequest kitRequest = kitRequestDao.create(
                kitRequestFactory.builder(testName)
                        .creatingAdminUserId(adminUser.getId())
                        .enrolleeId(enrollee.getId())
                        .kitTypeId(kitType.getId())
                        .distributionMethod(DistributionMethod.IN_PERSON)
                        .status(KitRequestStatus.CREATED)
                        .creatingAdminUserId(adminUser.getId())
                        .build());

        kitRequestService.collectKit(adminUser, "somestudy", kitRequest);

        KitRequest collectedKit = kitRequestDao.find(kitRequest.getId()).get();

        assertThat(collectedKit.getStatus(), equalTo(KitRequestStatus.COLLECTED_BY_STAFF));
        assertThat(collectedKit.getCreatingAdminUserId(), equalTo(adminUser.getId()));
        assertThat(collectedKit.getCollectingAdminUserId(), equalTo(adminUser.getId()));

        verify(mockPepperDSMClient).sendKitRequest(any(), any(), any(), any(), any());
    }

    @Transactional
    @Test
    void testFindByEnrollees(TestInfo testInfo) throws Exception {
        String testName = getTestName(testInfo);
        AdminUser adminUser = adminUserFactory.buildPersisted(testName);
        StudyEnvironment studyEnvironment = studyEnvironmentFactory.buildPersisted(testName);
        KitType kitType = kitTypeFactory.buildPersisted(testName);

        PepperKit pepperKit1 = PepperKit.builder()
                .dsmShippingLabel(UUID.randomUUID().toString())
                .currentStatus(PepperKitStatus.CREATED.pepperString)
                .build();

        Enrollee enrollee1 = enrolleeFactory.buildPersisted(testName, studyEnvironment);
        KitRequest kitRequest1 = kitRequestFactory.buildPersisted(testName,
                enrollee1, pepperKit1, kitType.getId(), adminUser.getId());

        String errorMessage = "Something went wrong";
        String deactivationReason = "Withdrawn from study";
        String deactivationDate =
                DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.systemDefault()).format(Instant.now());
        PepperKit pepperKit2 = PepperKit.builder()
                .dsmShippingLabel(UUID.randomUUID().toString())
                .currentStatus(PepperKitStatus.DEACTIVATED.pepperString)
                .errorMessage(errorMessage)
                .deactivationReason(deactivationReason)
                .deactivationDate(deactivationDate)
                .build();

        Enrollee enrollee2 = enrolleeFactory.buildPersisted(testName, studyEnvironment);
        KitRequest kitRequest2 = kitRequestFactory.buildPersisted(testName,
                enrollee2, pepperKit2, kitType.getId(), adminUser.getId());

        Map<UUID, List<KitRequestDto>> kits = kitRequestService.findByEnrollees(List.of(enrollee1, enrollee2));

        for (Map.Entry<UUID, List<KitRequestDto>> entry : kits.entrySet()) {
            if (enrollee1.getId().equals(entry.getKey())) {
                KitRequestDto kit = entry.getValue().get(0);
                assertThat(kit.getId(), equalTo(kitRequest1.getId()));
                assertThat(kit.getStatus(), equalTo(KitRequestStatus.CREATED));
                assertThat(kit.getEnrolleeShortcode(), equalTo(enrollee1.getShortcode()));
                assertThat(kit.getKitType().getName(), equalTo(kitType.getName()));

                // note: this works because the order of insertion in KitRequestDetails is deterministic
                ObjectNode detailsJson = objectMapper.createObjectNode();
                detailsJson.put("requestId", kitRequest1.getId().toString());
                detailsJson.put("shippingId", pepperKit1.getDsmShippingLabel());
                assertThat(kit.getDetails(), equalTo(objectMapper.writeValueAsString(detailsJson)));
            } else if (enrollee2.getId().equals(entry.getKey())) {
                KitRequestDto kit = entry.getValue().get(0);
                assertThat(kit.getId(), equalTo(kitRequest2.getId()));
                assertThat(kit.getStatus(), equalTo(KitRequestStatus.DEACTIVATED));
                assertThat(kit.getKitType().getName(), equalTo(kitType.getName()));
                assertThat(kit.getEnrolleeShortcode(), equalTo(enrollee2.getShortcode()));
                assertThat(kit.getErrorMessage(), equalTo(errorMessage));

                // note: this works because the order of insertion in KitRequestDetails is deterministic
                ObjectNode detailsJson = objectMapper.createObjectNode();
                detailsJson.put("requestId", kitRequest2.getId().toString());
                detailsJson.put("shippingId", pepperKit2.getDsmShippingLabel());
                detailsJson.put("deactivationReason", deactivationReason);
                detailsJson.put("deactivationDate", deactivationDate);
                assertThat(kit.getDetails(), equalTo(objectMapper.writeValueAsString(detailsJson)));
            } else {
                Assertions.fail("Unexpected enrollee ID: " + entry.getKey());
            }
        }

        List<KitRequestDto> kits2 = kitRequestService.findByEnrollee(enrollee2);
        assertThat(kits2.size(), equalTo(1));
        KitRequestDto kit = kits2.get(0);
        assertThat(kit.getId(), equalTo(kitRequest2.getId()));
        assertThat(kit.getStatus(), equalTo(KitRequestStatus.DEACTIVATED));
        assertThat(kit.getEnrolleeShortcode(), equalTo(enrollee2.getShortcode()));
        assertThat(kit.getDetails().contains("deactivationDate"), equalTo(true));
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
        KitType kitType = kitTypeFactory.buildPersisted(testName);
        AdminUser adminUser = adminUserFactory.buildPersisted(testName);
        StudyEnvironment studyEnvironment = studyEnvironmentFactory.buildPersisted(testName);
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(testName);
        kitTypeFactory.attachTypeToEnvironment(kitType.getId(), studyEnvironment.getId());
        Study study = studyDao.find(studyEnvironment.getStudyId()).get();
        EnrolleeBundle enrolleeBundle1a = enrolleeFactory.buildWithPortalUser(testName, portalEnv, studyEnvironment);
        EnrolleeBundle enrolleeBundle1b = enrolleeFactory.buildWithPortalUser(testName, portalEnv, studyEnvironment);

        Enrollee enrollee1a = enrolleeBundle1a.enrollee();
        Enrollee enrollee1b = enrolleeBundle1b.enrollee();
        KitRequest kitRequest1a = kitRequestFactory.buildPersisted(testName,
            enrollee1a, PepperKitStatus.CREATED, kitType.getId(), adminUser.getId());
        KitRequest kitRequest1b = kitRequestFactory.buildPersisted(testName,
            enrollee1b, PepperKitStatus.CREATED, kitType.getId(), adminUser.getId());

        StudyEnvironment studyEnvironment2 = studyEnvironmentFactory.buildPersisted(testName);
        kitTypeFactory.attachTypeToEnvironment(kitType.getId(), studyEnvironment2.getId());
        Study study2 = studyDao.find(studyEnvironment2.getStudyId()).get();
        EnrolleeBundle enrolleeBundle2 = enrolleeFactory.buildWithPortalUser(testName, portalEnv, studyEnvironment2);
        Enrollee enrollee2 = enrolleeBundle2.enrollee();
        KitRequest kitRequest2 = kitRequestFactory.buildPersisted(testName,
            enrollee2, PepperKitStatus.CREATED, kitType.getId(), adminUser.getId());

        /*
         * Mock DSM to return kits by study:
         *  - the first study has two kits, one in flight and one complete
         *  - the second study has one kit with an error
         */
        PepperKit kitStatus1a = PepperKit.builder()
                .juniperKitId(kitRequest1a.getId().toString())
                .currentStatus(PepperKitStatus.SENT.pepperString)
                .participantId(enrollee1a.getShortcode())
                .build();
        PepperKit kitStatus1b = PepperKit.builder()
                .juniperKitId(kitRequest1b.getId().toString())
                .currentStatus(PepperKitStatus.RECEIVED.pepperString)
                .participantId(enrollee1b.getShortcode())
                .build();
        PepperKit kitStatus2 = PepperKit.builder()
                .juniperKitId(kitRequest2.getId().toString())
                .currentStatus(PepperKitStatus.ERRORED.pepperString)
                .participantId(enrollee2.getShortcode())
                .errorMessage("Something went wrong")
                .errorDate(DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.systemDefault()).format(Instant.now()))
                .build();
        when(mockPepperDSMClient.fetchKitStatusByStudy(eq(study.getShortcode()), any()))
                .thenReturn(List.of(kitStatus1a, kitStatus1b));
        when(mockPepperDSMClient.fetchKitStatusByStudy(eq(study2.getShortcode()), any()))
                .thenReturn(List.of(kitStatus2));
        when(mockEventService.publishKitStatusEvent(any(KitRequest.class), any(Enrollee.class), any(PortalParticipantUser.class), any(KitRequestStatus.class)))
                .thenReturn(null);

        /* Finally, sync the kit statuses  */
        kitRequestService.syncAllKitStatusesFromPepper();

        /* Load and verify each kit */
        verifyKit(kitRequest1a, kitStatus1a, KitRequestStatus.SENT);
        verifyKit(kitRequest1b, kitStatus1b, KitRequestStatus.RECEIVED);
        verifyKit(kitRequest2, kitStatus2, KitRequestStatus.ERRORED);
    }

    @Transactional
    @Test
    void testNotifyKitStatus(TestInfo testInfo) {
        String testName = getTestName(testInfo);
        EnrolleeBundle enrolleeBundle = enrolleeFactory.buildWithPortalUser(testName);

        UUID kitRequestId = UUID.randomUUID();
        KitRequest kitRequest = KitRequest.builder()
                .id(kitRequestId)
                .status(KitRequestStatus.SENT)
                .enrolleeId(enrolleeBundle.enrollee().getId())
                .build();

        when(mockEventService.publishKitStatusEvent(any(KitRequest.class), any(Enrollee.class), any(PortalParticipantUser.class), any(KitRequestStatus.class))).thenAnswer(invocation -> {
            KitRequest kitRequestArg = (KitRequest) invocation.getArguments()[0];
            Enrollee enrolleeArg = (Enrollee) invocation.getArguments()[1];
            PortalParticipantUser ppUserArg = (PortalParticipantUser) invocation.getArguments()[2];
            KitRequestStatus priorStatusArg = (KitRequestStatus) invocation.getArguments()[3];

            assertThat(kitRequestArg.getId(), equalTo(kitRequestId));
            assertThat(kitRequestArg.getStatus(), equalTo(KitRequestStatus.SENT));
            assertThat(ppUserArg.getProfileId(), equalTo(enrolleeBundle.portalParticipantUser().getProfileId()));
            assertThat(enrolleeArg.getShortcode(), equalTo(enrolleeBundle.enrollee().getShortcode()));
            assertThat(priorStatusArg, equalTo(KitRequestStatus.CREATED));
            return null;
        });

        kitRequestService.notifyKitStatusChange(kitRequest, KitRequestStatus.CREATED);
    }

    @Transactional
    @Test
    public void testSyncAllKitStatusesSkipsWithNoKitTypes(TestInfo testInfo) {
        String testName = getTestName(testInfo);
        AdminUser adminUser = adminUserFactory.buildPersisted(testName);
        StudyEnvironment studyEnvironment = studyEnvironmentFactory.buildPersisted(testName);

        kitRequestService.syncAllKitStatusesFromPepper();

        /**
         * note that because we're testing that no kits attempt to be synced, this test is sensitive to studies
         * leaking from other tests.  If this test fails, check that other tests are cleaning up after themselves.
         */
        Mockito.verifyNoInteractions(mockPepperDSMClient);
    }

    /** test that requests are dispatched to the live/stub DSM based on the study environment config */
    @Transactional
    @Test
    public void testRequestKitStudyEnvConfig(TestInfo testInfo) throws Exception {
        PepperKit mockKit = PepperKit.builder().currentStatus("blah").build();
        // set up a study and enrollee, initially set the study env to use stub DSM
        String testName = getTestName(testInfo);
        AdminUser adminUser = adminUserFactory.buildPersisted(testName);
        StudyEnvironmentBundle envBundle = studyEnvironmentFactory.buildBundle(testName, EnvironmentName.sandbox);
        StudyEnvironmentConfig config = studyEnvironmentConfigService.find(envBundle.getStudyEnv().getStudyEnvironmentConfigId()).orElseThrow();
        config.setUseStubDsm(true);
        studyEnvironmentConfigService.update(config);
        Enrollee enrollee = enrolleeFactory.buildWithPortalUser(testName, envBundle.getPortalEnv(), envBundle.getStudyEnv(), new Profile()).enrollee();
        when(mockPepperDSMClient.sendKitRequest(any(), any(), any(), any(), any())).thenReturn(mockKit);
        when(mockPepperDSMClient.fetchKitStatus(any(), any())).thenReturn(mockKit);
        KitRequestDto kitRequestDto = kitRequestService.requestKit(adminUser, envBundle.getStudy().getShortcode(),
                enrollee, new KitRequestService.KitRequestCreationDto("SALIVA", DistributionMethod.MAILED, null, true) );
        kitRequestService.syncKitStatusFromPepper(kitRequestDto.getId());
        Mockito.verify(mockPepperDSMClient).fetchKitStatus(any(), any());
        Mockito.verify(mockPepperDSMClient).sendKitRequest(any(), any(), any(), any(), any());
        Mockito.verifyNoInteractions(livePepperDSMClient);

        // now configure to use the live dsmClient
        config.setUseStubDsm(false);
        studyEnvironmentConfigService.update(config);
        when(livePepperDSMClient.sendKitRequest(any(), any(), any(), any(), any())).thenReturn(mockKit);
        when(livePepperDSMClient.fetchKitStatus(any(), any())).thenReturn(mockKit);
        kitRequestDto = kitRequestService.requestKit(adminUser, envBundle.getStudy().getShortcode(),
                enrollee, new KitRequestService.KitRequestCreationDto("SALIVA", DistributionMethod.MAILED, null, true) );
        kitRequestService.syncKitStatusFromPepper(kitRequestDto.getId());
        Mockito.verify(livePepperDSMClient).sendKitRequest(any(), any(), any(), any(), any());
        Mockito.verify(livePepperDSMClient).fetchKitStatus(any(), any());
    }

    private void verifyKit(KitRequest kit, PepperKit expectedDSMStatus, KitRequestStatus expectedStatus)
            throws JsonProcessingException {
        KitRequest savedKit = kitRequestDao.find(kit.getId()).get();
        assertThat(savedKit.getStatus(), equalTo(expectedStatus));
        assertThat(objectMapper.readValue(savedKit.getExternalKit(), PepperKit.class),
                equalTo(expectedDSMStatus));
    }


    @MockBean
    private StubPepperDSMClient mockPepperDSMClient;
    @MockBean
    private LivePepperDSMClient livePepperDSMClient;
    @MockBean
    private EventService mockEventService;
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
    @Autowired
    private PortalEnvironmentFactory portalEnvironmentFactory;
    @Autowired
    private StudyEnvironmentConfigService studyEnvironmentConfigService;
}
