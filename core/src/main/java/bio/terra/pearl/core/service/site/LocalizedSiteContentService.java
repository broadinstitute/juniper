package bio.terra.pearl.core.service.site;

import bio.terra.pearl.core.dao.site.HtmlSectionDao;
import bio.terra.pearl.core.dao.site.LocalizedSiteContentDao;
import bio.terra.pearl.core.model.site.HtmlPage;
import bio.terra.pearl.core.model.site.HtmlSection;
import bio.terra.pearl.core.model.site.LocalizedSiteContent;
import bio.terra.pearl.core.model.site.NavbarItem;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.ImmutableEntityService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class LocalizedSiteContentService extends ImmutableEntityService<LocalizedSiteContent, LocalizedSiteContentDao> {
    private NavbarItemService navbarItemService;
    private HtmlPageService htmlPageService;
    private HtmlSectionDao htmlSectionDao;
    public LocalizedSiteContentService(LocalizedSiteContentDao dao, NavbarItemService navbarItemService,
                                       HtmlPageService htmlPageService, HtmlSectionDao htmlSectionDao) {
        super(dao);
        this.navbarItemService = navbarItemService;
        this.htmlPageService = htmlPageService;
        this.htmlSectionDao = htmlSectionDao;
    }

    public List<LocalizedSiteContent> findBySiteContent(UUID siteContentId) {
        return dao.findBySiteContent(siteContentId);
    }

    @Override
    public LocalizedSiteContent create(LocalizedSiteContent localSite) {
        if (localSite.getFooterSection() != null) {
            HtmlSection footer = htmlSectionDao.create(localSite.getFooterSection());
            localSite.setFooterSectionId(footer.getId());
            localSite.setFooterSection(footer);
        }
        LocalizedSiteContent savedSite = dao.create(localSite);

        List<HtmlPage> pages = localSite.getPages();
        for (int i = 0; i < pages.size(); i++) {
            HtmlPage page = pages.get(i);
            page.setLocalizedSiteContentId(savedSite.getId());
            page = htmlPageService.create(page);
            pages.set(i, page);
        }
        for (int i = 0; i < localSite.getNavbarItems().size(); i++) {
            NavbarItem navItem = localSite.getNavbarItems().get(i);

            validateNavbarItem(navItem, pages);

            navItem.setItemOrder(i);
            navItem.setLocalizedSiteContentId(savedSite.getId());
            NavbarItem savedItem = navbarItemService.create(navItem);
            savedSite.getNavbarItems().add(savedItem);
        }
        HtmlPage landingPage = localSite.getLandingPage();
        if (landingPage != null) {
            landingPage.setLocalizedSiteContentId(savedSite.getId());
            landingPage = htmlPageService.create(landingPage);
            savedSite.setLandingPageId(landingPage.getId());
            dao.setLandingPageId(savedSite.getId(), landingPage.getId());
        }
        savedSite.setLandingPage(landingPage);
        savedSite.setFooterSection(localSite.getFooterSection());
        return savedSite;
    }

    private void validateNavbarItem(NavbarItem item, List<HtmlPage> pages) {
        if (item.getHtmlPagePath() != null) {
            pages.stream()
                    .filter(p -> p.getPath().equals(item.getHtmlPagePath()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Navbar item page path (%s) not found".formatted(item.getHtmlPagePath())));
        }

        if (item.getItems() != null) {
            for (NavbarItem groupedItem : item.getItems()) {
                validateNavbarItem(groupedItem, pages);
            }
        }
    }

    @Override
    public void delete(UUID localSiteId, Set<CascadeProperty> cascades) {
        dao.clearLandingPageId(localSiteId);
        navbarItemService.deleteByLocalSiteId(localSiteId, cascades);
        htmlPageService.deleteByLocalSite(localSiteId, cascades);
        dao.delete(localSiteId);
    }
}
