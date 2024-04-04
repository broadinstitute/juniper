package bio.terra.pearl.core.service.i18n;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.i18n.LanguageTextFactory;
import bio.terra.pearl.core.factory.portal.PortalFactory;
import bio.terra.pearl.core.model.i18n.LanguageText;
import bio.terra.pearl.core.model.portal.Portal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class LanguageTextServiceTests extends BaseSpringBootTest  {

    @Autowired
    private LanguageTextService languageTextService;

    @Autowired
    private LanguageTextFactory languageTextFactory;

    @Autowired
    private PortalFactory portalFactory;

    @Test
    @Transactional
    public void testFindByKeyNameAndLanguage(TestInfo testInfo) {
        String testName = getTestName(testInfo);
        languageTextFactory.buildPersisted(testName, "testLogin", "fr");
        languageTextFactory.buildPersisted(testName, "testLogin", "es");

        Optional<LanguageText> frenchLoginText = languageTextService.findByKeyNameAndLanguage(testName + "testLogin", "fr");
        assertThat(frenchLoginText.isPresent(), equalTo(true));
        assertThat(frenchLoginText.get().getText(), equalTo(testName + " text"));

        Optional<LanguageText> missingText = languageTextService.findByKeyNameAndLanguage("doesNotExist", "fr");
        assertThat(missingText.isPresent(), equalTo(false));
    }

    @Test
    @Transactional
    public void testGetLanguageTextMapForLanguage(TestInfo testInfo) {
        String testName = getTestName(testInfo);
        languageTextFactory.buildPersisted(testName, "login", "testLang");
        languageTextFactory.buildPersisted(testName, "logout", "testLang");
        languageTextFactory.buildPersisted(testName, "logout", "otherTestLang");

        Map<String, String> langTexts = languageTextService.getLanguageTextMapForLanguage(null, "testLang");

        assertThat(langTexts, equalTo(Map.of(
            testName + "login", testName + " text",
            testName + "logout", testName + " text"
        )));
    }

    @Test
    @Transactional
    public void testGetLanguageTextMapForPortal(TestInfo testInfo) {
        String testName = getTestName(testInfo);

        Portal portalA = portalFactory.buildPersisted(testName);
        Portal portalB = portalFactory.buildPersisted(testName);

        languageTextFactory.buildPersisted(testName, "globalKey", "testLang");
        languageTextFactory.buildPersisted(testName, "login", "testLang", portalA.getId());
        languageTextFactory.buildPersisted(testName, "login", "testLang", portalB.getId());
        languageTextFactory.buildPersisted(testName, "logout", "testLang", portalA.getId());
        languageTextFactory.buildPersisted(testName, "logout", "testLang", portalB.getId());

        Map<String, String> langTexts = languageTextService.getLanguageTextMapForLanguage(portalA.getId(), "testLang");

        //This should only return the texts for portalA, as well as the global texts
        assertThat(langTexts, equalTo(Map.of(
            testName + "login", testName + " text",
            testName + "logout", testName + " text",
            testName + "globalKey", testName + " text"
        )));
    }

}
