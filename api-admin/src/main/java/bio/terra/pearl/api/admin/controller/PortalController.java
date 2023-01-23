package bio.terra.pearl.api.admin.controller;

import bio.terra.common.iam.SamUser;
import bio.terra.common.iam.SamUserFactory;
import bio.terra.pearl.api.admin.api.PortalApi;
import bio.terra.pearl.api.admin.model.PortalShallowDto;
import bio.terra.pearl.api.admin.service.RequestUtilService;
import bio.terra.pearl.core.config.SamConfiguration;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.service.portal.PortalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class PortalController implements PortalApi {
  private PortalService portalService;
  private ObjectMapper objectMapper;
  private RequestUtilService requestService;
  // TODO: autowire
  private SamConfiguration samConfiguration = new SamConfiguration("https://sam.dsde-dev.broadinstitute.org");
  private SamUserFactory samUserFactory;
  private final HttpServletRequest request;

  public PortalController(
          PortalService portalService,
          ObjectMapper objectMapper,
          RequestUtilService requestService,
          SamUserFactory samUserFactory,
          HttpServletRequest request) {
    this.portalService = portalService;
    this.objectMapper = objectMapper;
    this.requestService = requestService;
    this.samUserFactory = samUserFactory;
    this.request = request;
  }

  @Override
  public ResponseEntity<Object> get(String portalShortcode) {
    Optional<Object> portalOpt =
        portalService.findOneByShortcodeFullLoad(portalShortcode, "en").map(portal -> portal);
    return ResponseEntity.of(portalOpt);
  }

  @Override
  public ResponseEntity<List<PortalShallowDto>> getAll() {
    AdminUser adminUser = requestService.getFromRequest(request);
    SamUser samUser = samUserFactory.from(request, samConfiguration.basePath());
    List<Portal> portals = portalService.findBySamUser(samUser);
    List<PortalShallowDto> portalDtos =
        portals.stream()
            .map(portal -> objectMapper.convertValue(portal, PortalShallowDto.class))
            .collect(Collectors.toList());
    return ResponseEntity.ok(portalDtos);
  }
}
