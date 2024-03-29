package bio.terra.pearl.core.service.site;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.DaoTestUtils;
import bio.terra.pearl.core.factory.site.SiteContentFactory;
import bio.terra.pearl.core.model.site.HtmlPage;
import bio.terra.pearl.core.model.site.HtmlSection;
import bio.terra.pearl.core.model.site.LocalizedSiteContent;
import bio.terra.pearl.core.model.site.NavbarItem;
import bio.terra.pearl.core.model.site.NavbarItemType;
import bio.terra.pearl.core.model.site.SiteContent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

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
        Assertions.assertNotNull(savedLocal.getId());
        Assertions.assertEquals("home", savedLocal.getLandingPage().getTitle());

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

        form.setDefaultLanguage("zzz");
        SiteContent newForm = siteContentService.createNewVersion(form);

        Assertions.assertNotEquals(newForm.getId(), form.getId());
        // check published version was NOT copied
        assertThat(newForm.getPublishedVersion(), equalTo(null));

        siteContentService.assignPublishedVersion(newForm.getId());
        newForm = siteContentService.find(newForm.getId()).get();
        assertThat(newForm.getPublishedVersion(), equalTo(2));
    }
}
