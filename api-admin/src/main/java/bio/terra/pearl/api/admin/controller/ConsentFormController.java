package bio.terra.pearl.api.admin.controller;

import bio.terra.pearl.api.admin.api.ConsentFormApi;
import bio.terra.pearl.api.admin.model.VersionedFormDto;
import bio.terra.pearl.api.admin.service.RequestUtilService;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.consent.ConsentForm;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.service.consent.ConsentFormService;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class ConsentFormController implements ConsentFormApi {
  private RequestUtilService requestService;
  private HttpServletRequest request;
  private ConsentFormService consentFormService;
  private ObjectMapper objectMapper;

  public ConsentFormController(
      RequestUtilService requestService,
      HttpServletRequest request,
      ConsentFormService consentFormService,
      ObjectMapper objectMapper) {
    this.requestService = requestService;
    this.request = request;
    this.consentFormService = consentFormService;
    this.objectMapper = objectMapper;
  }

  @Override
  public ResponseEntity<VersionedFormDto> newVersion(
      String portalShortcode, String stableId, VersionedFormDto body) {
    AdminUser adminUser = requestService.getFromRequest(request);
    Portal portal = requestService.authUserToPortal(adminUser, portalShortcode);
    if (!stableId.equals(body.getStableId())) {
      throw new IllegalArgumentException("form parameters don't match");
    }
    ConsentForm consentForm = objectMapper.convertValue(body, ConsentForm.class);

    ConsentForm savedConsent =
        consentFormService.createNewVersion(adminUser, portal.getId(), consentForm);

    VersionedFormDto savedConsentDto =
        objectMapper.convertValue(savedConsent, VersionedFormDto.class);
    return ResponseEntity.ok(savedConsentDto);
  }
}
