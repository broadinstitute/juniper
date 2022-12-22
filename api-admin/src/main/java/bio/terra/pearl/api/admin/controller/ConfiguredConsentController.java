package bio.terra.pearl.api.admin.controller;

import bio.terra.pearl.api.admin.api.ConfiguredConsentApi;
import bio.terra.pearl.api.admin.model.ConfiguredConsentDto;
import bio.terra.pearl.api.admin.service.RequestUtilService;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.consent.StudyEnvironmentConsent;
import bio.terra.pearl.core.service.study.StudyEnvironmentConsentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class ConfiguredConsentController implements ConfiguredConsentApi {
  private RequestUtilService requestService;
  private HttpServletRequest request;
  private ObjectMapper objectMapper;
  private StudyEnvironmentConsentService sesService;

  public ConfiguredConsentController(
      RequestUtilService requestService,
      HttpServletRequest request,
      ObjectMapper objectMapper,
      StudyEnvironmentConsentService sesService) {
    this.requestService = requestService;
    this.request = request;
    this.objectMapper = objectMapper;
    this.sesService = sesService;
  }

  @Override
  public ResponseEntity<ConfiguredConsentDto> patch(
      String portalShortcode,
      String studyShortcode,
      String envName,
      UUID configuredConsentId,
      ConfiguredConsentDto body) {
    AdminUser adminUser = requestService.getFromRequest(request);
    requestService.authUserToPortal(adminUser, portalShortcode);

    StudyEnvironmentConsent configuredSurvey =
        objectMapper.convertValue(body, StudyEnvironmentConsent.class);
    StudyEnvironmentConsent existing = sesService.find(configuredSurvey.getId()).get();
    BeanUtils.copyProperties(body, existing);
    StudyEnvironmentConsent savedSes = sesService.update(adminUser, existing);
    return ResponseEntity.ok(objectMapper.convertValue(savedSes, ConfiguredConsentDto.class));
  }
}
