package bio.terra.pearl.api.admin.service.enrollee;

import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.EnforcePortalStudyEnvPermission;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyEnvAuthContext;
import bio.terra.pearl.core.model.search.EnrolleeSearchExpressionResult;
import bio.terra.pearl.core.model.search.SearchValueTypeDefinition;
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

  @EnforcePortalStudyEnvPermission(permission = "BASE")
  public Map<String, SearchValueTypeDefinition> getExpressionSearchFacets(
      PortalStudyEnvAuthContext authContext) {
    return this.enrolleeSearchService.getExpressionSearchFacetsForStudyEnv(
        authContext.getStudyEnvironment().getId());
  }

  @EnforcePortalStudyEnvPermission(permission = "participant_data_view")
  public List<EnrolleeSearchExpressionResult> executeSearchExpression(
      PortalStudyEnvAuthContext authContext,
      String expression,
      Integer limit,
      List<EnrolleeSearchOptions.Include> includes) {

    return this.enrolleeSearchService.executeSearchExpression(
        authContext.getStudyEnvironment().getId(),
        expression,
        EnrolleeSearchOptions.builder().limit(limit).includes(includes).build());
  }
}
