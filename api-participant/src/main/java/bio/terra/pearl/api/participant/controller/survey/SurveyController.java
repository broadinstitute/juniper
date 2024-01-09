package bio.terra.pearl.api.participant.controller.survey;

import bio.terra.pearl.api.participant.api.OutreachApi;
import bio.terra.pearl.api.participant.service.RequestUtilService;
import bio.terra.pearl.api.participant.service.SurveyExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class SurveyController implements OutreachApi {
  private EnrolleeService enrolleeService;
  private SurveyExtService surveyExtService;
  private RequestUtilService requestUtilService;
  private HttpServletRequest request;

  @Autowired
  public SurveyController(
      EnrolleeService enrolleeService,
      SurveyExtService surveyExtService,
      RequestUtilService requestUtilService,
      HttpServletRequest request) {
    this.enrolleeService = enrolleeService;
    this.surveyExtService = surveyExtService;
    this.requestUtilService = requestUtilService;
    this.request = request;
  }

  @Override
  public ResponseEntity<Object> listOutreachActivities(
      String portalShortcode, String envName, String studyShortcode, String enrolleeShortcode) {
    ParticipantUser user = requestUtilService.requireUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);

    List<Survey> outreachSurveys =
        surveyExtService.listOutreachActivities(
            user, portalShortcode, environmentName, studyShortcode, enrolleeShortcode);
    return ResponseEntity.ok(outreachSurveys);
  }
}
