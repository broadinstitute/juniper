package bio.terra.pearl.api.participant.controller.enrollment;

import bio.terra.pearl.api.participant.api.PreEnrollmentApi;
import bio.terra.pearl.api.participant.service.RequestUtilService;
import bio.terra.pearl.core.model.survey.ParsedPreEnrollResponse;
import bio.terra.pearl.core.model.survey.PreEnrollmentResponse;
import bio.terra.pearl.core.service.workflow.EnrollmentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class PreEnrollmentController implements PreEnrollmentApi {
  private ObjectMapper objectMapper;
  private EnrollmentService enrollmentService;
  private RequestUtilService requestUtilService;
  private HttpServletRequest request;

  public PreEnrollmentController(
      ObjectMapper objectMapper,
      EnrollmentService enrollmentService,
      RequestUtilService requestUtilService,
      HttpServletRequest request) {
    this.objectMapper = objectMapper;
    this.enrollmentService = enrollmentService;
    this.requestUtilService = requestUtilService;
    this.request = request;
  }

  @Override
  public ResponseEntity<Object> createAnonymous(
      String portalShortcode,
      String envName,
      String surveyStableId,
      Integer surveyVersion,
      Object body) {
    ParsedPreEnrollResponse response =
        objectMapper.convertValue(body, ParsedPreEnrollResponse.class);
    try {
      String responseData = objectMapper.writeValueAsString(response.getParsedData());
      PreEnrollmentResponse createdResponse =
          enrollmentService.createAnonymousPreEnroll(
              response.getStudyEnvironmentId(), surveyStableId, surveyVersion, responseData);
      return ResponseEntity.ok(createdResponse);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("malformatted response data", e);
    }
  }

  /**
   * Confirms that a given preEnrollResponseId is valid to be used for an enrollment. This is
   * necessary for cases where the UI caches the ID across page refreshes, to ensure the participant
   * won't complete enrollment only to find out the linked pre-enroll was not valid.
   */
  @Override
  public ResponseEntity<Object> confirm(
      String portalShortcode, String envName, UUID preRegResponseId) {
    Optional<PreEnrollmentResponse> responseOpt =
        enrollmentService.findPreEnrollResponse(preRegResponseId);
    if (responseOpt.isPresent() && responseOpt.get().getEnrolleeId() != null) {
      return ResponseEntity.unprocessableEntity().body("Already enrolled");
    }
    return ResponseEntity.of(responseOpt.map(response -> response));
  }
}
