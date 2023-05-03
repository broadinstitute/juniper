package bio.terra.pearl.api.admin.controller;

import bio.terra.pearl.api.admin.api.ConsentFormApi;
import bio.terra.pearl.api.admin.model.VersionedFormDto;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.api.admin.service.forms.ConsentFormExtService;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.consent.ConsentForm;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class ConsentFormController implements ConsentFormApi {
  private AuthUtilService requestService;
  private HttpServletRequest request;
  private ConsentFormExtService consentFormExtService;
  private ObjectMapper objectMapper;

  public ConsentFormController(
      AuthUtilService requestService,
      HttpServletRequest request,
      ConsentFormExtService consentFormExtService,
      ObjectMapper objectMapper) {
    this.requestService = requestService;
    this.request = request;
    this.consentFormExtService = consentFormExtService;
    this.objectMapper = objectMapper;
  }

  @Override
  public ResponseEntity<VersionedFormDto> newVersion(
      String portalShortcode, String stableId, VersionedFormDto body) {
    AdminUser adminUser = requestService.requireAdminUser(request);
    if (!stableId.equals(body.getStableId())) {
      throw new IllegalArgumentException("form parameters don't match");
    }
    ConsentForm consentForm = objectMapper.convertValue(body, ConsentForm.class);

    ConsentForm savedConsent =
        consentFormExtService.createNewVersion(portalShortcode, consentForm, adminUser);

    VersionedFormDto savedConsentDto =
        objectMapper.convertValue(savedConsent, VersionedFormDto.class);
    return ResponseEntity.ok(savedConsentDto);
  }
}
