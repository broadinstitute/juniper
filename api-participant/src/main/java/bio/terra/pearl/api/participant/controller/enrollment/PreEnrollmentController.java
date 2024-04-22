package bio.terra.pearl.api.participant.controller.enrollment;

import bio.terra.pearl.api.participant.api.PreEnrollmentApi;
import bio.terra.pearl.api.participant.service.RequestUtilService;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.survey.ParsedPreEnrollResponse;
import bio.terra.pearl.core.model.survey.PreEnrollmentResponse;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.workflow.EnrollmentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.NotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.util.Optional;
import java.util.UUID;

@Controller
public class PreEnrollmentController implements PreEnrollmentApi {
  private ObjectMapper objectMapper;
  private EnrollmentService enrollmentService;
  private RequestUtilService requestUtilService;
  private HttpServletRequest request;
  private PortalService portalService;

  public PreEnrollmentController(
      ObjectMapper objectMapper,
      EnrollmentService enrollmentService,
      RequestUtilService requestUtilService,
      PortalService portalService,
      HttpServletRequest request) {
    this.objectMapper = objectMapper;
    this.enrollmentService = enrollmentService;
    this.requestUtilService = requestUtilService;
    this.request = request;
    this.portalService = portalService;
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


  // TODO: endpoint for getting pre-enroll survey when adding new participant
  //       Steps:
  //        - get pre enroll
  //        - get answer mappings
  //        - use survey utils to disable proxy question
  //        - get survey utils to set default value of true to proxy question
  //       addGovernedUserPreEnrollSurvey()
  // TODO: endpoint for creating new governed user
  //       Steps:
  //         - Call enrollAsProxy directly? Maybe with EXT method to grab proxy enrollee user
  //       createGovernedUser()
}
