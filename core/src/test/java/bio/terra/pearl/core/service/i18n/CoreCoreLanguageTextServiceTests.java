package bio.terra.pearl.core.service.i18n;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.i18n.CoreLanguageTextFactory;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.model.i18n.CoreLanguageText;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.List;
import java.util.Map;

public class CoreCoreLanguageTextServiceTests extends BaseSpringBootTest  {

    @Autowired
    private CoreLanguageTextService coreLanguageTextService;

    @Autowired
    private CoreLanguageTextFactory coreLanguageTextFactory;

    @Test
    @Transactional
    public void testFindAll(TestInfo testInfo) {
        coreLanguageTextFactory.buildPersisted(getTestName(testInfo), "login", "dev");
        coreLanguageTextFactory.buildPersisted(getTestName(testInfo), "login", "es");

        List<CoreLanguageText> langTexts = coreLanguageTextService.findAll();

        assertThat(langTexts, hasSize(2));
        assertThat(langTexts.get(0).getLanguage(), equalTo("dev"));
        assertThat(langTexts.get(1).getLanguage(), equalTo("es"));
    }

    @Test
    @Transactional
    public void testFindByPortalEnvIdAndLanguage(TestInfo testInfo) {
        coreLanguageTextFactory.buildPersisted(getTestName(testInfo), "login", "dev");
        coreLanguageTextFactory.buildPersisted(getTestName(testInfo), "login", "es");

        Map<String, String> langTexts = coreLanguageTextService.getLanguageTextMapForLanguage("dev");

        assertThat(langTexts.size(), equalTo(1));
        assertThat(langTexts.get(getTestName(testInfo) + "login"), equalTo(getTestName(testInfo) + " text"));
    }

}
