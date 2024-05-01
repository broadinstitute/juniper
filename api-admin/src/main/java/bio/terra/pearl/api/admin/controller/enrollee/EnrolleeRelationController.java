package bio.terra.pearl.api.admin.controller.enrollee;

import bio.terra.pearl.api.admin.api.EnrolleeRelationApi;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.api.admin.service.enrollee.EnrolleeRelationExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.participant.EnrolleeRelation;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class EnrolleeRelationController implements EnrolleeRelationApi {
  private AuthUtilService authUtilService;
  private EnrolleeRelationExtService enrolleeRelationExtService;
  private HttpServletRequest request;
  private ObjectMapper objectMapper;

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
  public ResponseEntity<Object> findRelationsByTargetIdWithEnrollees(
      String portalShortcode, String studyShortcode, String envName, String enrolleeShortcode) {
    AdminUser adminUser = authUtilService.requireAdminUser(request);
    List<EnrolleeRelation> relations =
        enrolleeRelationExtService.findRelationsByTargetIdWithEnrollees(
            portalShortcode,
            studyShortcode,
            EnvironmentName.valueOfCaseInsensitive(envName),
            adminUser,
            enrolleeShortcode);
    return ResponseEntity.ok(relations);
  }
}
