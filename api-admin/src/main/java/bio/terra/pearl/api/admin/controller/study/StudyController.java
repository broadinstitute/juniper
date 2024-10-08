package bio.terra.pearl.api.admin.controller.study;

import bio.terra.pearl.api.admin.api.StudyApi;
import bio.terra.pearl.api.admin.models.dto.StudyCreationDto;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.context.PortalAuthContext;
import bio.terra.pearl.api.admin.service.study.StudyExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.service.study.StudyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class StudyController implements StudyApi {
  private final AuthUtilService requestService;
  private final HttpServletRequest request;
  private final StudyExtService studyExtService;
  private final ObjectMapper objectMapper;

  public StudyController(
      AuthUtilService requestService,
      HttpServletRequest request,
      StudyExtService studyExtService,
      StudyService studyService,
      ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    this.requestService = requestService;
    this.request = request;
    this.studyExtService = studyExtService;
  }

  @Override
  public ResponseEntity<Object> create(String portalShortcode, Object body) {
    AdminUser operator = requestService.requireAdminUser(request);
    StudyCreationDto studyDto = objectMapper.convertValue(body, StudyCreationDto.class);
    Study study = studyExtService.create(portalShortcode, studyDto, operator);
    return ResponseEntity.ok(study);
  }

  @Override
  public ResponseEntity<Void> delete(String portalShortCode, String studyShortcode) {
    AdminUser operator = requestService.requireAdminUser(request);
    studyExtService.delete(portalShortCode, studyShortcode, operator);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Object> getByPortalWithEnvs(String portalShortcode, String envName) {
    AdminUser adminUser = requestService.requireAdminUser(request);
    List<Study> studies =
        studyExtService.getStudiesWithEnvs(
            PortalAuthContext.of(adminUser, portalShortcode),
            EnvironmentName.valueOfCaseInsensitive(envName));
    return ResponseEntity.ok(studies);
  }
}
