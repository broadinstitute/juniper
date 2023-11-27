package bio.terra.pearl.api.admin.controller.forms;

import bio.terra.pearl.api.admin.api.ConsentFormApi;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.api.admin.service.forms.ConsentFormExtService;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.consent.ConsentForm;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class ConsentFormController implements ConsentFormApi {
  private AuthUtilService authUtilService;
  private HttpServletRequest request;
  private ConsentFormExtService consentFormExtService;
  private ObjectMapper objectMapper;

  public ConsentFormController(
      AuthUtilService authUtilService,
      HttpServletRequest request,
      ConsentFormExtService consentFormExtService,
      ObjectMapper objectMapper) {
    this.authUtilService = authUtilService;
    this.request = request;
    this.consentFormExtService = consentFormExtService;
    this.objectMapper = objectMapper;
  }

  @Override
  public ResponseEntity<Object> newVersion(String portalShortcode, String stableId, Object body) {
    AdminUser adminUser = authUtilService.requireAdminUser(request);
    ConsentForm consentForm = objectMapper.convertValue(body, ConsentForm.class);
    if (!stableId.equals(consentForm.getStableId())) {
      throw new IllegalArgumentException("form parameters don't match");
    }
    ConsentForm savedConsent =
        consentFormExtService.createNewVersion(portalShortcode, consentForm, adminUser);
    return ResponseEntity.ok(savedConsent);
  }

  @Override
  public ResponseEntity<Object> create(String portalShortcode, String stableId, Object body) {
    AdminUser adminUser = authUtilService.requireAdminUser(request);
    ConsentForm consentForm = objectMapper.convertValue(body, ConsentForm.class);
    if (!stableId.equals(consentForm.getStableId())) {
      throw new IllegalArgumentException("form parameters don't match");
    }
    ConsentForm savedConsent =
        consentFormExtService.create(portalShortcode, consentForm, adminUser);
    return ResponseEntity.ok(savedConsent);
  }
}
