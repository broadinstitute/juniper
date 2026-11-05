package bio.terra.pearl.api.admin.controller.kit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import bio.terra.pearl.api.admin.model.ErrorReport;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyEnvAuthContext;
import bio.terra.pearl.api.admin.service.kit.KitExtService;
import bio.terra.pearl.core.model.kit.DistributionMethod;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.kit.KitRequestService;
import bio.terra.pearl.core.service.kit.sidr.SIDRApiException;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** what study staff do and don't get to see when SIDR rejects a kit request */
class KitControllerSIDRErrorTest {
  private final ObjectMapper objectMapper = new ObjectMapper();
  private static final String SECRET_BODY = "{\"trace\": \"SECRET-INTERNAL-DETAIL\"}";

  @Test
  void singleRequestErrorsOnlyIncludeTheUserMessage() {
    KitController controller =
        new KitController(null, null, mock(HttpServletRequest.class), objectMapper);

    ResponseEntity<ErrorReport> response =
        controller.handleSIDRApiException(
            SIDRApiException.fromResponse(HttpStatus.INTERNAL_SERVER_ERROR, SECRET_BODY));
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertFalse(response.getBody().getMessage().contains("SECRET"));

    response =
        controller.handleSIDRApiException(
            SIDRApiException.fromResponse(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "{\"errors\":[{\"type\":\"invalid.address_invalid\"}]}"));
    assertTrue(response.getBody().getMessage().startsWith("ADDRESS_VALIDATION_ERROR"));
  }

  @Test
  void batchRequestErrorsDoNotLeakDetailsOrCauses() throws Exception {
    KitRequestService kitRequestService = mock(KitRequestService.class);
    EnrolleeService enrolleeService = mock(EnrolleeService.class);
    when(enrolleeService.findByShortcodeAndStudyEnvId(any(), any()))
        .thenReturn(Optional.of(Enrollee.builder().shortcode("HDSALK").build()));
    when(kitRequestService.requestKit(any(), any(), any(), any()))
        .thenThrow(SIDRApiException.fromResponse(HttpStatus.INTERNAL_SERVER_ERROR, SECRET_BODY));
    KitExtService service = new KitExtService(kitRequestService, null, null, enrolleeService);
    PortalStudyEnvAuthContext authContext = mock(PortalStudyEnvAuthContext.class);
    when(authContext.getStudyEnvironment())
        .thenReturn(StudyEnvironment.builder().id(UUID.randomUUID()).build());

    KitExtService.KitRequestListResponse response =
        service.requestKits(
            authContext,
            List.of("HDSALK"),
            new KitRequestService.KitRequestCreationDto(
                "SALIVA", DistributionMethod.MAILED, null, false, null, null, true));

    assertEquals(1, response.getExceptions().size());
    assertTrue(
        response.getExceptions().get(0).getMessage().startsWith("HDSALK: SIDR could not process"));
    assertNull(response.getExceptions().get(0).getCause());
    // this is what actually gets serialized to the browser
    assertFalse(objectMapper.writeValueAsString(response).contains("SECRET"));
  }
}
