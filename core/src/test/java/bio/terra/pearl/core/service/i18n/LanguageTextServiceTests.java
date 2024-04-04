package bio.terra.pearl.core.service.i18n;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.i18n.LanguageTextFactory;
import bio.terra.pearl.core.model.i18n.LanguageText;
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

        assertThat(langTexts.size(), equalTo(2));
        assertThat(langTexts.get(testName + "login"), equalTo(testName + " text"));
        assertThat(langTexts.get(testName + "logout"), equalTo(testName + " text"));
    }

}
