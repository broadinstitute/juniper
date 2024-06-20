package bio.terra.pearl.core.service.portal;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.portal.PortalEnvironmentLanguage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PortalEnvironmentLanguageServiceTest extends BaseSpringBootTest {
    @Autowired
    private PortalEnvironmentLanguageService portalEnvironmentLanguageService;
    @Autowired
    private PortalEnvironmentFactory portalEnvironmentFactory;

    @Test
    @Transactional
    public void testSetLanguages(TestInfo info) {
        PortalEnvironment portalEnvironment = portalEnvironmentFactory.buildPersisted(getTestName(info));
        List<PortalEnvironmentLanguage> languages = List.of(
            PortalEnvironmentLanguage.builder().languageName("English").languageCode("en").build(),
            PortalEnvironmentLanguage.builder().languageName("Espanish").languageCode("es").build()
        );
        List<PortalEnvironmentLanguage> savedLanguages = portalEnvironmentLanguageService.setPortalEnvLanguages(portalEnvironment.getId(), languages);
        assertThat(savedLanguages, hasSize(2));
        assertThat(portalEnvironmentLanguageService.findByPortalEnvId(portalEnvironment.getId()), hasSize(2));

        // now confirm that a second call deletes previous languages and sets the new one
        List<PortalEnvironmentLanguage> newLanguages = List.of(
            PortalEnvironmentLanguage.builder().languageName("French").languageCode("fr").build()
        );
        savedLanguages = portalEnvironmentLanguageService.setPortalEnvLanguages(portalEnvironment.getId(), newLanguages);
        assertThat(savedLanguages, hasSize(1));
        assertThat(portalEnvironmentLanguageService.findByPortalEnvId(portalEnvironment.getId()), hasSize(1));
        assertThat(portalEnvironmentLanguageService.findByPortalEnvId(portalEnvironment.getId()).get(0),
                hasProperty("languageName", equalTo("French")));
    }
}
