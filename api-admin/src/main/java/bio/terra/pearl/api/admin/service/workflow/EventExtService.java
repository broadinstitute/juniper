package bio.terra.pearl.api.admin.service.workflow;

import bio.terra.pearl.api.admin.service.auth.EnforcePortalEnrolleePermission;
import bio.terra.pearl.api.admin.service.auth.context.PortalEnrolleeAuthContext;
import bio.terra.pearl.core.model.workflow.Event;
import bio.terra.pearl.core.service.workflow.EventService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class EventExtService {
  private final EventService eventService;

  public EventExtService(EventService eventService) {
    this.eventService = eventService;
  }

  @EnforcePortalEnrolleePermission(permission = "participant_data_view")
  public List<Event> findAllByEnrollee(PortalEnrolleeAuthContext authContext) {
    return eventService.findAllEventsByEnrolleeId(authContext.getEnrollee().getId());
  }
}
