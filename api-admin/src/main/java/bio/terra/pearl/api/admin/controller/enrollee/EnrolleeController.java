package bio.terra.pearl.api.admin.controller.enrollee;

import bio.terra.pearl.api.admin.api.EnrolleeApi;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.context.PortalEnrolleeAuthContext;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyEnvAuthContext;
import bio.terra.pearl.api.admin.service.enrollee.EnrolleeExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.audit.ParticipantDataChange;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.EnrolleeWithdrawalReason;
import bio.terra.pearl.core.model.participant.WithdrawnEnrollee;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class EnrolleeController implements EnrolleeApi {
  private AuthUtilService authUtilService;
  private EnrolleeExtService enrolleeExtService;
  private HttpServletRequest request;
  private ObjectMapper objectMapper;

  public EnrolleeController(
      AuthUtilService authUtilService,
      EnrolleeExtService enrolleeExtService,
      ObjectMapper objectMapper,
      HttpServletRequest request) {
    this.authUtilService = authUtilService;
    this.enrolleeExtService = enrolleeExtService;
    this.objectMapper = objectMapper;
    this.request = request;
  }

  @Override
  public ResponseEntity<Object> find(
      String portalShortcode, String studyShortcode, String envName, String enrolleeShortcodeOrId) {
    AdminUser adminUser = authUtilService.requireAdminUser(request);
    Enrollee enrollee =
        enrolleeExtService.findWithAdminLoad(
            PortalEnrolleeAuthContext.of(
                adminUser,
                portalShortcode,
                studyShortcode,
                EnvironmentName.valueOf(envName),
                enrolleeShortcodeOrId));
    return ResponseEntity.ok(enrollee);
  }

  @Override
  public ResponseEntity<Object> listChangeRecords(
      String portalShortcode,
      String studyShortcode,
      String envName,
      String enrolleeShortcode,
      String modelName) {
    AdminUser adminUser = authUtilService.requireAdminUser(request);
    List<ParticipantDataChange> records =
        enrolleeExtService.findDataChangeRecords(
            PortalEnrolleeAuthContext.of(
                adminUser,
                portalShortcode,
                studyShortcode,
                EnvironmentName.valueOf(envName),
                enrolleeShortcode),
            modelName);
    return ResponseEntity.ok(records);
  }

  @Override
  public ResponseEntity<Object> withdraw(
      String portalShortcode,
      String studyShortcode,
      String envName,
      String enrolleeShortcode,
      Object body) {
    AdminUser adminUser = authUtilService.requireAdminUser(request);
    WithdrawalParams params = objectMapper.convertValue(body, WithdrawalParams.class);
    try {
      WithdrawnEnrollee withdrawn =
          enrolleeExtService.withdrawEnrollee(
              PortalEnrolleeAuthContext.of(
                  adminUser,
                  portalShortcode,
                  studyShortcode,
                  EnvironmentName.valueOf(envName),
                  enrolleeShortcode),
              params.reason,
              params.note);
      return ResponseEntity.ok(new WithdrawnResponse(withdrawn.getId()));
    } catch (JsonProcessingException e) {
      return ResponseEntity.internalServerError().body(e.getMessage());
    }
  }

  @Override
  public ResponseEntity<Object> enrolleesWithKits(
      String portalShortcode, String studyShortcode, String envName) {
    AdminUser adminUser = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);

    List<Enrollee> enrollees =
        enrolleeExtService.findForKitManagement(
            PortalStudyEnvAuthContext.of(
                adminUser, portalShortcode, studyShortcode, environmentName));
    return ResponseEntity.ok(enrollees);
  }

  public record WithdrawnResponse(UUID withdrawnEnrolleeId) {}

  public record WithdrawalParams(EnrolleeWithdrawalReason reason, String note) {}
}
