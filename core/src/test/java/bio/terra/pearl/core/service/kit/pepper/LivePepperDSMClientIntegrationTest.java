package bio.terra.pearl.core.service.kit.pepper;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.IntegrationTest;
import bio.terra.pearl.core.dao.kit.KitTypeDao;
import bio.terra.pearl.core.factory.kit.KitRequestFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.kit.KitType;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import bio.terra.pearl.core.service.study.StudyEnvironmentConfigService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.UUID;

import static com.github.seregamorph.hamcrest.MoreMatchers.where;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Integration tests for kit integration with Pepper (DSM).
 *
 * These tests need a signing secret to generate valid JWTs to talk to Pepper. See the project-level README.md for
 * details about setting the DSM_JWT_SIGNING_SECRET environment variable.
 */
public class LivePepperDSMClientIntegrationTest extends BaseSpringBootTest {
    public static final String STUDY_SHORTCODE = "ourheart";

    @Autowired
    private LivePepperDSMClient livePepperDSMClient;
    @Autowired
    private StudyEnvironmentConfigService studyEnvironmentConfigService;

    @Disabled("Avoid creating endless test data in dev DSM")
    @Transactional
    @IntegrationTest
    public void testSendKitRequest(TestInfo info) throws Exception {
        Enrollee enrollee = enrolleeFactory.buildPersisted(getTestName(info));
        KitType kitType = kitTypeDao.findByName("SALIVA").get();
        KitRequest kitRequest = kitRequestFactory.buildPersisted(getTestName(info), enrollee, PepperKitStatus.CREATED,
                kitType.getId());
        kitRequest.setKitType(kitType);
        PepperKitAddress address = PepperKitAddress.builder()
                .firstName("Juniper")
                .lastName("Testerson")
                .street1("415 Main Street")
                .city("Cambridge")
                .state("MA")
                .postalCode("02142")
                .country("USA")
                .build();
        StudyEnvironmentConfig studyEnvironmentConfig = studyEnvironmentConfigService.findByStudyEnvironmentId(enrollee.getStudyEnvironmentId());
        PepperKit sendKitResponse = livePepperDSMClient.sendKitRequest(STUDY_SHORTCODE, studyEnvironmentConfig, enrollee, kitRequest, address);
        assertThat(sendKitResponse.getCurrentStatus(), equalTo("Kit without label"));
    }

    @Transactional
    @IntegrationTest
    public void testSendKitRequestParsesPepperError(TestInfo info) throws Exception {
        Enrollee enrollee = enrolleeFactory.buildPersisted(getTestName(info));
        KitRequest kitRequest = kitRequestFactory.buildPersisted(getTestName(info), enrollee);
        PepperKitAddress address = PepperKitAddress.builder()
                .firstName("Juniper")
                .lastName("Testerson")
                .street1("123 Fake Street")
                .city("Cambridge")
                .state("MA")
                .postalCode("02142")
                .country("USA")
                .build();

        StudyEnvironmentConfig studyEnvironmentConfig = studyEnvironmentConfigService.findByStudyEnvironmentId(enrollee.getStudyEnvironmentId());

        PepperApiException pepperApiException = assertThrows(PepperApiException.class, () -> {
            livePepperDSMClient.sendKitRequest(STUDY_SHORTCODE, studyEnvironmentConfig, enrollee, kitRequest, address);
        });
        assertThat(pepperApiException.getMessage(), pepperApiException.getErrorResponse(), notNullValue());
        assertThat(pepperApiException.getMessage(), equalTo("UNKNOWN_KIT_TYPE"));
    }

    @Transactional
    @IntegrationTest
    public void testGetKitStatusUnknownKit() throws Exception {
        // Arrange
        UUID kitId = UUID.randomUUID();

        // "Act"
        Executable act = () -> {
            livePepperDSMClient.fetchKitStatus(new StudyEnvironmentConfig(), kitId);
        };

        // Assert
        PepperApiException pepperApiException = assertThrows(PepperApiException.class, act);
        assertThat(pepperApiException.getMessage(), containsString(kitId.toString()));
    }

    @Transactional
    @IntegrationTest
    public void testGetKitStatusByKitId() throws Exception {
        // "Arrange"
        /*
        Pepper response from previous successful kit request:
        {"kits":[{"error":false,"juniperKitId":"09f5651b-f6e3-4489-a5e7-ffd10700a724","dsmShippingLabel":"RR2V2LTG967LVHP","participantId":"KGGMPK","labelByEmail":"","scanByEmail":"","deactivationByEmail":"","trackingScanBy":"","errorMessage":"","discardBy":""}],"isError":false}
         */
        String kitId = "09f5651b-f6e3-4489-a5e7-ffd10700a724";

        // Act
        PepperKit status = livePepperDSMClient.fetchKitStatus(new StudyEnvironmentConfig(),UUID.fromString(kitId));

        // Assert
        assertThat(status.getJuniperKitId(), equalTo(kitId));
    }

    @Transactional
    @IntegrationTest
    public void testGetKitStatusesByStudy() throws Exception {
        // No arrange; assumes that DSM contains some kits to find
        StudyEnvironmentConfig config = StudyEnvironmentConfig.builder()
                .useDevDsmRealm(false).build();

        // Act
        Collection<PepperKit> statuses = livePepperDSMClient.fetchKitStatusByStudy(STUDY_SHORTCODE, config);

        // Assert
        assertThat(statuses, not(statuses.isEmpty()));
        assertThat(statuses, everyItem(where(PepperKit::getJuniperKitId, notNullValue())));
    }

    @Autowired
    private EnrolleeFactory enrolleeFactory;
    @Autowired
    private KitRequestFactory kitRequestFactory;
    @Autowired
    private KitTypeDao kitTypeDao;
    @Autowired
    private ObjectMapper objectMapper;
}
