package bio.terra.pearl.api.admin.service.forms;

import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.PortalStudy;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpressionParser;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.study.StudyEnvironmentSurveyService;
import bio.terra.pearl.core.service.survey.SurveyService;
import bio.terra.pearl.core.service.workflow.EventService;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SurveyExtService {
  private AuthUtilService authUtilService;
  private SurveyService surveyService;
  private StudyEnvironmentSurveyService studyEnvironmentSurveyService;
  private StudyEnvironmentService studyEnvironmentService;
  private PortalEnvironmentService portalEnvironmentService;
  private EventService eventService;
  private EnrolleeSearchExpressionParser enrolleeSearchExpressionParser;

  public SurveyExtService(
      AuthUtilService authUtilService,
      SurveyService surveyService,
      StudyEnvironmentSurveyService studyEnvironmentSurveyService,
      StudyEnvironmentService studyEnvironmentService,
      PortalEnvironmentService portalEnvironmentService,
      EventService eventService,
      EnrolleeSearchExpressionParser enrolleeSearchExpressionParser) {
    this.authUtilService = authUtilService;
    this.surveyService = surveyService;
    this.studyEnvironmentSurveyService = studyEnvironmentSurveyService;
    this.studyEnvironmentService = studyEnvironmentService;
    this.portalEnvironmentService = portalEnvironmentService;
    this.eventService = eventService;
    this.enrolleeSearchExpressionParser = enrolleeSearchExpressionParser;
  }

  public Survey get(String portalShortcode, String stableId, int version, AdminUser operator) {
    Portal portal = authUtilService.authUserToPortal(operator, portalShortcode);
    Survey survey = authUtilService.authSurveyToPortal(portal, stableId, version);
    surveyService.attachAnswerMappings(survey);
    return survey;
  }

  public List<Survey> listVersions(String portalShortcode, String stableId, AdminUser operator) {
    Portal portal = authUtilService.authUserToPortal(operator, portalShortcode);
    // This is used to populate the version selector in the admin UI. It's not necessary
    // to return the surveys with any content or answer mappings, the response will
    // be too large. Instead, just get the individual versions as content is needed.
    List<Survey> surveys = surveyService.findByStableIdNoContent(stableId);
    List<Survey> surveysInPortal =
        surveys.stream().filter(survey -> portal.getId().equals(survey.getPortalId())).toList();
    return surveysInPortal;
  }

  public List<StudyEnvironmentSurvey> findWithSurveyNoContent(
      String portalShortcode,
      String studyShortcode,
      EnvironmentName envName,
      String stableId,
      Boolean active,
      AdminUser operator) {
    authUtilService.authUserToPortal(operator, portalShortcode);
    PortalStudy portalStudy =
        authUtilService.authUserToStudy(operator, portalShortcode, studyShortcode);
    List<UUID> studyEnvIds =
        studyEnvIds =
            studyEnvironmentService
                .findByStudy(portalStudy.getStudyId())
                // if no envName is specified, include all environments, otherwise just include the
                // specified one
                .stream()
                .filter(
                    studyEnv -> envName == null || studyEnv.getEnvironmentName().equals(envName))
                .map(StudyEnvironment::getId)
                .toList();
    return studyEnvironmentSurveyService.findAllWithSurveyNoContent(studyEnvIds, stableId, active);
  }

  public Survey create(String portalShortcode, Survey survey, AdminUser operator) {
    Portal portal = authUtilService.authUserToPortal(operator, portalShortcode);
    List<Survey> existing = surveyService.findByStableId(survey.getStableId(), portal.getId());
    if (existing.size() > 0) {
      throw new IllegalArgumentException("A survey with that stableId already exists");
    }
    // ensure the rule can be parsed (if null/empty, this will be a no-op)
    enrolleeSearchExpressionParser.parseRule(survey.getEligibilityRule());
    survey.setPortalId(portal.getId());
    survey.setVersion(1);
    return surveyService.create(survey);
  }

  @Transactional
  public void delete(String portalShortcode, String surveyStableId, AdminUser operator) {
    Portal portal = authUtilService.authUserToPortal(operator, portalShortcode);
    // Find all of the versions of the specified survey that are in the specified portal
    List<Survey> existingVersions =
        surveyService.findByStableId(surveyStableId, portal.getId()).stream()
            .filter(survey -> portal.getId().equals(survey.getPortalId()))
            .toList();

    if (existingVersions.size() == 0) {
      throw new NotFoundException("Survey not found");
    }

    List<UUID> existingVersionIds = existingVersions.stream().map(BaseEntity::getId).toList();

    // Find all of the configured surveys that use any version of the specified survey
    List<StudyEnvironmentSurvey> referencingSurveys =
        existingVersionIds.stream()
            .flatMap(
                surveyId -> {
                  List<StudyEnvironmentSurvey> configuredSurveys =
                      studyEnvironmentSurveyService.findBySurveyId(surveyId);
                  return configuredSurveys.stream();
                })
            .toList();

    // Resolve all of the study environments that contain the configured surveys
    List<StudyEnvironment> referencingStudyEnvs =
        studyEnvironmentService.findAll(
            referencingSurveys.stream()
                .map(StudyEnvironmentSurvey::getStudyEnvironmentId)
                .toList());

    if (referencingStudyEnvs.stream()
        .anyMatch(r -> !EnvironmentName.sandbox.equals(r.getEnvironmentName()))) {
      throw new IllegalArgumentException(
          "Survey is referenced by a non-sandbox environment and cannot be deleted");
    } else {
      // At this point, only sandbox environments reference the survey, so we can unlink
      // the survey from each env to prepare for full deletion.
      existingVersionIds.forEach(id -> studyEnvironmentSurveyService.deleteBySurveyId(id));

      // Delete all survey versions
      existingVersions.forEach(
          survey -> surveyService.delete(survey.getId(), CascadeProperty.EMPTY_SET));
    }
  }

  public Survey createNewVersion(String portalShortcode, Survey survey, AdminUser operator) {
    Portal portal = authUtilService.authUserToPortal(operator, portalShortcode);
    // ensure the rule can be parsed (if null/empty, this will be a no-op)
    enrolleeSearchExpressionParser.parseRule(survey.getEligibilityRule());
    return surveyService.createNewVersion(portal.getId(), survey);
  }

  public StudyEnvironmentSurvey createConfiguredSurvey(
      String portalShortcode,
      String studyShortcode,
      EnvironmentName envName,
      StudyEnvironmentSurvey surveyToConfigure,
      AdminUser operator) {
    AuthEntities authEntities =
        authConfiguredSurveyRequest(
            portalShortcode, envName, studyShortcode, surveyToConfigure, operator);

    StudyEnvironmentSurvey studyEnvSurvey = studyEnvironmentSurveyService.create(surveyToConfigure);
    eventService.publishSurveyPublishedEvent(
        authEntities.portalEnv.getId(), authEntities.studyEnv.getId(), authEntities.survey());
    return studyEnvSurvey;
  }

  public void removeConfiguredSurvey(
      String portalShortcode,
      String studyShortcode,
      EnvironmentName envName,
      UUID configuredSurveyId,
      AdminUser operator) {
    StudyEnvironmentSurvey configuredSurvey =
        studyEnvironmentSurveyService.find(configuredSurveyId).get();
    authConfiguredSurveyRequest(
        portalShortcode, envName, studyShortcode, configuredSurvey, operator);
    studyEnvironmentSurveyService.deactivate(configuredSurveyId);
  }

  public StudyEnvironmentSurvey updateConfiguredSurvey(
      String portalShortcode,
      EnvironmentName envName,
      String studyShortcode,
      StudyEnvironmentSurvey updatedObj,
      AdminUser operator) {
    authConfiguredSurveyRequest(portalShortcode, envName, studyShortcode, updatedObj, operator);
    StudyEnvironmentSurvey existing = studyEnvironmentSurveyService.find(updatedObj.getId()).get();
    BeanUtils.copyProperties(updatedObj, existing);
    return studyEnvironmentSurveyService.update(existing);
  }

  /**
   * deactivates the studyEnvironmentSurvey with studyEnvrionmentSurveyId, and adds a new config as
   * specified in the update object. Note that the portalEnvironmentId and studyEnvironmentId will
   * be set from the portalShortcode and studyShortcode params.
   */
  @Transactional
  public StudyEnvironmentSurvey replace(
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName,
      UUID studyEnvironmentSurveyId,
      StudyEnvironmentSurvey update,
      AdminUser operator) {
    authUtilService.authUserToPortal(operator, portalShortcode);

    AuthEntities authEntities =
        authConfiguredSurveyRequest(
            portalShortcode, environmentName, studyShortcode, update, operator);
    StudyEnvironmentSurvey existing =
        studyEnvironmentSurveyService
            .find(studyEnvironmentSurveyId)
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "No existing StudyEnvironmentSurvey with id " + studyEnvironmentSurveyId));
    verifyStudyEnvironmentSurvey(existing, authEntities.studyEnv);
    StudyEnvironmentSurvey newConfig =
        studyEnvironmentSurveyService.create(update.cleanForCopying());
    // after creating the new config, deactivate the old config
    existing.setActive(false);
    studyEnvironmentSurveyService.update(existing);
    eventService.publishSurveyPublishedEvent(
        authEntities.portalEnv.getId(), authEntities.studyEnv.getId(), authEntities.survey());
    return newConfig;
  }

  /**
   * confirms the user has access to the study and that the configured survey belongs to that study,
   * and that it's in the sandbox environment. Returns the study environment for which the change is
   * being made in.
   */
  protected AuthEntities authConfiguredSurveyRequest(
      String portalShortcode,
      EnvironmentName envName,
      String studyShortcode,
      StudyEnvironmentSurvey updatedObj,
      AdminUser operator) {
    authUtilService.authUserToStudy(operator, portalShortcode, studyShortcode);
    StudyEnvironment studyEnv = studyEnvironmentService.findByStudy(studyShortcode, envName).get();
    if (!EnvironmentName.sandbox.equals(envName)) {
      throw new IllegalArgumentException(
          "Updates can only be made directly to the sandbox environment".formatted(envName));
    }
    if (!studyEnv.getId().equals(updatedObj.getStudyEnvironmentId())) {
      throw new IllegalArgumentException(
          "Study environment id in request body does not belong to this study");
    }
    PortalEnvironment portalEnvironment =
        portalEnvironmentService.findOne(portalShortcode, envName).get();
    Survey survey = surveyService.find(updatedObj.getSurveyId()).get();
    if (!portalEnvironment.getPortalId().equals(survey.getPortalId())) {
      throw new IllegalArgumentException("Survey does not belong to the specified portal");
    }
    return new AuthEntities(studyEnv, portalEnvironment, survey);
  }

  private record AuthEntities(
      StudyEnvironment studyEnv, PortalEnvironment portalEnv, Survey survey) {}

  /** confirms the given config is associated with the given study */
  private void verifyStudyEnvironmentSurvey(
      StudyEnvironmentSurvey config, StudyEnvironment studyEnvironment) {
    if (!studyEnvironment.getId().equals(config.getStudyEnvironmentId())) {
      throw new IllegalArgumentException(
          "existing studyEnvironmentSurvey does not match the study environment");
    }
  }
}
