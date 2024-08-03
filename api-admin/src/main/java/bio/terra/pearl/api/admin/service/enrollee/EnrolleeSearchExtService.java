package bio.terra.pearl.api.admin.service.enrollee;

import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.search.EnrolleeSearchExpressionResult;
import bio.terra.pearl.core.model.search.SearchValueTypeDefinition;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.search.EnrolleeSearchOptions;
import bio.terra.pearl.core.service.search.EnrolleeSearchService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class EnrolleeSearchExtService {
  private final AuthUtilService authUtilService;
  private final EnrolleeSearchService enrolleeSearchService;
  private final StudyEnvironmentService studyEnvironmentService;

  public EnrolleeSearchExtService(
      AuthUtilService authUtilService,
      EnrolleeSearchService enrolleeSearchService,
      StudyEnvironmentService studyEnvironmentService) {
    this.authUtilService = authUtilService;
    this.enrolleeSearchService = enrolleeSearchService;
    this.studyEnvironmentService = studyEnvironmentService;
  }

  public Map<String, SearchValueTypeDefinition> getExpressionSearchFacets(
      AdminUser operator, String portalShortcode, String studyShortcode, EnvironmentName envName) {
    authUtilService.authUserToStudy(operator, portalShortcode, studyShortcode);

    StudyEnvironment studyEnvironment =
        studyEnvironmentService
            .findByStudy(studyShortcode, envName)
            .orElseThrow(() -> new IllegalStateException("Study environment not found"));

    return this.enrolleeSearchService.getExpressionSearchFacetsForStudyEnv(
        studyEnvironment.getId());
  }

  public List<EnrolleeSearchExpressionResult> executeSearchExpression(
      AdminUser operator,
      String portalShortcode,
      String studyShortcode,
      EnvironmentName envName,
      String expression,
      Integer limit) {

    authUtilService.authUserToStudy(operator, portalShortcode, studyShortcode);

    StudyEnvironment studyEnvironment =
        studyEnvironmentService
            .findByStudy(studyShortcode, envName)
            .orElseThrow(() -> new IllegalStateException("Study environment not found"));

    return this.enrolleeSearchService.executeSearchExpression(
        studyEnvironment.getId(), expression, EnrolleeSearchOptions.builder().limit(limit).build());
  }
}
