package bio.terra.pearl.core.service.site;

import bio.terra.pearl.core.dao.site.LocalizedSiteContentDao;
import bio.terra.pearl.core.model.site.HtmlPage;
import bio.terra.pearl.core.model.site.LocalizedSiteContent;
import bio.terra.pearl.core.model.site.NavbarItem;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.CrudService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class LocalizedSiteContentService extends CrudService<LocalizedSiteContent, LocalizedSiteContentDao> {
    private NavbarItemService navbarItemService;
    private HtmlPageService htmlPageService;
    public LocalizedSiteContentService(LocalizedSiteContentDao dao, NavbarItemService navbarItemService,
                                       HtmlPageService htmlPageService) {
        super(dao);
        this.navbarItemService = navbarItemService;
        this.htmlPageService = htmlPageService;
    }

    public List<LocalizedSiteContent> findBySiteContent(UUID siteContentId) {
        return dao.findBySiteContent(siteContentId);
    }

    @Override
    public LocalizedSiteContent create(LocalizedSiteContent localSite) {
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
