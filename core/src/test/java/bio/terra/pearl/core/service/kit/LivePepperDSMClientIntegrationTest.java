package bio.terra.pearl.core.service.kit;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.IntegrationTest;
import bio.terra.pearl.core.dao.kit.KitTypeDao;
import bio.terra.pearl.core.factory.kit.KitRequestFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.function.Executable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.github.seregamorph.hamcrest.MoreMatchers.where;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Integration tests for kit integration with Pepper (DSM).
 *
 * These tests need a signing secret to generate valid JWTs to talk to Pepper. See the project-level README.md for
 * details about setting the DSM_JWT_SIGNING_SECRET environment variable.
 */
public class LivePepperDSMClientIntegrationTest extends BaseSpringBootTest {

    public static final Logger log = LoggerFactory.getLogger(LivePepperDSMClientIntegrationTest.class);

    public static final String STUDY_SHORTCODE = "ourheart";

    @Autowired
    private LivePepperDSMClient livePepperDSMClient;

    @Disabled("Avoid creating endless test data in dev DSM")
    @Transactional
    @IntegrationTest
    public void testSendKitRequest() throws Exception {
        // Arrange
        var enrollee = enrolleeFactory.buildPersisted("testSendKitRequest");
        var kitType = kitTypeDao.findByName("SALIVA").get();
        var kitRequest = kitRequestFactory.buildPersisted("testSendKitRequest", enrollee.getId(), kitType.getId());
        kitRequest.setKitType(kitType);
        var address = PepperKitAddress.builder()
                .firstName("Juniper")
                .lastName("Testerson")
                .street1("415 Main Street")
                .city("Cambridge")
                .state("MA")
                .postalCode("02142")
                .country("USA")
                .build();

        // Act
        var sendKitResponse = livePepperDSMClient.sendKitRequest(STUDY_SHORTCODE, enrollee, kitRequest, address);
        log.info(sendKitResponse);

        // Assert
        var status = objectMapper.readValue(sendKitResponse, PepperKitStatusResponse.class);
        assertThat(status.getKits().length, equalTo(1));
        assertThat(status.getKits()[0].getError(), equalTo(false));
    }

    @Transactional
    @IntegrationTest
    public void testSendKitRequestParsesPepperError() throws Exception {
        // Arrange
        var enrollee = enrolleeFactory.buildPersisted("testSendKitRequestParsesPepperError");
        var kitRequest = kitRequestFactory.buildPersisted("testSendKitRequestParsesPepperError", enrollee.getId());
        var address = PepperKitAddress.builder()
                .firstName("Juniper")
                .lastName("Testerson")
                .street1("123 Fake Street")
                .city("Cambridge")
                .state("MA")
                .postalCode("02142")
                .country("USA")
                .build();

        // "Act"
        Executable act = () -> {
            var newKitStatus = livePepperDSMClient.sendKitRequest(STUDY_SHORTCODE, enrollee, kitRequest, address);
            log.info(newKitStatus);
        };

        // Assert
        PepperException pepperException = assertThrows(PepperException.class, act);
        assertThat(pepperException.getMessage(), pepperException.getErrorResponse(), notNullValue());
        assertThat(pepperException.getErrorResponse().getErrorMessage(), equalTo("UNKNOWN_KIT_TYPE"));
    }

    @Transactional
    @IntegrationTest
    public void testGetKitStatusUnknownKit() throws Exception {
        // Arrange
        var kitId = UUID.randomUUID();

        // "Act"
        Executable act = () -> {
            livePepperDSMClient.fetchKitStatus(kitId);
        };

        // Assert
        PepperException pepperException = assertThrows(PepperException.class, act);
        assertThat(pepperException.getMessage(), containsString(kitId.toString()));
    }

    @Transactional
    @IntegrationTest
    public void testGetKitStatusByKitId() throws Exception {
        // "Arrange"
        /*
        Pepper response from previous successful kit request:
        {"kits":[{"error":false,"juniperKitId":"09f5651b-f6e3-4489-a5e7-ffd10700a724","dsmShippingLabel":"RR2V2LTG967LVHP","participantId":"KGGMPK","labelByEmail":"","scanByEmail":"","deactivationByEmail":"","trackingScanBy":"","errorMessage":"","discardBy":""}],"isError":false}
         */
        var kitId = "09f5651b-f6e3-4489-a5e7-ffd10700a724";

        // Act
        var status = livePepperDSMClient.fetchKitStatus(UUID.fromString(kitId));

        // Assert
        assertThat(status.getJuniperKitId(), equalTo(kitId));
    }

    @Transactional
    @IntegrationTest
    public void testGetKitStatusesByStudy() throws Exception {
        // No arrange; assumes that DSM contains some kits to find

        // Act
        var statuses = livePepperDSMClient.fetchKitStatusByStudy(STUDY_SHORTCODE);

        // Assert
        assertThat(statuses, not(statuses.isEmpty()));
        assertThat(statuses, everyItem(where(PepperKitStatus::getJuniperKitId, notNullValue())));
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
