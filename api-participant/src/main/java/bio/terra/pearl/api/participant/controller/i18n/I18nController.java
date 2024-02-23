package bio.terra.pearl.api.participant.controller.i18n;

import bio.terra.pearl.api.participant.api.I18nApi;
import bio.terra.pearl.core.service.i18n.CoreLanguageTextService;
import java.util.HashMap;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class I18nController implements I18nApi {
  private CoreLanguageTextService languageTextService;

  public I18nController(CoreLanguageTextService languageTextService) {
    this.languageTextService = languageTextService;
  }

  @Override
  public ResponseEntity<Object> listCoreLanguageTexts(String language) {
    HashMap<String, String> coreLanguageTexts;
    if (language != null) {
      coreLanguageTexts = languageTextService.getLanguageTextMapForLanguage(language);
    } else {
      // default to English
      coreLanguageTexts = languageTextService.getLanguageTextMapForLanguage("en");
    }
    return ResponseEntity.ok(coreLanguageTexts);
  }
}
