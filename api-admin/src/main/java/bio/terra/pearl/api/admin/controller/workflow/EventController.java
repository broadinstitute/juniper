package bio.terra.pearl.api.admin.controller.workflow;

import bio.terra.pearl.api.admin.api.EventApi;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.context.PortalEnrolleeAuthContext;
import bio.terra.pearl.api.admin.service.workflow.EventExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.workflow.Event;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class EventController implements EventApi {
  private final AuthUtilService authUtilService;
  private final HttpServletRequest request;
  private final EventExtService eventExtService;

  public EventController(
      AuthUtilService authUtilService,
      HttpServletRequest request,
      EventExtService eventExtService) {
    this.authUtilService = authUtilService;
    this.request = request;
    this.eventExtService = eventExtService;
  }

  @Override
  public ResponseEntity<Object> getEventsByEnrollee(
      String portalShortcode, String studyShortcode, String envName, String enrolleeShortcode) {
    AdminUser adminUser = authUtilService.requireAdminUser(request);
    List<Event> events =
        eventExtService.findAllByEnrollee(
            PortalEnrolleeAuthContext.of(
                adminUser,
                portalShortcode,
                studyShortcode,
                EnvironmentName.valueOfCaseInsensitive(envName),
                enrolleeShortcode));
    return ResponseEntity.ok(events);
  }
}
