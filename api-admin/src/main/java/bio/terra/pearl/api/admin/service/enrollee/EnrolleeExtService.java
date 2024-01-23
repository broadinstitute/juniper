package bio.terra.pearl.api.admin.service.enrollee;

import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.EnrolleeSearchFacet;
import bio.terra.pearl.core.model.participant.EnrolleeSearchResult;
import bio.terra.pearl.core.model.participant.WithdrawnEnrollee;
import bio.terra.pearl.core.model.workflow.DataChangeRecord;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.ProfileService;
import bio.terra.pearl.core.service.participant.WithdrawnEnrolleeService;
import bio.terra.pearl.core.service.participant.search.EnrolleeSearchService;
import bio.terra.pearl.core.service.participant.search.facets.sql.SqlSearchableFacet;
import bio.terra.pearl.core.service.workflow.DataChangeRecordService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnrolleeExtService {
  private AuthUtilService authUtilService;
  private EnrolleeService enrolleeService;
  private WithdrawnEnrolleeService withdrawnEnrolleeService;
  private DataChangeRecordService dataChangeRecordService;
  private EnrolleeSearchService enrolleeSearchService;
  private ProfileService profileService;

  public EnrolleeExtService(
      AuthUtilService authUtilService,
      EnrolleeService enrolleeService,
      WithdrawnEnrolleeService withdrawnEnrolleeService,
      DataChangeRecordService dataChangeRecordService,
      EnrolleeSearchService enrolleeSearchService,
      ProfileService profileService) {
    this.authUtilService = authUtilService;
    this.enrolleeService = enrolleeService;
    this.withdrawnEnrolleeService = withdrawnEnrolleeService;
    this.dataChangeRecordService = dataChangeRecordService;
    this.enrolleeSearchService = enrolleeSearchService;
    this.profileService = profileService;
  }

  public List<EnrolleeSearchResult> search(
      AdminUser operator,
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName,
      List<SqlSearchableFacet> facets) {
    authUtilService.authUserToStudy(operator, portalShortcode, studyShortcode);
    return enrolleeSearchService.search(studyShortcode, environmentName, facets);
  }

  public List<EnrolleeSearchFacet> getSearchFacets(
      AdminUser operator,
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName) {
    authUtilService.authUserToStudy(operator, portalShortcode, studyShortcode);
    return enrolleeSearchService.getFacets(studyShortcode, environmentName);
  }

  public List<Enrollee> findForKitManagement(
      AdminUser operator,
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName) {
    authUtilService.authUserToStudy(operator, portalShortcode, studyShortcode);
    return enrolleeService.findForKitManagement(studyShortcode, environmentName);
  }

  public Enrollee findWithAdminLoad(AdminUser operator, String enrolleeShortcode) {
    Enrollee enrollee = authUtilService.authAdminUserToEnrollee(operator, enrolleeShortcode);
    return enrolleeService.loadForAdminView(enrollee);
  }

  public List<DataChangeRecord> findDataChangeRecords(
      AdminUser operator, String enrolleeShortcode) {
    Enrollee enrollee = authUtilService.authAdminUserToEnrollee(operator, enrolleeShortcode);
    return dataChangeRecordService.findByEnrollee(enrollee.getId());
  }

  public WithdrawnEnrollee withdrawEnrollee(AdminUser operator, String enrolleeShortcode)
      throws JsonProcessingException {
    Enrollee enrollee = authUtilService.authAdminUserToEnrollee(operator, enrolleeShortcode);
    return withdrawnEnrolleeService.withdrawEnrollee(enrollee);
  }
}
