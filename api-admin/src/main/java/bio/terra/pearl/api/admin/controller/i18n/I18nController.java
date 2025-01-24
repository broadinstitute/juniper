package bio.terra.pearl.api.admin.controller.i18n;

import bio.terra.pearl.api.admin.api.I18nApi;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.i18n.LanguageTextService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.portal.PortalService;
import java.util.HashMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class I18nController implements I18nApi {
  private final LanguageTextService languageTextService;
  private final PortalService portalService;
  private final PortalEnvironmentService portalEnvironmentService;

  public I18nController(
      LanguageTextService languageTextService,
      PortalService portalService,
      PortalEnvironmentService portalEnvironmentService) {
    this.languageTextService = languageTextService;
    this.portalService = portalService;
    this.portalEnvironmentService = portalEnvironmentService;
  }

  @Override
  public ResponseEntity<Object> listLanguageTexts(
      String language, String portalShortcode, String environmentName) {
    if (StringUtils.isEmpty(environmentName)) {
      environmentName = "live";
    }

    PortalEnvironment portalEnv =
        portalEnvironmentService
            .findOne(portalShortcode, EnvironmentName.valueOfCaseInsensitive(environmentName))
            .orElseThrow(() -> new NotFoundException("Portal not found"));

    HashMap<String, String> languageTexts;
    if (language != null) {
      languageTexts =
          languageTextService.getLanguageTextMapForLanguage(portalEnv.getId(), language);
    } else {
      // default to English
      languageTexts = languageTextService.getLanguageTextMapForLanguage(portalEnv.getId(), "en");
    }
    return ResponseEntity.ok(languageTexts);
  }
}
