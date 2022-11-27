package bio.terra.pearl.api.admin.controller;

import bio.terra.pearl.api.admin.api.PortalApi;
import bio.terra.pearl.api.admin.model.PortalDto;
import bio.terra.pearl.core.service.PortalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.util.Optional;

@Controller
public class PortalController implements PortalApi {
  private PortalService portalService;
  private ObjectMapper objectMapper;

  public PortalController(PortalService portalService, ObjectMapper objectMapper) {
    this.portalService = portalService;
    this.objectMapper = objectMapper;
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
}
