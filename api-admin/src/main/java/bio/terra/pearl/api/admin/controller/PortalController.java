package bio.terra.pearl.api.admin.controller;

import bio.terra.pearl.api.admin.api.PortalApi;
import bio.terra.pearl.api.admin.model.PortalDto;
import bio.terra.pearl.api.admin.service.RequestUtilService;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.service.portal.PortalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class PortalController implements PortalApi {
  private PortalService portalService;
  private ObjectMapper objectMapper;
  private RequestUtilService requestService;
  private final HttpServletRequest request;

  public PortalController(
      PortalService portalService,
      ObjectMapper objectMapper,
      RequestUtilService requestService,
      HttpServletRequest request) {
    this.portalService = portalService;
    this.objectMapper = objectMapper;
    this.requestService = requestService;
    this.request = request;
  }

  @Override
  public ResponseEntity<PortalDto> get(String portalShortcode) {
    Optional<PortalDto> portalDtoOpt =
        portalService
            .findOneByShortcode(portalShortcode)
            .map(
                portal -> {
                  PortalDto portalDto = new PortalDto();
                  BeanUtils.copyProperties(portal, portalDto);
                  return portalDto;
                });
    return ResponseEntity.of(portalDtoOpt);
  }

  @Override
  public ResponseEntity<List<PortalDto>> getAll() {
    AdminUser adminUser = requestService.getFromRequest(request);

    List<Portal> portals = portalService.findByAdminUserId(adminUser.getId());
    List<PortalDto> portalDtos =
        portals.stream()
            .map(portal -> objectMapper.convertValue(portal, PortalDto.class))
            .collect(Collectors.toList());
    return ResponseEntity.ok(portalDtos);
  }
}
