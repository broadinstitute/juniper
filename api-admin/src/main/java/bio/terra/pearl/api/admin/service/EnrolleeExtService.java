package bio.terra.pearl.api.admin.service;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.EnrolleeSearchResult;
import bio.terra.pearl.core.model.participant.WithdrawnEnrollee;
import bio.terra.pearl.core.model.study.PortalStudy;
import bio.terra.pearl.core.model.workflow.DataChangeRecord;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import bio.terra.pearl.core.service.kit.KitRequestService;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.WithdrawnEnrolleeService;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.study.PortalStudyService;
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
  private PortalService portalService;
  private PortalStudyService portalStudyService;
  private DataChangeRecordService dataChangeRecordService;
  private KitRequestService kitRequestService;

  public EnrolleeExtService(
      AuthUtilService authUtilService,
      EnrolleeService enrolleeService,
      WithdrawnEnrolleeService withdrawnEnrolleeService,
      PortalService portalService,
      PortalStudyService portalStudyService,
      DataChangeRecordService dataChangeRecordService,
      KitRequestService kitRequestService) {
    this.authUtilService = authUtilService;
    this.enrolleeService = enrolleeService;
    this.withdrawnEnrolleeService = withdrawnEnrolleeService;
    this.portalService = portalService;
    this.portalStudyService = portalStudyService;
    this.dataChangeRecordService = dataChangeRecordService;
    this.kitRequestService = kitRequestService;
  }

  public List<EnrolleeSearchResult> search(
      AdminUser user,
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName) {
    authUtilService.authUserToStudy(user, portalShortcode, studyShortcode);
    return enrolleeService.search(studyShortcode, environmentName);
  }

  public Enrollee findWithAdminLoad(AdminUser user, String enrolleeShortcode) {
    Enrollee enrollee = authAdminUserToEnrollee(user, enrolleeShortcode);
    return enrolleeService.loadForAdminView(enrollee);
  }

  public List<DataChangeRecord> findDataChangeRecords(AdminUser user, String enrolleeShortcode) {
    Enrollee enrollee = authAdminUserToEnrollee(user, enrolleeShortcode);
    return dataChangeRecordService.findByEnrollee(enrollee.getId());
  }

  public WithdrawnEnrollee withdrawEnrollee(AdminUser user, String enroleeShortcode)
      throws JsonProcessingException {
    if (!user.isSuperuser()) {
      throw new PermissionDeniedException("Not authoried to withdraw participants");
    }
    Enrollee enrollee = authAdminUserToEnrollee(user, enroleeShortcode);
    return withdrawnEnrolleeService.withdrawEnrollee(enrollee);
  }

  public KitRequest requestKit(AdminUser user, String enrolleeShortcode, String kitType) throws JsonProcessingException {
    Enrollee enrollee = authAdminUserToEnrollee(user, enrolleeShortcode);
    return kitRequestService.requestKit(enrollee, kitType);
  }

  /**
   * returns the enrollee if the user is authorized to access/modify it, throws an error otherwise
   */
  public Enrollee authAdminUserToEnrollee(AdminUser user, String enrolleeShortcode) {
    // find what portal(s) the enrollee is in, and then check that the adminUser is authorized in at
    // least one
    List<PortalStudy> portalStudies = portalStudyService.findByEnrollee(enrolleeShortcode);
    List<UUID> portalIds = portalStudies.stream().map(PortalStudy::getPortalId).toList();
    if (!portalService.checkAdminInAtLeastOnePortal(user, portalIds)) {
      throw new PermissionDeniedException(
          "User %s does not have permissions on enrollee %s or enrollee does not exist"
              .formatted(user.getUsername(), enrolleeShortcode));
    }
    return enrolleeService.findOneByShortcode(enrolleeShortcode).get();
  }
}
