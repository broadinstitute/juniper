package bio.terra.pearl.core.service.kit;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.kit.KitRequestFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.participant.Enrollee;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

public class LivePepperDSMClientTest extends BaseSpringBootTest {

    // Common test fixture entities
    private Enrollee enrollee;
    private KitRequest kitRequest;
    private PepperKitAddress address;

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

        enrollee = enrolleeFactory.buildPersisted(info.getDisplayName());
        kitRequest = kitRequestFactory.buildPersisted(info.getDisplayName(), enrollee.getId());
        address = PepperKitAddress.builder().build();
    }

    @AfterEach void shutdown() throws Exception {
        mockWebServer.shutdown();
    }

    @Transactional
    @Test
    public void testMalformedErrorResponseFromPepper() throws Exception {
        // Arrange
        var unexpectedJsonBody = "{\"error\": \"boom\"}";
        mockPepperResponse(HttpStatus.BAD_REQUEST, unexpectedJsonBody);

        // "Act"
        Executable act = () -> client.sendKitRequest(enrollee, kitRequest, address);

        // Assert
        PepperException pepperException = assertThrows(PepperException.class, act);
        assertThat(pepperException.getMessage(), containsString(unexpectedJsonBody));
    }

    @Transactional
    @Test
    public void test4xxResponseFromPepper() throws Exception {
        // Arrange
        var kitId = "111-222-333";
        var errorMessage = "UNABLE_TO_VERIFY_ADDRESS";
        mockPepperResponse(
                HttpStatus.BAD_REQUEST,
                objectMapper.writeValueAsString(PepperErrorResponse.builder()
                        .juniperKitId(kitId)
                        .errorMessage(errorMessage)
                        .build()
        ));

        // "Act"
        Executable act = () -> client.sendKitRequest(enrollee, kitRequest, address);

        // Assert
        var pepperException = assertThrows(PepperException.class, act);
        assertThat(pepperException.getMessage(), containsString(kitId));
        assertThat(pepperException.getMessage(), containsString(errorMessage));
    }

    @Transactional
    @Test
    public void test5xxResponseFromPepper() throws Exception {
        // Arrange
        var errorResponseBody = "Internal server error";
        mockPepperResponse(HttpStatus.INTERNAL_SERVER_ERROR, errorResponseBody);

        // "Act"
        Executable act = () -> client.sendKitRequest(enrollee, kitRequest, address);

        // Assert
        var pepperException = assertThrows(PepperException.class, act);
        assertThat(pepperException.getMessage(), containsString(errorResponseBody));
    }

    @Transactional
    @Test
    public void testSendKitRequest() throws Exception {
        // Arrange
        PepperKitStatus kitStatus = PepperKitStatus.builder()
                .kitId(kitRequest.getId().toString())
                .currentStatus("New")
                .build();
        mockPepperResponse(HttpStatus.OK, objectMapper.writeValueAsString(kitStatus));

        // Act
        var response = client.sendKitRequest(enrollee, kitRequest, address);

        // Assert
        assertThat(response, containsString(kitStatus.getKitId()));
        assertThat(response, containsString("New"));
        verifyRequestForPath("/shipKit");
    }

    @Transactional
    @Test
    public void testFetchKitStatus() throws Exception {
        // Arrange
        PepperKitStatus kitStatus = PepperKitStatus.builder()
                .kitId("testFetchKitStatusByStudy1")
                .build();
        PepperKitStatus[] kits = { kitStatus };
        var pepperResponse = PepperKitStatusResponse.builder()
                .kits(kits)
                .build();
        mockPepperResponse(HttpStatus.OK, objectMapper.writeValueAsString(pepperResponse));

        // Act
        var kitId = UUID.randomUUID();
        var fetchedKitStatus = client.fetchKitStatus(kitId);

        // Assert
        assertThat(fetchedKitStatus, equalTo(kitStatus));
        verifyRequestForPath("/kitStatus/kit/%s".formatted(kitId));
    }

    @Transactional
    @Test
    public void testFetchKitStatusByStudy() throws Exception {
        // Arrange
        PepperKitStatus kitStatus1 = PepperKitStatus.builder()
                .kitId("testFetchKitStatusByStudy_kit1")
                .build();
        PepperKitStatus kitStatus2 = PepperKitStatus.builder()
                .kitId("testFetchKitStatusByStudy_kit2")
                .build();
        PepperKitStatus[] kits = { kitStatus1, kitStatus2 };
        var pepperResponse = PepperKitStatusResponse.builder()
                .kits(kits)
                .build();
        mockPepperResponse(HttpStatus.OK, objectMapper.writeValueAsString(pepperResponse));

        // Act
        var studyName = "test_study";
        var fetchedKitStatuses = client.fetchKitStatusByStudy(studyName);

        // Assert
        assertThat(fetchedKitStatuses.size(), equalTo(2));
        assertThat(fetchedKitStatuses, contains(kitStatus1, kitStatus2));
        verifyRequestForPath("/kitStatus/study/%s".formatted(studyName));
    }

    private static void mockPepperResponse(HttpStatus status, String pepperResponse) {
        mockWebServer.enqueue(new MockResponse()
                .addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON)
                .setResponseCode(status.value())
                .setBody(pepperResponse));
    }

    private void verifyRequestForPath(String path) throws Exception {
        var recordedRequest = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertThat(recordedRequest.getPath(), equalTo(path));
        assertThat(recordedRequest.getHeader("Authorization"), matchesRegex("Bearer .+"));
    }

    @Autowired
    private EnrolleeFactory enrolleeFactory;
    @Autowired
    private KitRequestFactory kitRequestFactory;
    @Autowired
    private ObjectMapper objectMapper;
}
