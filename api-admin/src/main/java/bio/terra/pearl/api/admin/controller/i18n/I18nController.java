package bio.terra.pearl.api.admin.controller.i18n;

import bio.terra.pearl.api.admin.api.I18nApi;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.i18n.LanguageTextService;
import bio.terra.pearl.core.service.portal.PortalService;
import java.util.HashMap;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class I18nController implements I18nApi {
  private final LanguageTextService languageTextService;
  private final PortalService portalService;

  public I18nController(LanguageTextService languageTextService, PortalService portalService) {
    this.languageTextService = languageTextService;
    this.portalService = portalService;
  }

  @Override
  public ResponseEntity<Object> listLanguageTexts(String language, String portalShortcode) {
    Portal portal =
        portalService
            .findOneByShortcode(portalShortcode)
            .orElseThrow(() -> new NotFoundException("Portal not found"));

    HashMap<String, String> languageTexts;
    if (language != null) {
      languageTexts = languageTextService.getLanguageTextMapForLanguage(portal.getId(), language);
    } else {
      // default to English
      languageTexts = languageTextService.getLanguageTextMapForLanguage(portal.getId(), "en");
    }
    return ResponseEntity.ok(languageTexts);
  }
}
