package bio.terra.pearl.api.admin.controller.forms;

import bio.terra.pearl.api.admin.api.ConfiguredConsentApi;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.api.admin.service.forms.ConsentFormExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.consent.StudyEnvironmentConsent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class ConfiguredConsentController implements ConfiguredConsentApi {
  private AuthUtilService authUtilService;
  private HttpServletRequest request;
  private ObjectMapper objectMapper;
  private ConsentFormExtService consentFormExtService;

  public ConfiguredConsentController(
      AuthUtilService authUtilService,
      HttpServletRequest request,
      ObjectMapper objectMapper,
      ConsentFormExtService consentFormExtService) {
    this.request = request;
    this.objectMapper = objectMapper;
    this.consentFormExtService = consentFormExtService;
    this.authUtilService = authUtilService;
  }

  @Override
  public ResponseEntity<Object> patch(
      String portalShortcode,
      String studyShortcode,
      String envName,
      UUID configuredConsentId,
      Object body) {
    AdminUser adminUser = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    StudyEnvironmentConsent configuredForm =
        objectMapper.convertValue(body, StudyEnvironmentConsent.class);
    var savedConfig =
        consentFormExtService.updateConfiguredConsent(
            portalShortcode, environmentName, studyShortcode, configuredForm, adminUser);
    return ResponseEntity.ok(savedConfig);
  }

  @Override
  public ResponseEntity<Object> create(
      String portalShortcode, String studyShortcode, String envName, Object body) {
    AdminUser adminUser = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    StudyEnvironmentConsent configuredForm =
        objectMapper.convertValue(body, StudyEnvironmentConsent.class);
    var savedConfig =
        consentFormExtService.createConfiguredConsent(
            portalShortcode, environmentName, studyShortcode, configuredForm, adminUser);
    return ResponseEntity.ok(savedConfig);
  }
}
