package bio.terra.pearl.core.service.site;

import bio.terra.pearl.core.dao.site.NavbarItemDao;
import bio.terra.pearl.core.model.site.HtmlPage;
import bio.terra.pearl.core.model.site.NavbarItem;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.ImmutableEntityService;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
public class NavbarItemService extends ImmutableEntityService<NavbarItem, NavbarItemDao> {
    private HtmlPageService htmlPageService;
    public NavbarItemService(NavbarItemDao dao, HtmlPageService htmlPageService) {
        super(dao);
        this.htmlPageService = htmlPageService;
    }

    @Override
    public NavbarItem create(NavbarItem item) {
        HtmlPage htmlPage = item.getHtmlPage();
        if (htmlPage != null) {
            htmlPage.setLocalizedSiteContentId(item.getLocalizedSiteContentId());
            htmlPage = htmlPageService.create(htmlPage);
            item.setHtmlPageId(htmlPage.getId());
        }
        item = dao.create(item);
        item.setHtmlPage(htmlPage);
        return item;
    }

    public void deleteByLocalSiteId(UUID localSiteId, Set<CascadeProperty> cascades) {
        dao.deleteByLocalSiteId(localSiteId);
    }
}
