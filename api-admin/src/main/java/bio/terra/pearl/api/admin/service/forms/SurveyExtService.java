package bio.terra.pearl.api.admin.service.forms;

import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.study.StudyEnvironmentSurveyService;
import bio.terra.pearl.core.service.survey.SurveyService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class SurveyExtService {
  private AuthUtilService authUtilService;
  private SurveyService surveyService;
  private StudyEnvironmentSurveyService studyEnvironmentSurveyService;
  private StudyEnvironmentService studyEnvironmentService;

  public SurveyExtService(
      AuthUtilService authUtilService,
      SurveyService surveyService,
      StudyEnvironmentSurveyService studyEnvironmentSurveyService,
      StudyEnvironmentService studyEnvironmentService) {
    this.authUtilService = authUtilService;
    this.surveyService = surveyService;
    this.studyEnvironmentSurveyService = studyEnvironmentSurveyService;
    this.studyEnvironmentService = studyEnvironmentService;
  }

  public Survey get(String portalShortcode, String stableId, int version, AdminUser adminUser) {
    Portal portal = authUtilService.authUserToPortal(adminUser, portalShortcode);
    Survey survey = authSurveyToPortal(portal, stableId, version);
    surveyService.attachAnswerMappings(survey);
    return survey;
  }

  public List<Survey> listVersions(String portalShortcode, String stableId, AdminUser adminUser) {
    Portal portal = authUtilService.authUserToPortal(adminUser, portalShortcode);
    authSurveyToPortal(portal, stableId, 1); // TODO: this assumes there's always a v1. Bad?

    // This is used to populate the version selector in the admin UI. It's not necessary
    // to return the surveys with any content or answer mappings, the response will
    // be too large. Instead, just get the individual versions as content is needed.
    List<Survey> surveys = surveyService.findByStableIdNoContent(stableId);
    return surveys;
  }

  public Survey create(String portalShortcode, Survey survey, AdminUser adminUser) {
    Portal portal = authUtilService.authUserToPortal(adminUser, portalShortcode);
    List<Survey> existing = surveyService.findByStableId(survey.getStableId());
    if (existing.size() > 0) {
      throw new IllegalArgumentException("A survey with that stableId already exists");
    }
    survey.setPortalId(portal.getId());
    survey.setVersion(1);
    return surveyService.create(survey);
  }

  public Survey createNewVersion(String portalShortcode, Survey survey, AdminUser adminUser) {
    Portal portal = authUtilService.authUserToPortal(adminUser, portalShortcode);
    return surveyService.createNewVersion(portal.getId(), survey);
  }

  public StudyEnvironmentSurvey createConfiguredSurvey(
      String portalShortcode,
      String studyShortcode,
      EnvironmentName envName,
      StudyEnvironmentSurvey surveyToConfigure,
      AdminUser user) {
    authUtilService.authUserToPortal(user, portalShortcode);
    if (user.isSuperuser() || EnvironmentName.sandbox.equals(envName)) {
      return studyEnvironmentSurveyService.create(surveyToConfigure);
    }
    throw new PermissionDeniedException(
        "You do not have permission to update the %s environment".formatted(envName));
  }

  public StudyEnvironmentSurvey updateConfiguredSurvey(
      String portalShortcode,
      EnvironmentName envName,
      StudyEnvironmentSurvey updatedObj,
      AdminUser user) {
    authUtilService.authUserToPortal(user, portalShortcode);
    if (user.isSuperuser() || EnvironmentName.sandbox.equals(envName)) {
      StudyEnvironmentSurvey existing =
          studyEnvironmentSurveyService.find(updatedObj.getId()).get();
      BeanUtils.copyProperties(updatedObj, existing);
      return studyEnvironmentSurveyService.update(existing);
    }
    throw new PermissionDeniedException(
        "You do not have permission to update the %s environment".formatted(envName));
  }

  /** confirms that the Survey is accessible from the given portal */
  public Survey authSurveyToPortal(Portal portal, String stableId, int version) {
    Optional<Survey> surveyOpt = surveyService.findByStableId(stableId, version);
    if (surveyOpt.isEmpty()) {
      throw new NotFoundException("No such survey exists in " + portal.getName());
    }
    Survey survey = surveyOpt.get();
    if (!portal.getId().equals(survey.getPortalId())) {
      throw new NotFoundException("No such survey exists in " + portal.getName());
    }
    return survey;
  }
}
