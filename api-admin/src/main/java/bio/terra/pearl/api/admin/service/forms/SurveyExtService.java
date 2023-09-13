package bio.terra.pearl.api.admin.service.forms;

import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.study.StudyEnvironmentSurveyService;
import bio.terra.pearl.core.service.survey.SurveyService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class SurveyExtService {
  private AuthUtilService authUtilService;
  private SurveyService surveyService;
  private StudyEnvironmentSurveyService studyEnvironmentSurveyService;

  public SurveyExtService(
      AuthUtilService authUtilService,
      SurveyService surveyService,
      StudyEnvironmentSurveyService studyEnvironmentSurveyService) {
    this.authUtilService = authUtilService;
    this.surveyService = surveyService;
    this.studyEnvironmentSurveyService = studyEnvironmentSurveyService;
  }

  public Survey get(String portalShortcode, String stableId, int version, AdminUser adminUser) {
    Portal portal = authUtilService.authUserToPortal(adminUser, portalShortcode);
    Survey survey = authSurveyToPortal(portal, stableId, version);
    surveyService.attachAnswerMappings(survey);
    return survey;
  }

  public List<Survey> listVersions(String portalShortcode, String stableId, AdminUser adminUser) {
    Portal portal = authUtilService.authUserToPortal(adminUser, portalShortcode);
    // This is used to populate the version selector in the admin UI. It's not necessary
    // to return the surveys with any content or answer mappings, the response will
    // be too large. Instead, just get the individual versions as content is needed.
    List<Survey> surveys = surveyService.findByStableIdNoContent(stableId);
    List<Survey> surveysInPortal =
        surveys.stream().filter(survey -> portal.getId().equals(survey.getPortalId())).toList();
    return surveysInPortal;
  }

  public Survey create(String portalShortcode, Survey survey, AdminUser adminUser) {
    Portal portal = authUtilService.authUserToPortal(adminUser, portalShortcode);
    List<Survey> existing = surveyService.findByStableId(survey.getStableId());
    if (existing.size() > 0) {
      throw new IllegalArgumentException("A survey with that stableId already exists");
    }
    survey.setPortalId(portal.getId());
    survey.setVersion(1);
    Instant now = Instant.now();
    survey.setCreatedAt(now);
    survey.setLastUpdatedAt(now);
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
    if (!EnvironmentName.sandbox.equals(envName)) {
      throw new IllegalArgumentException(
          "Updates can only be made directly to the sandbox environment".formatted(envName));
    }
    return studyEnvironmentSurveyService.create(surveyToConfigure);
  }

  public StudyEnvironmentSurvey updateConfiguredSurvey(
      String portalShortcode,
      EnvironmentName envName,
      StudyEnvironmentSurvey updatedObj,
      AdminUser user) {
    authUtilService.authUserToPortal(user, portalShortcode);
    if (!EnvironmentName.sandbox.equals(envName)) {
      throw new IllegalArgumentException(
          "Updates can only be made directly to the sandbox environment".formatted(envName));
    }
    StudyEnvironmentSurvey existing = studyEnvironmentSurveyService.find(updatedObj.getId()).get();
    BeanUtils.copyProperties(updatedObj, existing);
    return studyEnvironmentSurveyService.update(existing);
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
