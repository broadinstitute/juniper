package bio.terra.pearl.api.admin.controller.portal;

import bio.terra.pearl.api.admin.api.PortalApi;
import bio.terra.pearl.api.admin.model.PortalShallowDto;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.api.admin.service.portal.PortalExtService;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.Portal;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class PortalController implements PortalApi {
  private PortalExtService portalExtService;
  private ObjectMapper objectMapper;
  private AuthUtilService requestService;
  private final HttpServletRequest request;

  public PortalController(
      PortalExtService portalExtService,
      ObjectMapper objectMapper,
      AuthUtilService requestService,
      HttpServletRequest request) {
    this.portalExtService = portalExtService;
    this.objectMapper = objectMapper;
    this.requestService = requestService;
    this.request = request;
  }

  @Override
  public ResponseEntity<Object> get(String portalShortcode) {
    AdminUser adminUser = requestService.requireAdminUser(request);
    Portal portal = portalExtService.fullLoad(adminUser, portalShortcode);
    return ResponseEntity.ok(portal);
  }

  @Override
  public ResponseEntity<List<PortalShallowDto>> getAll() {
    AdminUser adminUser = requestService.requireAdminUser(request);

    List<Portal> portals = portalExtService.getAll(adminUser);
    List<PortalShallowDto> portalDtos =
        portals.stream()
            .map(portal -> objectMapper.convertValue(portal, PortalShallowDto.class))
            .collect(Collectors.toList());
    return ResponseEntity.ok(portalDtos);
  }

  @Override
  public ResponseEntity<Void> removePortalUser(String portalShortcode, UUID adminUserId) {
    AdminUser operator = requestService.requireAdminUser(request);
    portalExtService.removeUserFromPortal(adminUserId, portalShortcode, operator);
    return ResponseEntity.noContent().build();
  }
}
