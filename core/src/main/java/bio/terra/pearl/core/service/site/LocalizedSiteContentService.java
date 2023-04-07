package bio.terra.pearl.core.service.site;

import bio.terra.pearl.core.dao.site.HtmlSectionDao;
import bio.terra.pearl.core.dao.site.LocalizedSiteContentDao;
import bio.terra.pearl.core.model.site.HtmlPage;
import bio.terra.pearl.core.model.site.HtmlSection;
import bio.terra.pearl.core.model.site.LocalizedSiteContent;
import bio.terra.pearl.core.model.site.NavbarItem;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.ImmutableEntityService;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;

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
        }
        LocalizedSiteContent savedSite = dao.create(localSite);
        for (int i = 0; i < localSite.getNavbarItems().size(); i++) {
            NavbarItem navItem = localSite.getNavbarItems().get(i);
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
        return savedSite;
    }

    @Override
    public void delete(UUID localSiteId, Set<CascadeProperty> cascades) {
        dao.clearLandingPageId(localSiteId);
        navbarItemService.deleteByLocalSiteId(localSiteId, cascades);
        htmlPageService.deleteByLocalSite(localSiteId, cascades);
        dao.delete(localSiteId);
    }
}
