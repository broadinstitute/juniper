package bio.terra.pearl.api.admin.service.enrollee;

import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.study.PortalStudy;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.WithdrawnEnrolleeService;
import bio.terra.pearl.core.service.participant.search.EnrolleeSearchService;
import bio.terra.pearl.core.service.search.terms.SearchValue;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.workflow.DataChangeRecordService;
import java.util.Map;
import org.springframework.stereotype.Service;

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

  public Map<String, SearchValue.SearchValueType> getExpressionSearchFacets(
      AdminUser operator, String portalShortcode, String studyShortcode, EnvironmentName envName) {
    PortalStudy portalStudy =
        authUtilService.authUserToStudy(operator, portalShortcode, studyShortcode);

    StudyEnvironment studyEnvironment =
        studyEnvironmentService
            .findByStudy(studyShortcode, envName)
            .orElseThrow(() -> new NotFoundException("Study environment not found"));

    return this.enrolleeSearchService.getExpressionSearchFacetsForStudyEnv(
        studyEnvironment.getId());
  }
}
