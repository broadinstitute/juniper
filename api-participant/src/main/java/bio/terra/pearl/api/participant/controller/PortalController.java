package bio.terra.pearl.api.participant.controller;

import bio.terra.pearl.api.participant.api.PortalApi;
import bio.terra.pearl.api.participant.model.PortalDto;
import bio.terra.pearl.core.service.portal.PortalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

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
