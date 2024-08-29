package bio.terra.pearl.core.service.site;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.DaoTestUtils;
import bio.terra.pearl.core.factory.site.SiteContentFactory;
import bio.terra.pearl.core.model.site.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;

public class SiteContentServiceTests extends BaseSpringBootTest {
    @Autowired
    private SiteContentService siteContentService;
    @Autowired
    private SiteContentFactory siteContentFactory;

    @Test
    @Transactional
    public void testSiteContentCrud(TestInfo info) {
        HtmlSection section = HtmlSection.builder()
                .rawContent("hello").build();
        HtmlPage landingPage = HtmlPage.builder()
                .title("home")
                .sections(Arrays.asList(section)).build();
        LocalizedSiteContent lsc = LocalizedSiteContent.builder()
                .language("en")
                .landingPage(landingPage).build();

        SiteContent content = siteContentFactory
                .builderWithDependencies(getTestName(info))
                .localizedSiteContents(List.of(lsc))
                .build();

        SiteContent savedContent = siteContentService.create(content);
        DaoTestUtils.assertGeneratedProperties(savedContent);
        LocalizedSiteContent savedLocal = savedContent.getLocalizedSiteContents().stream().findFirst().get();
        assertNotNull(savedLocal.getId());
        assertEquals("home", savedLocal.getLandingPage().getTitle());

        siteContentService.delete(savedContent.getId(), new HashSet<>());
        Assertions.assertTrue(siteContentService.find(savedContent.getId()).isEmpty());
    }

    @Test
    @Transactional
    public void testSiteContentCreateNewVersion(TestInfo info) {
        HtmlSection section = HtmlSection.builder()
                .rawContent("hello").build();
        HtmlPage landingPage = HtmlPage.builder()
                .title("home")
                .sections(Arrays.asList(section)).build();
        NavbarItem navbarItem = NavbarItem.builder()
                .itemType(NavbarItemType.EXTERNAL)
                .href("url1.com").build();
        LocalizedSiteContent lsc = LocalizedSiteContent.builder()
                .language("en")
                .navbarItems(List.of(navbarItem))
                .landingPage(landingPage).build();

        SiteContent content = siteContentFactory
                .builderWithDependencies(getTestName(info))
                .localizedSiteContents(List.of(lsc))
                .build();

        SiteContent savedContent = siteContentService.create(content);
        DaoTestUtils.assertGeneratedProperties(savedContent);

        LocalizedSiteContent savedLsc = savedContent.getLocalizedSiteContents().get(0);
        savedLsc.getNavbarItems().get(0).setHref("url2.com");
        savedLsc.getLandingPage().getSections().get(0).setRawContent("goodbye");
        SiteContent originalContent = siteContentService.find(savedContent.getId()).get();
        siteContentService.attachChildContent(originalContent, "en");
        SiteContent updatedContent = siteContentService.createNewVersion(savedContent);


        DaoTestUtils.assertGeneratedProperties(updatedContent);
        assertThat(updatedContent.getId(), not(equalTo(savedContent.getId())));
        assertThat(updatedContent.getVersion(), equalTo(originalContent.getVersion() + 1));
        LocalizedSiteContent updatedLsc = updatedContent.getLocalizedSiteContents().get(0);
        assertThat(updatedLsc.getNavbarItems().get(0).getHref(), equalTo("url2.com"));
        assertThat(updatedLsc.getLandingPage().getSections().get(0).getRawContent(), equalTo("goodbye"));

        // confirm original was not changed
        LocalizedSiteContent originalLsc = originalContent.getLocalizedSiteContents().get(0);
        assertThat(originalLsc.getNavbarItems().get(0).getHref(), equalTo("url1.com"));
        assertThat(originalLsc.getLandingPage().getSections().get(0).getRawContent(), equalTo("hello"));
    }

    @Test
    @Transactional
    public void testAssignPublishedVersion(TestInfo info) {
        SiteContent form = siteContentFactory.buildPersisted(getTestName(info));
        siteContentService.assignPublishedVersion(form.getId());
        form = siteContentService.find(form.getId()).get();
        assertThat(form.getPublishedVersion(), equalTo(1));

        SiteContent newForm = siteContentService.createNewVersion(form);

        Assertions.assertNotEquals(newForm.getId(), form.getId());
        // check published version was NOT copied
        assertThat(newForm.getPublishedVersion(), equalTo(null));

        siteContentService.assignPublishedVersion(newForm.getId());
        newForm = siteContentService.find(newForm.getId()).get();
        assertThat(newForm.getPublishedVersion(), equalTo(2));
    }

    @Test
    @Transactional
    public void testCreateGroupedNavbar(TestInfo info) {
        HtmlSection section = HtmlSection.builder()
                .rawContent("hello").build();
        HtmlPage landingPage = HtmlPage.builder()
                .title("home")
                .sections(Arrays.asList(section)).build();

        HtmlPage aboutUs = HtmlPage.builder()
                .path("about-us")
                .title("About Us")
                .sections(Arrays.asList(section)).build();

        HtmlPage faq = HtmlPage.builder()
                .path("faq")
                .title("Freqently Asked Questions")
                .sections(Arrays.asList(section)).build();

        NavbarItem navbarItem = NavbarItem.builder()
                .itemType(NavbarItemType.INTERNAL)
                .internalPath("about-us").build();

        NavbarItem groupedItem = NavbarItem.builder()
                .itemType(NavbarItemType.GROUP)
                .text("Learn More")
                .items(new ArrayList<>(Arrays.asList(
                        NavbarItem.builder()
                                .itemType(NavbarItemType.INTERNAL)
                                .text("FAQ")
                                .internalPath("faq")
                                .build(),
                        NavbarItem.builder()
                                .itemType(NavbarItemType.EXTERNAL)
                                .text("Other Publications")
                                .href("test.com")
                                .build()
                )))
                .build();

        LocalizedSiteContent lsc = LocalizedSiteContent.builder()
                .language("en")
                .navbarItems(new ArrayList<>(Arrays.asList(navbarItem, groupedItem)))
                .pages(new ArrayList<>(Arrays.asList(aboutUs, faq)))
                .landingPage(landingPage).build();

        SiteContent content = siteContentFactory
                .builderWithDependencies(getTestName(info))
                .localizedSiteContents(List.of(lsc))
                .build();

        SiteContent savedContent = siteContentService.create(content);

        // reload fully to ensure all relationships are saved
        savedContent = siteContentService.find(savedContent.getId()).get();
        siteContentService.attachChildContent(savedContent, "en");


        DaoTestUtils.assertGeneratedProperties(savedContent);

        LocalizedSiteContent savedLsc = savedContent.getLocalizedSiteContents().get(0);

        NavbarItem savedNavbarItem = savedLsc.getNavbarItems().get(0);
        assertNotNull(savedNavbarItem.getId());
        assertEquals("about-us", savedNavbarItem.getInternalPath());
        assertEquals(0, savedNavbarItem.getItemOrder());

        NavbarItem savedGroupedItem = savedLsc.getNavbarItems().get(1);
        assertNotNull(savedGroupedItem.getId());
        assertEquals("Learn More", savedGroupedItem.getText());
        assertEquals(1, savedGroupedItem.getItemOrder());

        NavbarItem savedFaqItem = savedGroupedItem.getItems().get(0);
        assertNotNull(savedFaqItem.getId());
        assertEquals("faq", savedFaqItem.getInternalPath());
        // should be 0 because it's the first item in the group
        assertEquals(0, savedFaqItem.getItemOrder());

        NavbarItem savedOtherItem = savedGroupedItem.getItems().get(1);
        assertNotNull(savedOtherItem.getId());
        assertEquals("test.com", savedOtherItem.getHref());
        assertEquals(1, savedOtherItem.getItemOrder());


    }

    @Test
    @Transactional
    public void testCreateNavbarMissingPage(TestInfo info) {

        HtmlSection section = HtmlSection.builder()
                .rawContent("hello").build();
        HtmlPage landingPage = HtmlPage.builder()
                .title("home")
                .sections(Arrays.asList(section)).build();

        NavbarItem navbarItem = NavbarItem.builder()
                .itemType(NavbarItemType.INTERNAL)
                .internalPath("doesnt-exist")
                .build();

        LocalizedSiteContent lsc = LocalizedSiteContent.builder()
                .language("en")
                .navbarItems(new ArrayList<>(Arrays.asList(navbarItem)))
                .landingPage(landingPage).build();

        SiteContent content = siteContentFactory
                .builderWithDependencies(getTestName(info))
                .localizedSiteContents(List.of(lsc))
                .build();

        assertThrows(IllegalArgumentException.class, () -> siteContentService.create(content));


        // also fails if the page is in a group

        NavbarItem grouped = NavbarItem.builder()
                .itemType(NavbarItemType.GROUP)
                .text("group test")
                .items(new ArrayList<>(Arrays.asList(
                        NavbarItem.builder()
                                .itemType(NavbarItemType.INTERNAL)
                                .text("aaaaa")
                                .internalPath("doesnt-exist")
                                .build()
                )))
                .build();

        LocalizedSiteContent lsc2 = LocalizedSiteContent.builder()
                .language("en")
                .navbarItems(new ArrayList<>(Arrays.asList(grouped)))
                .landingPage(landingPage).build();

        SiteContent content2 = siteContentFactory
                .builderWithDependencies(getTestName(info))
                .localizedSiteContents(List.of(lsc2))
                .build();

        assertThrows(IllegalArgumentException.class, () -> siteContentService.create(content2));

    }
}
