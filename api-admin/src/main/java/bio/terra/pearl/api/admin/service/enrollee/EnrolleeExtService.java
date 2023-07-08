package bio.terra.pearl.api.admin.service.enrollee;

import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.EnrolleeSearchResult;
import bio.terra.pearl.core.model.participant.WithdrawnEnrollee;
import bio.terra.pearl.core.model.workflow.DataChangeRecord;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import bio.terra.pearl.core.service.kit.KitRequestService;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.WithdrawnEnrolleeService;
import bio.terra.pearl.core.service.participant.search.EnrolleeSearchService;
import bio.terra.pearl.core.service.participant.search.facets.sql.SqlSearchableFacet;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.study.PortalStudyService;
import bio.terra.pearl.core.service.workflow.DataChangeRecordService;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class EnrolleeExtService {
  private AuthUtilService authUtilService;
  private EnrolleeService enrolleeService;
  private WithdrawnEnrolleeService withdrawnEnrolleeService;
  private PortalService portalService;
  private PortalStudyService portalStudyService;
  private DataChangeRecordService dataChangeRecordService;
  private KitRequestService kitRequestService;

  private EnrolleeSearchService enrolleeSearchService;

  public EnrolleeExtService(
      AuthUtilService authUtilService,
      EnrolleeService enrolleeService,
      WithdrawnEnrolleeService withdrawnEnrolleeService,
      PortalService portalService,
      PortalStudyService portalStudyService,
      DataChangeRecordService dataChangeRecordService,
      KitRequestService kitRequestService,
      EnrolleeSearchService enrolleeSearchService) {
    this.authUtilService = authUtilService;
    this.enrolleeService = enrolleeService;
    this.withdrawnEnrolleeService = withdrawnEnrolleeService;
    this.portalService = portalService;
    this.portalStudyService = portalStudyService;
    this.dataChangeRecordService = dataChangeRecordService;
    this.kitRequestService = kitRequestService;
    this.enrolleeSearchService = enrolleeSearchService;
  }

  public List<EnrolleeSearchResult> search(
      AdminUser user,
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName,
      List<SqlSearchableFacet> facets) {
    authUtilService.authUserToStudy(user, portalShortcode, studyShortcode);
    return enrolleeSearchService.search(studyShortcode, environmentName, facets);
  }

  public Enrollee findWithAdminLoad(AdminUser user, String enrolleeShortcode) {
    Enrollee enrollee = authUtilService.authAdminUserToEnrollee(user, enrolleeShortcode);
    return enrolleeService.loadForAdminView(enrollee);
  }

  public List<DataChangeRecord> findDataChangeRecords(AdminUser user, String enrolleeShortcode) {
    Enrollee enrollee = authUtilService.authAdminUserToEnrollee(user, enrolleeShortcode);
    return dataChangeRecordService.findByEnrollee(enrollee.getId());
  }

  public WithdrawnEnrollee withdrawEnrollee(AdminUser user, String enroleeShortcode)
      throws JsonProcessingException {
    if (!user.isSuperuser()) {
      throw new PermissionDeniedException("Not authoried to withdraw participants");
    }
    Enrollee enrollee = authUtilService.authAdminUserToEnrollee(user, enroleeShortcode);
    return withdrawnEnrolleeService.withdrawEnrollee(enrollee);
  }

  public KitRequest requestKit(AdminUser adminUser, String enrolleeShortcode, String kitTypeName)
      throws JsonProcessingException {
    Enrollee enrollee = authUtilService.authAdminUserToEnrollee(adminUser, enrolleeShortcode);
    return kitRequestService.requestKit(adminUser, enrollee, kitTypeName);
  }

  public Collection<KitRequest> getKitRequests(AdminUser adminUser, String enrolleeShortcode) {
    Enrollee enrollee = authUtilService.authAdminUserToEnrollee(adminUser, enrolleeShortcode);
    return kitRequestService.getKitRequests(adminUser, enrollee);
  }
}
