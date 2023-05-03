package bio.terra.pearl.api.admin.service.forms;

import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import bio.terra.pearl.core.service.study.StudyEnvironmentSurveyService;
import bio.terra.pearl.core.service.survey.SurveyService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class SurveyExtService {
  private AuthUtilService authUtilService;
  private SurveyService surveyService;
  private StudyEnvironmentSurveyService studyEnvironmentSurveyService;

  public SurveyExtService(AuthUtilService authUtilService, SurveyService surveyService,
                          StudyEnvironmentSurveyService studyEnvironmentSurveyService) {
    this.authUtilService = authUtilService;
    this.surveyService = surveyService;
    this.studyEnvironmentSurveyService = studyEnvironmentSurveyService;
  }

  public Survey createNewVersion(String portalShortcode, Survey survey, AdminUser adminUser) {
    Portal portal = authUtilService.authUserToPortal(adminUser, portalShortcode);
    return surveyService.createNewVersion(portal.getId(), survey);
  }

  public StudyEnvironmentSurvey updateConfiguredSurvey(String portalShortcode,
                                                         EnvironmentName envName,
                                                         StudyEnvironmentSurvey updatedObj,
                                                         AdminUser user) {
    authUtilService.authUserToPortal(user, portalShortcode);
    if (user.isSuperuser() || EnvironmentName.sandbox.equals(envName)) {
      StudyEnvironmentSurvey existing = studyEnvironmentSurveyService.find(updatedObj.getId()).get();
      BeanUtils.copyProperties(updatedObj, existing);
      return studyEnvironmentSurveyService.update(existing);
    }
    throw new PermissionDeniedException("You do not have permission to update the {} environment".formatted(envName));
  }
}
