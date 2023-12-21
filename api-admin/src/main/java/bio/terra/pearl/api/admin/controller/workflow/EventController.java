package bio.terra.pearl.api.admin.controller.workflow;

import bio.terra.pearl.api.admin.api.EventApi;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.workflow.Event;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.workflow.EventService;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class EventController implements EventApi {
  private final AuthUtilService authUtilService;
  private final HttpServletRequest request;
  private final EventService eventService;
  private final EnrolleeService enrolleeService;

  public EventController(
      AuthUtilService authUtilService,
      HttpServletRequest request,
      EventService eventService,
      EnrolleeService enrolleeService) {
    this.authUtilService = authUtilService;
    this.request = request;
    this.eventService = eventService;
    this.enrolleeService = enrolleeService;
  }

  @Override
  public ResponseEntity<Object> getEventsByEnrollee(
      String portalShortcode, String studyShortcode, String envName, String enrolleeShortcode) {
    AdminUser adminUser = authUtilService.requireAdminUser(request);
    authUtilService.authUserToStudy(adminUser, portalShortcode, studyShortcode);
    Enrollee enrollee = enrolleeService.findOneByShortcode(enrolleeShortcode).get();
    List<Event> events = eventService.findAllEventsByEnrolleeId(enrollee.getId());
    return ResponseEntity.ok(events);
  }
}
