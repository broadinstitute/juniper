package bio.terra.pearl.api.participant.controller.enrollment;

import bio.terra.pearl.api.participant.api.PreEnrollmentApi;
import bio.terra.pearl.api.participant.service.RequestUtilService;
import bio.terra.pearl.api.participant.service.SurveyExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.survey.ParsedPreEnrollResponse;
import bio.terra.pearl.core.model.survey.PreEnrollmentResponse;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.workflow.EnrollmentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.NotFoundException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class PreEnrollmentController implements PreEnrollmentApi {
  private final ObjectMapper objectMapper;
  private final EnrollmentService enrollmentService;
  private final RequestUtilService requestUtilService;
  private final HttpServletRequest request;
  private final PortalService portalService;
  private final SurveyExtService surveyExtService;

  public PreEnrollmentController(
      ObjectMapper objectMapper,
      EnrollmentService enrollmentService,
      RequestUtilService requestUtilService,
      PortalService portalService,
      HttpServletRequest request,
      SurveyExtService surveyExtService) {
    this.objectMapper = objectMapper;
    this.enrollmentService = enrollmentService;
    this.requestUtilService = requestUtilService;
    this.request = request;
    this.portalService = portalService;
    this.surveyExtService = surveyExtService;
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
    Portal portal =
        portalService.findOneByShortcode(portalShortcode).orElseThrow(NotFoundException::new);
    try {
      PreEnrollmentResponse createdResponse =
          enrollmentService.createAnonymousPreEnroll(
              portal.getId(),
              response.getStudyEnvironmentId(),
              surveyStableId,
              surveyVersion,
              response);
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
        enrollmentService.confirmPreEnrollResponse(preRegResponseId);
    return ResponseEntity.of(responseOpt.map(response -> response));
  }

  @Override
  public ResponseEntity<Object> fetchNewGovernedUserPreEnrollSurvey(
      String portalShortcode, String envName, String studyShortcode) {

    ParticipantUser user = requestUtilService.requireUser(request);

    return ResponseEntity.ok(
        surveyExtService.fetchNewGovernedUserPreEnrollmentSurvey(
            user, portalShortcode, envName, EnvironmentName.valueOf(studyShortcode)));
  }
}
