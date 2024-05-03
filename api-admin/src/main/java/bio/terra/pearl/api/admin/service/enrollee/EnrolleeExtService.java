package bio.terra.pearl.api.admin.service.enrollee;

import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.audit.DataChangeRecord;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.EnrolleeSearchFacet;
import bio.terra.pearl.core.model.participant.EnrolleeSearchResult;
import bio.terra.pearl.core.model.participant.WithdrawnEnrollee;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.WithdrawnEnrolleeService;
import bio.terra.pearl.core.service.participant.search.EnrolleeSearchService;
import bio.terra.pearl.core.service.participant.search.facets.BooleanFacetValue;
import bio.terra.pearl.core.service.participant.search.facets.sql.EnrolleeFacetSqlGenerator;
import bio.terra.pearl.core.service.participant.search.facets.sql.SqlSearchableFacet;
import bio.terra.pearl.core.service.workflow.DataChangeRecordService;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class EnrolleeExtService {
  private AuthUtilService authUtilService;
  private EnrolleeService enrolleeService;
  private WithdrawnEnrolleeService withdrawnEnrolleeService;
  private DataChangeRecordService dataChangeRecordService;
  private EnrolleeSearchService enrolleeSearchService;

  public EnrolleeExtService(
      AuthUtilService authUtilService,
      EnrolleeService enrolleeService,
      WithdrawnEnrolleeService withdrawnEnrolleeService,
      DataChangeRecordService dataChangeRecordService,
      EnrolleeSearchService enrolleeSearchService) {
    this.authUtilService = authUtilService;
    this.enrolleeService = enrolleeService;
    this.withdrawnEnrolleeService = withdrawnEnrolleeService;
    this.dataChangeRecordService = dataChangeRecordService;
    this.enrolleeSearchService = enrolleeSearchService;
  }

  public List<EnrolleeSearchResult> search(
      AdminUser operator,
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName,
      List<SqlSearchableFacet> facets) {
    authUtilService.authUserToStudy(operator, portalShortcode, studyShortcode);
    // for now, we're hardcoded to always limit the search to subjects (e.g. don't return proxies)
    facets.add(
        new SqlSearchableFacet(
            new BooleanFacetValue("subject", true), new EnrolleeFacetSqlGenerator()));
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

  public Enrollee findWithAdminLoad(AdminUser operator, String enrolleeShortcodeOrId) {
    String enrolleeShortcode = enrolleeShortcodeOrId;
    if (enrolleeShortcode != null && enrolleeShortcode.length() > 16) {
      // it's an id, not a shortcode
      enrolleeShortcode =
          enrolleeService
              .find(UUID.fromString(enrolleeShortcode))
              .orElseThrow(
                  () ->
                      new NotFoundException(
                          "User %s does not have permissions on enrollee %s or enrollee does not exist"
                              .formatted(operator.getUsername(), enrolleeShortcodeOrId)))
              .getShortcode();
    }
    Enrollee enrollee = authUtilService.authAdminUserToEnrollee(operator, enrolleeShortcode);
    return enrolleeService.loadForAdminView(enrollee);
  }

  public List<DataChangeRecord> findDataChangeRecords(
      AdminUser operator, String enrolleeShortcode) {
    Enrollee enrollee = authUtilService.authAdminUserToEnrollee(operator, enrolleeShortcode);
    return dataChangeRecordService.findAllRecordsForEnrollee(enrollee);
  }

  public WithdrawnEnrollee withdrawEnrollee(AdminUser operator, String enrolleeShortcode)
      throws JsonProcessingException {
    Enrollee enrollee = authUtilService.authAdminUserToEnrollee(operator, enrolleeShortcode);
    return withdrawnEnrolleeService.withdrawEnrollee(enrollee);
  }
}
