package bio.terra.pearl.api.participant.service;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.study.PortalStudyService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.study.StudyService;
import bio.terra.pearl.core.service.survey.SurveyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SurveyExtService {
  private final AuthUtilService authUtilService;
  private final SurveyService surveyService;
  private final StudyEnvironmentService studyEnvironmentService;
  private final StudyService studyService;
  private final PortalStudyService portalStudyService;

  public SurveyExtService(
      AuthUtilService authUtilService,
      SurveyService surveyService,
      StudyEnvironmentService studyEnvironmentService,
      StudyService studyService,
      PortalStudyService portalStudyService) {
    this.authUtilService = authUtilService;
    this.surveyService = surveyService;
    this.studyEnvironmentService = studyEnvironmentService;
    this.studyService = studyService;
    this.portalStudyService = portalStudyService;
  }

  public Survey fetchNewGovernedUserPreEnrollmentSurvey(
      ParticipantUser user,
      String portalShortcode,
      String studyShortcode,
      EnvironmentName envName) {
    Portal portal =
        authUtilService.authParticipantToPortal(user.getId(), portalShortcode, envName).portal();

    // study is in portal
    portalStudyService.findStudyInPortal(studyShortcode, portal.getId()).orElseThrow();

    StudyEnvironment studyEnvironment =
        studyEnvironmentService
            .findByStudy(studyShortcode, envName)
            .orElseThrow(() -> new NotFoundException("Study environment not found"));

    return surveyService.fetchNewGovernedUserPreEnrollmentSurvey(studyEnvironment.getId());
  }
}
