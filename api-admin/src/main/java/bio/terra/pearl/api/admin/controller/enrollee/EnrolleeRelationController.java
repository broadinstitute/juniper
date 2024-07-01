package bio.terra.pearl.api.admin.controller.enrollee;

import bio.terra.pearl.api.admin.api.EnrolleeRelationApi;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyEnvAuthContext;
import bio.terra.pearl.api.admin.service.enrollee.EnrolleeRelationExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.participant.EnrolleeRelation;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
@Slf4j
public class EnrolleeRelationController implements EnrolleeRelationApi {
  private final AuthUtilService authUtilService;
  private final EnrolleeRelationExtService enrolleeRelationExtService;
  private final HttpServletRequest request;
  private final ObjectMapper objectMapper;

  public EnrolleeRelationController(
      AuthUtilService authUtilService,
      EnrolleeRelationExtService enrolleeRelationExtService,
      ObjectMapper objectMapper,
      HttpServletRequest request) {
    this.authUtilService = authUtilService;
    this.enrolleeRelationExtService = enrolleeRelationExtService;
    this.objectMapper = objectMapper;
    this.request = request;
  }

  @Override
  public ResponseEntity<Object> findRelationsForTargetEnrollee(
      String portalShortcode, String studyShortcode, String envName, String enrolleeShortcode) {
    AdminUser adminUser = authUtilService.requireAdminUser(request);
    List<EnrolleeRelation> relations =
        enrolleeRelationExtService.findRelationsForTargetEnrollee(
                PortalStudyEnvAuthContext.of(
                        adminUser,
                        portalShortcode,
                        studyShortcode,
                        EnvironmentName.valueOfCaseInsensitive(envName)),
            enrolleeShortcode);
    return ResponseEntity.ok(relations);
  }

  @Override
  public ResponseEntity<Object> create(
      String justification,
      String portalShortcode,
      String studyShortcode,
      String envName,
      Object body) {
    EnrolleeRelation relation = objectMapper.convertValue(body, EnrolleeRelation.class);
    AdminUser adminUser = authUtilService.requireAdminUser(request);
    EnrolleeRelation createdRelation =
        enrolleeRelationExtService.create(
            PortalStudyEnvAuthContext.of(
                adminUser,
                portalShortcode,
                studyShortcode,
                EnvironmentName.valueOfCaseInsensitive(envName)),
            relation,
            justification);
    return ResponseEntity.ok(createdRelation);
  }

  @Override
  public ResponseEntity<Void> delete(
      String portalShortcode,
      String studyShortcode,
      String envName,
      UUID enrolleeRelationId,
      String justification) {
    AdminUser adminUser = authUtilService.requireAdminUser(request);
    enrolleeRelationExtService.delete(
        PortalStudyEnvAuthContext.of(
            adminUser,
            portalShortcode,
            studyShortcode,
            EnvironmentName.valueOfCaseInsensitive(envName)),
        enrolleeRelationId,
        justification);
    return ResponseEntity.noContent().build();
  }
}
