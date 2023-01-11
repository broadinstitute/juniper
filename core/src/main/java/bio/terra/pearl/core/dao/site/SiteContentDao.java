package bio.terra.pearl.core.dao.site;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.site.HtmlPage;
import bio.terra.pearl.core.model.site.HtmlSection;
import bio.terra.pearl.core.model.site.NavbarItem;
import bio.terra.pearl.core.model.site.SiteContent;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class SiteContentDao extends BaseJdbiDao<SiteContent> {
    private LocalizedSiteContentDao localizedSiteContentDao;
    private NavbarItemDao navbarItemDao;
    private HtmlPageDao htmlPageDao;
    private HtmlSectionDao htmlSectionDao;

    public SiteContentDao(Jdbi jdbi, LocalizedSiteContentDao localizedSiteContentDao, NavbarItemDao navbarItemDao,
                          HtmlPageDao htmlPageDao, HtmlSectionDao htmlSectionDao) {
        super(jdbi);
        this.localizedSiteContentDao = localizedSiteContentDao;
        this.navbarItemDao = navbarItemDao;
        this.htmlPageDao = htmlPageDao;
        this.htmlSectionDao = htmlSectionDao;
    }

    @Override
    protected Class<SiteContent> getClazz() {
        return SiteContent.class;
    }

    public Optional<SiteContent> findOne(String stableId, int version) {
        return findByTwoProperties("stable_id", stableId, "version", version);
    }

    public List<SiteContent> findByPortalId(UUID portalId) {
        return findAllByProperty("portal_id", portalId);
    }

    /**
     * returns a fully hydrated SiteContent with all kids attached of the specified language
     * images are excluded
     * */
    public Optional<SiteContent> findOneFull(UUID siteContentId, String language) {
        Optional<SiteContent> siteContentOpt = find(siteContentId);
        siteContentOpt.ifPresent(siteContent -> {
            localizedSiteContentDao.findBySiteContent(siteContentId, language).ifPresent(localSite -> {
                siteContent.getLocalizedSiteContents().add(localSite);
                List<NavbarItem> navbarItems = navbarItemDao.findByLocalSiteId(localSite.getId());
                List<HtmlPage> htmlPages = htmlPageDao.findByLocalSite(localSite.getId());
                List<HtmlSection> htmlSections = htmlSectionDao.findByLocalizedSite(localSite.getId());
                navbarItems.forEach(item -> {
                    item.setHtmlPage(htmlPages.stream().filter(page -> page.getId().equals(item.getHtmlPageId()))
                            .findFirst().orElse(null));
                });
                htmlPages.forEach(page -> {
                    page.getSections().addAll(htmlSections.stream().filter(section ->
                            page.getId().equals(section.getHtmlPageId())
                    ).collect(Collectors.toList()));
                });
                localSite.setLandingPage(htmlPages.stream()
                        .filter(page -> page.getId().equals(localSite.getLandingPageId()))
                        .findFirst().orElse(null));
                localSite.getNavbarItems().addAll(navbarItems);
            });
        });
        return siteContentOpt;
    }
}
