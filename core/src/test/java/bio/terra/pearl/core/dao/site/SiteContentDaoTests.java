package bio.terra.pearl.core.dao.site;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.site.SiteContentFactory;
import bio.terra.pearl.core.model.site.*;
import bio.terra.pearl.core.service.site.SiteContentService;
import java.util.Arrays;
import java.util.HashSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class SiteContentDaoTests extends BaseSpringBootTest {
    @Autowired
    private SiteContentService siteContentService;
    @Autowired
    private SiteContentFactory siteContentFactory;

    @Autowired
    private SiteContentDao siteContentDao;

    @Test
    @Transactional
    public void testAttachChildren() {
        HtmlSection helloSection = HtmlSection.builder()
                .rawContent("helloSection").build();
        HtmlSection goodbyeSection = HtmlSection.builder()
                .rawContent("goodbyeSection").build();
        HtmlPage landingPage = HtmlPage.builder()
                .title("home")
                .sections(Arrays.asList(helloSection, goodbyeSection)).build();

        HtmlSection aboutUsSection = HtmlSection.builder()
                .rawContent("a great team").build();
        HtmlPage aboutUsPage = HtmlPage.builder()
                .title("About Us Title")
                .sections(Arrays.asList(aboutUsSection)).build();
        NavbarItem navbarItem = NavbarItem.builder()
                .htmlPage(aboutUsPage)
                .text("About Us").build();

        LocalizedSiteContent lsc = LocalizedSiteContent.builder()
                .language("en")
                .landingPage(landingPage)
                .navbarItems(Arrays.asList(navbarItem)).build();

        SiteContent content = siteContentFactory
                .builderWithDependencies("testSiteContentCrud")
                .localizedSiteContents(new HashSet<>(Arrays.asList(lsc)))
                .build();
        SiteContent savedContent = siteContentService.create(content);

        SiteContent loadedContent = siteContentService.find(savedContent.getId()).get();
        // this load should be shallow
        Assertions.assertEquals(0, loadedContent.getLocalizedSiteContents().size());

        siteContentDao.attachChildContent(loadedContent, "en");

        Assertions.assertEquals(1, loadedContent.getLocalizedSiteContents().size());
        LocalizedSiteContent loadedLocal = loadedContent.getLocalizedSiteContents().stream().findFirst().get();
        Assertions.assertEquals(1, loadedLocal.getNavbarItems().size());
        NavbarItem loadedItem = loadedLocal.getNavbarItems().get(0);
        Assertions.assertEquals("About Us", loadedItem.getText());
        HtmlPage loadedAboutUs = loadedItem.getHtmlPage();
        Assertions.assertEquals("About Us Title", loadedAboutUs.getTitle());
        Assertions.assertEquals("a great team", loadedAboutUs.getSections().get(0).getRawContent());

        HtmlPage loadedLandingPage = loadedLocal.getLandingPage();
        Assertions.assertEquals(2, loadedLandingPage.getSections().size());
        Assertions.assertEquals("helloSection", loadedLandingPage.getSections().get(0).getRawContent());
    }
}
