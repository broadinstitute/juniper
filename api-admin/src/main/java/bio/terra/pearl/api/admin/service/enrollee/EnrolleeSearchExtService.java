package bio.terra.pearl.api.admin.service.enrollee;

import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.search.EnrolleeSearchExpressionResult;
import bio.terra.pearl.core.model.search.SearchValueTypeDefinition;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.WithdrawnEnrolleeService;
import bio.terra.pearl.core.service.participant.search.EnrolleeSearchService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.workflow.DataChangeRecordService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class EnrolleeSearchExtService {
  private AuthUtilService authUtilService;
  private EnrolleeService enrolleeService;
  private WithdrawnEnrolleeService withdrawnEnrolleeService;
  private DataChangeRecordService dataChangeRecordService;
  private EnrolleeSearchService enrolleeSearchService;
  private StudyEnvironmentService studyEnvironmentService;

  public EnrolleeSearchExtService(
      AuthUtilService authUtilService,
      EnrolleeService enrolleeService,
      WithdrawnEnrolleeService withdrawnEnrolleeService,
      DataChangeRecordService dataChangeRecordService,
      EnrolleeSearchService enrolleeSearchService,
      StudyEnvironmentService studyEnvironmentService) {
    this.authUtilService = authUtilService;
    this.enrolleeService = enrolleeService;
    this.withdrawnEnrolleeService = withdrawnEnrolleeService;
    this.dataChangeRecordService = dataChangeRecordService;
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
      String expression) {

    authUtilService.authUserToStudy(operator, portalShortcode, studyShortcode);

    StudyEnvironment studyEnvironment =
        studyEnvironmentService
            .findByStudy(studyShortcode, envName)
            .orElseThrow(() -> new IllegalStateException("Study environment not found"));

    return this.enrolleeSearchService.executeSearchExpression(studyEnvironment.getId(), expression);
  }
}
