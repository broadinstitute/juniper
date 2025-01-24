package bio.terra.pearl.core.dao.site;

import bio.terra.pearl.core.dao.BaseVersionedJdbiDao;
import bio.terra.pearl.core.dao.i18n.LanguageTextDao;
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
public class SiteContentDao extends BaseVersionedJdbiDao<SiteContent> {
    private final LanguageTextDao languageTextDao;
    private LocalizedSiteContentDao localizedSiteContentDao;
    private NavbarItemDao navbarItemDao;
    private HtmlPageDao htmlPageDao;
    private HtmlSectionDao htmlSectionDao;

    public SiteContentDao(Jdbi jdbi, LocalizedSiteContentDao localizedSiteContentDao, NavbarItemDao navbarItemDao,
                          HtmlPageDao htmlPageDao, HtmlSectionDao htmlSectionDao, LanguageTextDao languageTextDao) {
        super(jdbi);
        this.localizedSiteContentDao = localizedSiteContentDao;
        this.navbarItemDao = navbarItemDao;
        this.htmlPageDao = htmlPageDao;
        this.htmlSectionDao = htmlSectionDao;
        this.languageTextDao = languageTextDao;
    }

    @Override
    protected Class<SiteContent> getClazz() {
        return SiteContent.class;
    }

    public Optional<SiteContent> findOne(String stableId, int version) {
        return findByTwoProperties("stable_id", stableId, "version", version);
    }

    public List<SiteContent> findByStableId(String stableId) {
        return findAllByProperty("stable_id", stableId);
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
        siteContentOpt.ifPresent(siteContent -> attachChildContent(siteContent, language));
        return siteContentOpt;
    }

    /** attaches all the content (pages, sections, navbar) children for the given language to the SiteContent */
    public void attachChildContent(SiteContent siteContent, String language) {
        localizedSiteContentDao.findBySiteContent(siteContent.getId(), language).ifPresent(localSite -> {
            siteContent.getLocalizedSiteContents().add(localSite);
            List<NavbarItem> navbarItems = navbarItemDao.findByLocalSiteId(localSite.getId());
            List<HtmlPage> htmlPages = htmlPageDao.findByLocalSite(localSite.getId());
            List<HtmlSection> htmlSections = htmlSectionDao.findByLocalizedSite(localSite.getId());
            navbarItems.forEach(item -> {
                // attach the children of this item, if they exist
                item.setItems(
                        navbarItems
                                .stream()
                                .filter(childItem -> item.getId().equals(childItem.getParentNavbarItemId()))
                                .toList());
            });
            htmlPages.forEach(page -> {
                page.getSections().addAll(htmlSections.stream().filter(section ->
                        page.getId().equals(section.getHtmlPageId())
                ).collect(Collectors.toList()));
            });

            localSite.setPages(htmlPages.stream().filter(page -> !page.getId().equals(localSite.getLandingPageId())).toList());

            localSite.setLandingPage(htmlPages.stream()
                    .filter(page -> page.getId().equals(localSite.getLandingPageId()))
                    .findFirst().orElse(null));
            localSite
                    .getNavbarItems()
                    .addAll(
                            // add only the top level items, the children are already attached
                            navbarItems.stream().filter(item -> item.getParentNavbarItemId() == null).toList()
                    );
            localSite.setLanguageTextOverrides(languageTextDao.findByLocalSiteId(localSite.getId()));
            if (localSite.getFooterSectionId() != null) {
                localSite.setFooterSection(htmlSectionDao.find(localSite.getFooterSectionId()).get());
            }
        });
    }

    public int getNextVersion(String cleanFileName, String portalShortcode) {
        return jdbi.withHandle(handle ->
                handle.createQuery("select max(version) from " + tableName + " where clean_file_name = :cleanFileName" +
                                " and portal_shortcode = :portalShortcode")
                        .bind("cleanFileName", cleanFileName)
                        .bind("portalShortcode", portalShortcode)
                        .mapTo(int.class)
                        .one()
        ) + 1;
    }

    public List<SiteContent> findActiveContentByPortalId(UUID portalId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                                 select sc.* from site_content sc
                                 inner join portal_environment pe on sc.id = pe.site_content_id
                                 where pe.portal_id = :portalId
                                """)
                        .bind("portalId", portalId)
                        .mapTo(getClazz())
                        .list()
        );
    }
}
