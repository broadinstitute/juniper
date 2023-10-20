package bio.terra.pearl.api.admin.controller.study;

import bio.terra.pearl.api.admin.api.StudyApi;
import bio.terra.pearl.api.admin.model.ErrorReport;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.api.admin.service.StudyExtService;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.service.study.StudyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.http.HttpServletRequest;
import org.postgresql.util.PSQLException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;

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
  public ResponseEntity<Object> getKitTypes(String portalShortcode, String studyShortcode) {
    AdminUser adminUser = requestService.requireAdminUser(request);
    var kitTypes = studyExtService.getKitTypes(adminUser, portalShortcode, studyShortcode);
    return ResponseEntity.ok(kitTypes);
  }

  @ExceptionHandler({PSQLException.class})
  public ResponseEntity<ErrorReport> handleDatabaseError(PSQLException e) {
    if (e.getMessage().contains("study_shortcode_key")) {
      return ResponseEntity.badRequest()
          .body(new ErrorReport().message("A study with that shortcode already exists"));
    } else
      return ResponseEntity.internalServerError().body(new ErrorReport().message(e.getMessage()));
  }

  @Override
  public ResponseEntity<Object> create(String portalShortcode, Object body) {
    AdminUser operator = requestService.requireAdminUser(request);
    var studyDto = objectMapper.convertValue(body, StudyExtService.StudyCreationDto.class);
    Study study = studyExtService.create(portalShortcode, studyDto, operator);
    return ResponseEntity.ok(study);
  }
}
