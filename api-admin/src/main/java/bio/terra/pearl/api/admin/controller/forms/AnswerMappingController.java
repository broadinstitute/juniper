package bio.terra.pearl.api.admin.controller.forms;

import bio.terra.pearl.api.admin.api.AnswerMappingApi;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.api.admin.service.forms.AnswerMappingExtService;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.survey.AnswerMapping;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class AnswerMappingController implements AnswerMappingApi {
  private final AuthUtilService authUtilService;
  private final ObjectMapper objectMapper;
  private final HttpServletRequest request;
  private final AnswerMappingExtService answerMappingExtService;

  public AnswerMappingController(
      AuthUtilService authUtilService,
      ObjectMapper objectMapper,
      HttpServletRequest request,
      AnswerMappingExtService answerMappingExtService) {
    this.authUtilService = authUtilService;
    this.objectMapper = objectMapper;
    this.request = request;
    this.answerMappingExtService = answerMappingExtService;
  }

  @Override
  public ResponseEntity<Object> findBySurvey(
      String portalShortcode, String stableId, Integer version) {
    AdminUser adminUser = authUtilService.requireAdminUser(request);
    return ResponseEntity.ok(
        answerMappingExtService.findBySurvey(adminUser, portalShortcode, stableId, version));
  }

  @Override
  public ResponseEntity<Object> create(
      String portalShortcode, String stableId, Integer version, Object body) {
    AdminUser adminUser = authUtilService.requireAdminUser(request);
    AnswerMapping mapping = objectMapper.convertValue(body, AnswerMapping.class);

    return ResponseEntity.ok(
        answerMappingExtService.createAnswerMappingForSurvey(
            adminUser, mapping, portalShortcode, stableId, version));
  }

  @Override
  public ResponseEntity<Void> delete(
      String portalShortcode, String stableId, Integer version, UUID answerMappingId) {
    AdminUser adminUser = authUtilService.requireAdminUser(request);

    answerMappingExtService.deleteAnswerMapping(
        adminUser, portalShortcode, stableId, version, answerMappingId);
    return ResponseEntity.noContent().build();
  }
}
