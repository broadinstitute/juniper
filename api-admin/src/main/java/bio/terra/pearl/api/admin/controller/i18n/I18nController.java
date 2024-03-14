package bio.terra.pearl.api.admin.controller.i18n;

import bio.terra.pearl.api.admin.api.I18nApi;
import bio.terra.pearl.core.service.i18n.LanguageTextService;
import java.util.HashMap;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class I18nController implements I18nApi {
  private LanguageTextService languageTextService;

  public I18nController(LanguageTextService languageTextService) {
    this.languageTextService = languageTextService;
  }

  @Override
  public ResponseEntity<Object> listLanguageTexts(String language) {
    HashMap<String, String> languageTexts;
    if (language != null) {
      languageTexts = languageTextService.getLanguageTextMapForLanguage(language);
    } else {
      // default to English
      languageTexts = languageTextService.getLanguageTextMapForLanguage("en");
    }
    return ResponseEntity.ok(languageTexts);
  }
}
