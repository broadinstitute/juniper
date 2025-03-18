package bio.terra.pearl.core.service.i18n;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.i18n.LanguageTextFactory;
import bio.terra.pearl.core.factory.portal.PortalFactory;
import bio.terra.pearl.core.factory.site.SiteContentFactory;
import bio.terra.pearl.core.model.i18n.LanguageText;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.site.LocalizedSiteContent;
import bio.terra.pearl.core.model.site.SiteContent;
import bio.terra.pearl.core.service.site.LocalizedSiteContentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class LanguageTextServiceTests extends BaseSpringBootTest  {

    @Autowired
    private LanguageTextService languageTextService;

    @Autowired
    private LanguageTextFactory languageTextFactory;

    @Autowired
    private PortalFactory portalFactory;

    @Autowired
    private SiteContentFactory siteContentFactory;

    @Autowired
    private LocalizedSiteContentService localizedSiteContentService;

    @Test
    @Transactional
    public void testFindSystemTextByKeyAndLanguage(TestInfo testInfo) {
        String testName = getTestName(testInfo);
        languageTextFactory.buildPersisted(testName, "testLogin", "fr");
        languageTextFactory.buildPersisted(testName, "testLogin", "es");

        Optional<LanguageText> frenchLoginText = languageTextService.findSystemTextByKeyAndLanguage(testName + "testLogin", "fr");
        assertThat(frenchLoginText.isPresent(), equalTo(true));
        assertThat(frenchLoginText.get().getText(), equalTo(testName + " text"));

        Optional<LanguageText> missingText = languageTextService.findSystemTextByKeyAndLanguage("doesNotExist", "fr");
        assertThat(missingText.isPresent(), equalTo(false));

        Portal portal = portalFactory.buildPersisted(testName);

        languageTextService.create(
                LanguageText
                        .builder()
                        .text("SHOULD NOT RETURN OVERRIDE")
                        .language("fr")
                        .keyName("testLogin").build());

        Optional<LanguageText> portalFrenchLoginText = languageTextService.findSystemTextByKeyAndLanguage(testName + "testLogin", "fr");

        assertThat(portalFrenchLoginText.isPresent(), equalTo(true));
        assertThat(portalFrenchLoginText.get().getText(), equalTo(testName + " text"));
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
    public void testGetLanguageTextBySiteContent(TestInfo info) {

        SiteContent content1 = siteContentFactory.buildPersisted(getTestName(info));

        LocalizedSiteContent localizedContent1Es = LocalizedSiteContent
                .builder()
                .siteContentId(content1.getId())
                .language("es")
                .build();

        LocalizedSiteContent localizedContent1Dev = LocalizedSiteContent
                .builder()
                .siteContentId(content1.getId())
                .language("dev")
                .build();


        localizedContent1Es = localizedSiteContentService.create(localizedContent1Es);
        localizedContent1Dev = localizedSiteContentService.create(localizedContent1Dev);

        SiteContent content2 = siteContentFactory.buildPersisted(getTestName(info));

        LocalizedSiteContent localizedContent2Es = LocalizedSiteContent
                .builder()
                .siteContentId(content2.getId())
                .language("es")
                .build();


        localizedContent2Es = localizedSiteContentService.create(localizedContent2Es);
        
        LanguageText languageTextEs = languageTextFactory.buildPersisted(getTestName(info), "testkey", "es", localizedContent1Es.getId());
        LanguageText languageTextDev = languageTextFactory.buildPersisted(getTestName(info), "testkey", "dev", localizedContent1Dev.getId());


        Optional<LanguageText> foundText = languageTextService.findBySiteContentLanguageAndKey(content1.getId(), "es", languageTextEs.getKeyName());
        assertThat(foundText.isPresent(), equalTo(true));
        assertThat(foundText.get().getText(), equalTo(languageTextEs.getText()));
        assertThat(foundText.get().getId(), equalTo(languageTextEs.getId()));


        foundText = languageTextService.findBySiteContentLanguageAndKey(content1.getId(), "dev", languageTextDev.getKeyName());
        assertThat(foundText.isPresent(), equalTo(true));
        assertThat(foundText.get().getText(), equalTo(languageTextDev.getText()));
        assertThat(foundText.get().getId(), equalTo(languageTextDev.getId()));

        foundText = languageTextService.findBySiteContentLanguageAndKey(content2.getId(), "es", languageTextEs.getKeyName());
        assertThat(foundText.isPresent(), equalTo(false));

    }

}
