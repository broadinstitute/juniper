package bio.terra.pearl.api.admin.service.forms;

import bio.terra.pearl.api.admin.service.auth.*;
import bio.terra.pearl.api.admin.service.auth.context.PortalAuthContext;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyAuthContext;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyEnvAuthContext;
import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
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

  @EnforcePortalPermission(permission = AuthUtilService.BASE_PERMISSON)
  public Survey get(PortalAuthContext authContext, String stableId, int version) {
    Survey survey = authUtilService.authSurveyToPortal(authContext.getPortal(), stableId, version);
    surveyService.attachAnswerMappings(survey);
    return survey;
  }

  @EnforcePortalPermission(permission = AuthUtilService.BASE_PERMISSON)
  public List<Survey> listVersions(PortalAuthContext authContext, String stableId) {
    // This is used to populate the version selector in the admin UI. It's not necessary
    // to return the surveys with any content or answer mappings, the response will
    // be too large. Instead, just get the individual versions as content is needed.
    List<Survey> surveys = surveyService.findByStableIdNoContent(stableId);
    List<Survey> surveysInPortal =
        surveys.stream()
            .filter(survey -> authContext.getPortal().getId().equals(survey.getPortalId()))
            .toList();
    return surveysInPortal;
  }

  @EnforcePortalStudyPermission(permission = AuthUtilService.BASE_PERMISSON)
  public List<StudyEnvironmentSurvey> findWithSurveyNoContent(
      PortalStudyAuthContext authContext,
      String stableId,
      EnvironmentName envName,
      Boolean active) {
    List<UUID> studyEnvIds =
        studyEnvIds =
            studyEnvironmentService
                .findByStudy(authContext.getPortalStudy().getStudyId())
                // if no envName is specified, include all environments, otherwise just include the
                // specified one
                .stream()
                .filter(
                    studyEnv -> envName == null || studyEnv.getEnvironmentName().equals(envName))
                .map(StudyEnvironment::getId)
                .toList();
    return studyEnvironmentSurveyService.findAllWithSurveyNoContent(studyEnvIds, stableId, active);
  }

  @EnforcePortalPermission(permission = "survey_edit")
  public Survey create(PortalAuthContext authContext, Survey survey) {
    List<Survey> existing =
        surveyService.findByStableId(survey.getStableId(), authContext.getPortal().getId());
    if (existing.size() > 0) {
      throw new IllegalArgumentException("A survey with that stableId already exists");
    }
    // ensure the rule can be parsed (if null/empty, this will be a no-op)
    enrolleeSearchExpressionParser.parseRule(survey.getEligibilityRule());
    survey.setPortalId(authContext.getPortal().getId());
    survey.setVersion(1);
    return surveyService.create(survey);
  }

  @EnforcePortalPermission(permission = "survey_edit")
  @Transactional
  public void delete(PortalAuthContext authContext, String surveyStableId) {
    // Find all of the versions of the specified survey that are in the specified portal
    List<Survey> existingVersions =
        surveyService.findByStableId(surveyStableId, authContext.getPortal().getId());

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

  @EnforcePortalPermission(permission = "survey_edit")
  public Survey createNewVersion(PortalAuthContext authContext, Survey survey) {
    // ensure the rule can be parsed (if null/empty, this will be a no-op)
    enrolleeSearchExpressionParser.parseRule(survey.getEligibilityRule());
    return surveyService.createNewVersion(authContext.getPortal().getId(), survey);
  }

  @SandboxOnly
  @EnforcePortalStudyEnvPermission(permission = "survey_edit")
  public StudyEnvironmentSurvey createConfiguredSurvey(
      PortalStudyEnvAuthContext authContext, StudyEnvironmentSurvey surveyToConfigure) {
    SurveyAuthEntities surveyAuth = authConfiguredSurveyRequest(authContext, surveyToConfigure);
    StudyEnvironmentSurvey studyEnvSurvey = studyEnvironmentSurveyService.create(surveyToConfigure);
    eventService.publishSurveyPublishedEvent(
        surveyAuth.portalEnv.getId(),
        authContext.getStudyEnvironment().getId(),
        surveyAuth.survey());
    return studyEnvSurvey;
  }

  @SandboxOnly
  @EnforcePortalStudyEnvPermission(permission = "survey_edit")
  public void removeConfiguredSurvey(
      PortalStudyEnvAuthContext authContext, UUID configuredSurveyId) {
    StudyEnvironmentSurvey configuredSurvey =
        studyEnvironmentSurveyService.find(configuredSurveyId).get();
    authConfiguredSurveyRequest(authContext, configuredSurvey);
    studyEnvironmentSurveyService.deactivate(configuredSurveyId);
  }

  @SandboxOnly
  @EnforcePortalStudyEnvPermission(permission = "survey_edit")
  public StudyEnvironmentSurvey updateConfiguredSurvey(
      PortalStudyEnvAuthContext authContext, StudyEnvironmentSurvey updatedObj) {
    authConfiguredSurveyRequest(authContext, updatedObj);
    StudyEnvironmentSurvey existing = studyEnvironmentSurveyService.find(updatedObj.getId()).get();
    BeanUtils.copyProperties(updatedObj, existing);
    return studyEnvironmentSurveyService.update(existing);
  }

  /**
   * deactivates any existing active studyEnvironmentSurvey for the given stableId, and adds a new
   * config as specified in the update object. Note that the portalEnvironmentId and
   * studyEnvironmentId will be set from the portalShortcode and studyShortcode params, and the order will be
   * pulled from the prior active version.
   */
  @SandboxOnly
  @EnforcePortalStudyEnvPermission(permission = "survey_edit")
  @Transactional
  public StudyEnvironmentSurvey replace(
      PortalStudyEnvAuthContext authContext, StudyEnvironmentSurvey update) {
    SurveyAuthEntities authEntities = authConfiguredSurveyRequest(authContext, update);
    List<StudyEnvironmentSurvey> existingActives =
        studyEnvironmentSurveyService.findActiveBySurvey(
            authContext.getStudyEnvironment().getId(), authEntities.survey().getStableId());
    if (existingActives.size() == 0) {
      throw new NotFoundException("No active survey found for the given stableId");
    }
    existingActives.forEach(
        existing -> {
          existing.setActive(false);
          studyEnvironmentSurveyService.update(existing);
        });
    // preserve the surveyOrder from the existing active config
    update.setSurveyOrder(existingActives.get(0).getSurveyOrder());

    StudyEnvironmentSurvey newConfig =
        studyEnvironmentSurveyService.create(update.cleanForCopying());

    eventService.publishSurveyPublishedEvent(
        authEntities.portalEnv.getId(),
        authContext.getStudyEnvironment().getId(),
        authEntities.survey());
    return newConfig;
  }

  /**
   * confirms the user has access to the study and that the configured survey belongs to that study,
   * and that it's in the sandbox environment. Returns the study environment for which the change is
   * being made in.
   */
  protected SurveyAuthEntities authConfiguredSurveyRequest(
      PortalStudyEnvAuthContext authContext, StudyEnvironmentSurvey updatedObj) {

    if (!authContext.getStudyEnvironment().getId().equals(updatedObj.getStudyEnvironmentId())) {
      throw new IllegalArgumentException(
          "Study environment id in request body does not belong to this study");
    }
    PortalEnvironment portalEnvironment =
        portalEnvironmentService
            .findOne(authContext.getPortalShortcode(), authContext.getEnvironmentName())
            .get();
    Survey survey = surveyService.find(updatedObj.getSurveyId()).get();
    if (!portalEnvironment.getPortalId().equals(survey.getPortalId())) {
      throw new IllegalArgumentException("Survey does not belong to the specified portal");
    }
    return new SurveyAuthEntities(portalEnvironment, survey);
  }

  private record SurveyAuthEntities(PortalEnvironment portalEnv, Survey survey) {}

  /** confirms the given config is associated with the given study */
  private void verifyStudyEnvironmentSurvey(
      StudyEnvironmentSurvey config, StudyEnvironment studyEnvironment) {
    if (!studyEnvironment.getId().equals(config.getStudyEnvironmentId())) {
      throw new IllegalArgumentException(
          "existing studyEnvironmentSurvey does not match the study environment");
    }
  }
}
