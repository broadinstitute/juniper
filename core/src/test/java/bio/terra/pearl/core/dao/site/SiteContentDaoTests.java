package bio.terra.pearl.core.dao.site;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.site.SiteContentFactory;
import bio.terra.pearl.core.model.site.*;
import bio.terra.pearl.core.service.site.SiteContentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SiteContentDaoTests extends BaseSpringBootTest {
    @Autowired
    private SiteContentService siteContentService;
    @Autowired
    private SiteContentFactory siteContentFactory;

    @Autowired
    private SiteContentDao siteContentDao;

    @Test
    @Transactional
    public void testAttachChildren(TestInfo info) {
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
                .path("about-us")
                .title("About Us Title")
                .sections(Arrays.asList(aboutUsSection)).build();
        NavbarItem navbarItem = NavbarItem.builder()
                .internalPath("about-us")
                .text("About Us").build();

        LocalizedSiteContent lsc = LocalizedSiteContent.builder()
                .language("en")
                .landingPage(landingPage)
                .navbarItems(Arrays.asList(navbarItem))
                .pages(Arrays.asList(aboutUsPage))
                .build();

        SiteContent content = siteContentFactory
                .builderWithDependencies(getTestName(info))
                .localizedSiteContents(List.of(lsc))
                .build();
        SiteContent savedContent = siteContentService.create(content);

        SiteContent loadedContent = siteContentService.find(savedContent.getId()).get();
        // this load should be shallow
        assertEquals(0, loadedContent.getLocalizedSiteContents().size());

        siteContentDao.attachChildContent(loadedContent, "en");

        assertEquals(1, loadedContent.getLocalizedSiteContents().size());
        LocalizedSiteContent loadedLocal = loadedContent.getLocalizedSiteContents().stream().findFirst().get();

        // does not include landing page
        assertEquals(1, loadedLocal.getPages().size());
        assertEquals("About Us Title", loadedLocal.getPages().get(0).getTitle());

        assertEquals(1, loadedLocal.getNavbarItems().size());
        NavbarItem loadedItem = loadedLocal.getNavbarItems().get(0);
        assertEquals("About Us", loadedItem.getText());
        HtmlPage loadedAboutUs = loadedLocal.getPages().stream().filter(page -> page.getPath().equals("about-us")).findFirst().get();
        assertEquals("About Us Title", loadedAboutUs.getTitle());
        assertEquals("a great team", loadedAboutUs.getSections().get(0).getRawContent());

        HtmlPage loadedLandingPage = loadedLocal.getLandingPage();
        assertEquals(2, loadedLandingPage.getSections().size());
        assertEquals("helloSection", loadedLandingPage.getSections().get(0).getRawContent());
    }

    @Test
    @Transactional
    public void attachGroupedNavbarItems(TestInfo info) {
        HtmlSection helloSection = HtmlSection.builder()
                .rawContent("helloSection").build();
        HtmlSection goodbyeSection = HtmlSection.builder()
                .rawContent("goodbyeSection").build();
        HtmlPage landingPage = HtmlPage.builder()
                .title("home")
                .sections(Arrays.asList(helloSection, goodbyeSection)).build();


        NavbarItem navbarItem = NavbarItem.builder()
                .items(Arrays.asList(
                        NavbarItem.builder().text("google").itemType(NavbarItemType.EXTERNAL).href("google.com").build(),
                        NavbarItem.builder().text("aaaaaa").itemType(NavbarItemType.MAILING_LIST).build()
                ))
                .text("About Us").build();

        LocalizedSiteContent lsc = LocalizedSiteContent.builder()
                .language("en")
                .landingPage(landingPage)
                .navbarItems(Arrays.asList(navbarItem))
                .build();

        SiteContent content = siteContentFactory
                .builderWithDependencies(getTestName(info))
                .localizedSiteContents(List.of(lsc))
                .build();

        SiteContent savedContent = siteContentService.create(content);

        SiteContent loadedContent = siteContentService.find(savedContent.getId()).get();
        siteContentDao.attachChildContent(loadedContent, "en");
        assertEquals(1, loadedContent.getLocalizedSiteContents().size());

        LocalizedSiteContent loadedLocal = loadedContent.getLocalizedSiteContents().stream().findFirst().get();
        assertEquals(1, loadedLocal.getNavbarItems().size());

        NavbarItem loadedItem = loadedLocal.getNavbarItems().get(0);
        assertEquals("About Us", loadedItem.getText());
        assertEquals(2, loadedItem.getItems().size());

        NavbarItem loadedGoogle = loadedItem.getItems().stream().filter(item -> item.getText().equals("google")).findFirst().get();
        assertEquals("google.com", loadedGoogle.getHref());

        NavbarItem loadedMailingList = loadedItem.getItems().stream().filter(item -> item.getText().equals("aaaaaa")).findFirst().get();
        assertEquals(NavbarItemType.MAILING_LIST, loadedMailingList.getItemType());
    }
}
