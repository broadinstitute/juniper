package bio.terra.pearl.core.factory.portal;

import bio.terra.pearl.core.model.portal.PortalEnvironmentLanguage;
import bio.terra.pearl.core.service.portal.PortalEnvironmentLanguageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PortalEnvironmentLanguageFactory {
    @Autowired
    private PortalEnvironmentLanguageService portalEnvironmentLanguageService;
    public PortalEnvironmentLanguage addToEnvironment(UUID portalEnvironmentId, String languageName, String languageCode) {
        PortalEnvironmentLanguage language = PortalEnvironmentLanguage.builder()
                .languageName(languageName.valueOf(languageName))
                .languageCode(languageCode)
                .portalEnvironmentId(portalEnvironmentId)
                .build();
        return portalEnvironmentLanguageService.create(language);
    }
}
