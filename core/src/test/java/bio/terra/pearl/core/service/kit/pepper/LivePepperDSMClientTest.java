package bio.terra.pearl.core.service.kit.pepper;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.kit.KitRequestFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesRegex;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

public class LivePepperDSMClientTest extends BaseSpringBootTest {


    @Autowired
    private EnrolleeFactory enrolleeFactory;
    @Autowired
    private KitRequestFactory kitRequestFactory;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private LivePepperDSMClient.PepperDSMConfig pepperDSMConfig;

    // select the LivePepperDSMClient @Component, not PepperDSMClientProvider.getLivePepperDSMClient()
    // (which should be exactly the same thing in this case, but Spring can't know that)
    @Qualifier("livePepperDSMClient")
    @Autowired
    private LivePepperDSMClient client;

    public static MockWebServer mockWebServer;

    @BeforeEach
    void startup(TestInfo info) throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        when(pepperDSMConfig.getBasePath())
                .thenReturn("http://localhost:%d".formatted(mockWebServer.getPort()));
        when(pepperDSMConfig.getSecret())
                .thenReturn("secret");
    }

    @AfterEach void shutdown() throws Exception {
        mockWebServer.shutdown();
    }

    @Transactional
    @Test
    public void testBadErrorResponseFromPepper(TestInfo info) throws Exception {
        Enrollee enrollee = enrolleeFactory.buildPersisted(getTestName(info));
        KitRequest kitRequest = kitRequestFactory.buildPersisted(getTestName(info), enrollee);
        PepperKitAddress address = PepperKitAddress.builder().build();

        // Arrange
        String unexpectedJsonBody = "{\"unexpected\": \"boom\"}";
        mockPepperResponse(HttpStatus.BAD_REQUEST, unexpectedJsonBody);

        // "Act"
        Executable act = () -> client.sendKitRequest("testStudy", new StudyEnvironmentConfig(), enrollee, kitRequest, address, false);

        // Assert
        PepperApiException pepperApiException = assertThrows(PepperApiException.class, act);
        assertThat(pepperApiException.getMessage(), containsString(unexpectedJsonBody));
    }

    @Transactional
    @Test
    public void testErrorResponseFromPepperWithUnexpectedAttributes(TestInfo info) throws Exception {
        Enrollee enrollee = enrolleeFactory.buildPersisted(getTestName(info));
        KitRequest kitRequest = kitRequestFactory.buildPersisted(getTestName(info), enrollee);
        PepperKitAddress address = PepperKitAddress.builder().build();

        String unexpectedJsonBody = """
                {
                  "errorMessage": "unknown kit",
                  "value": "12345",
                  "juniperKitId": "12345",
                  "isError": "true",
                  "unexpected": "boom"
                }
                """;
        mockPepperResponse(HttpStatus.BAD_REQUEST, unexpectedJsonBody);

        PepperApiException pepperApiException = assertThrows(PepperApiException.class,
                () -> client.sendKitRequest("testStudy", new StudyEnvironmentConfig(), enrollee, kitRequest, address, false));

        assertThat(pepperApiException.getMessage(), pepperApiException.getErrorResponse(), notNullValue());
        assertThat(pepperApiException.getErrorResponse().getErrorMessage(), equalTo("unknown kit"));
        assertThat(pepperApiException.getErrorResponse().getValue().getDetailMessage(), equalTo("12345"));
        assertThat(pepperApiException.getErrorResponse().getJuniperKitId(), equalTo("12345"));
        assertThat(pepperApiException.getErrorResponse().getIsError(), equalTo(true));
    }

    @Transactional
    @Test
    public void test4xxResponseFromPepper(TestInfo info) throws Exception {
        Enrollee enrollee = enrolleeFactory.buildPersisted(getTestName(info));
        KitRequest kitRequest = kitRequestFactory.buildPersisted(getTestName(info), enrollee);
        PepperKitAddress address = PepperKitAddress.builder().build();

        String kitId = "111-222-333";
        String errorMessage = "UNABLE_TO_VERIFY_ADDRESS";
        mockPepperResponse(
                HttpStatus.BAD_REQUEST,
                objectMapper.writeValueAsString(PepperErrorResponse.builder()
                        .juniperKitId(kitId)
                        .errorMessage(errorMessage)
                        .build()
        ));

        // Assert
        PepperApiException pepperException = assertThrows(PepperApiException.class,
                () -> client.sendKitRequest("testStudy", new StudyEnvironmentConfig(), enrollee, kitRequest, address, false)
        );
        assertThat(pepperException.getMessage(), containsString(kitId));
        assertThat(pepperException.getMessage(), containsString(errorMessage));
    }

    @Transactional
    @Test
    public void test5xxResponseFromPepper(TestInfo info) throws Exception {
        Enrollee enrollee = enrolleeFactory.buildPersisted(getTestName(info));
        KitRequest kitRequest = kitRequestFactory.buildPersisted(getTestName(info), enrollee);
        PepperKitAddress address = PepperKitAddress.builder().build();

        // Arrange
        String errorResponseBody = "Internal server error";
        mockPepperResponse(HttpStatus.INTERNAL_SERVER_ERROR, errorResponseBody);

        // "Act"
        Executable act = () -> client.sendKitRequest("testStudy", new StudyEnvironmentConfig(), enrollee, kitRequest, address, false);

        // Assert
        PepperApiException pepperException = assertThrows(PepperApiException.class, act);
        assertThat(pepperException.getMessage(), containsString(errorResponseBody));
    }

    @Transactional
    @Test
    public void testSendKitRequest(TestInfo info) throws Exception {
        Enrollee enrollee = enrolleeFactory.buildPersisted(getTestName(info));
        KitRequest kitRequest = kitRequestFactory.buildPersisted(getTestName(info), enrollee);
        PepperKitAddress address = PepperKitAddress.builder().build();

        PepperKit kitStatus = PepperKit.builder()
                .juniperKitId(kitRequest.getId().toString())
                .currentStatus(PepperKitStatus.CREATED.pepperString)
                .build();
        PepperKitStatusResponse mockResponse = PepperKitStatusResponse.builder()
                        .isError(false)
                .kits(new PepperKit[]{kitStatus})
                .build();

        mockPepperResponse(HttpStatus.OK, objectMapper.writeValueAsString(mockResponse));

        PepperKit parsedResponse = client.sendKitRequest("testStudy", new StudyEnvironmentConfig(), enrollee, kitRequest, address, false);

        assertThat(parsedResponse.getCurrentStatus(), equalTo(PepperKitStatus.CREATED.pepperString));
        verifyRequestForPath("/shipKit");
    }

    @Transactional
    @Test
    public void testFetchKitStatus(TestInfo info) throws Exception {
        // Arrange
        PepperKit kitStatus = PepperKit.builder()
                .juniperKitId(getTestName(info) + "1")
                .build();
        PepperKit[] kits = { kitStatus };
        PepperKitStatusResponse pepperResponse = PepperKitStatusResponse.builder()
                .kits(kits)
                .isError(false)
                .build();
        mockPepperResponse(HttpStatus.OK, objectMapper.writeValueAsString(pepperResponse));

        // Act
        UUID kitId = UUID.randomUUID();
        PepperKit fetchedKitStatus = client.fetchKitStatus(new StudyEnvironmentConfig(), kitId);

        // Assert
        assertThat(fetchedKitStatus, equalTo(kitStatus));
        // TODO: change "juniperKit" to "juniperkit" after DSM updates this path
        verifyRequestForPath("/kitstatus/juniperKit/%s".formatted(kitId));
    }

    @Transactional
    @Test
    public void testFetchKitStatusByStudy(TestInfo info) throws Exception {
        // Arrange
        PepperKit kitStatus1 = PepperKit.builder()
                .juniperKitId(getTestName(info) + "_kit1")
                .build();
        PepperKit kitStatus2 = PepperKit.builder()
                .juniperKitId(getTestName(info) + "_kit2")
                .build();
        PepperKit[] kits = { kitStatus1, kitStatus2 };
        PepperKitStatusResponse pepperResponse = PepperKitStatusResponse.builder()
                .kits(kits)
                .isError(false)
                .build();
        mockPepperResponse(HttpStatus.OK, objectMapper.writeValueAsString(pepperResponse));

        // Act
        String studyShortcode = "test_study";
        StudyEnvironmentConfig config = StudyEnvironmentConfig.builder().useDevDsmRealm(true).build();
        Collection<PepperKit> fetchedKitStatuses = client.fetchKitStatusByStudy(studyShortcode, config);

        // Assert
        assertThat(fetchedKitStatuses.size(), equalTo(2));
        assertThat(fetchedKitStatuses, contains(kitStatus1, kitStatus2));
        verifyRequestForPath("/kitstatus/study/%s".formatted(LivePepperDSMClient.DEV_STUDY_REALM));

        // now check without the dev path
        config = StudyEnvironmentConfig.builder().useDevDsmRealm(false).build();
        mockPepperResponse(HttpStatus.OK, objectMapper.writeValueAsString(pepperResponse));
        client.fetchKitStatusByStudy(studyShortcode, config);
        verifyRequestForPath("/kitstatus/study/juniper-%s".formatted(studyShortcode));
    }

    private static void mockPepperResponse(HttpStatus status, String pepperResponse) {
        mockWebServer.enqueue(new MockResponse()
                .addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON)
                .setResponseCode(status.value())
                .setBody(pepperResponse));
    }

    private void verifyRequestForPath(String path) throws Exception {
        RecordedRequest recordedRequest = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertThat(recordedRequest.getPath(), equalTo(path));
        assertThat(recordedRequest.getHeader("Authorization"), matchesRegex("Bearer .+"));
    }
}
