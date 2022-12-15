package bio.terra.pearl.core.service.site;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.site.SiteContentFactory;
import bio.terra.pearl.core.model.site.HtmlPage;
import bio.terra.pearl.core.model.site.HtmlSection;
import bio.terra.pearl.core.model.site.LocalizedSiteContent;
import bio.terra.pearl.core.model.site.SiteContent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;

public class SiteContentServiceTests extends BaseSpringBootTest {
    @Autowired
    private SiteContentService siteContentService;
    @Autowired
    private SiteContentFactory siteContentFactory;

    @Test
    @Transactional
    public void testSiteContentCrud() {
        HtmlSection section = HtmlSection.builder()
                .rawContent("hello").build();
        HtmlPage landingPage = HtmlPage.builder()
                .title("home")
                .sections(Arrays.asList(section)).build();
        LocalizedSiteContent lsc = LocalizedSiteContent.builder()
                .language("en")
                .landingPage(landingPage).build();

        SiteContent content = siteContentFactory
                .builderWithDependencies("testSiteContentCrud")
                .localizedSiteContents(new HashSet<>(Arrays.asList(lsc)))
                .build();

        SiteContent savedContent = siteContentService.create(content);
        Assertions.assertNotNull(savedContent.getId());
        LocalizedSiteContent savedLocal = savedContent.getLocalizedSiteContents().stream().findFirst().get();
        Assertions.assertNotNull(savedLocal.getId());
        Assertions.assertEquals("home", savedLocal.getLandingPage().getTitle());

        siteContentService.delete(savedContent.getId(), new HashSet<>());
        Assertions.assertTrue(siteContentService.find(savedContent.getId()).isEmpty());
    }
}
