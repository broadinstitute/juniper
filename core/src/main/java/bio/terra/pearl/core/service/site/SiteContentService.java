package bio.terra.pearl.core.service.site;

import bio.terra.pearl.core.dao.site.SiteContentDao;
import bio.terra.pearl.core.model.i18n.LanguageText;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.publishing.PortalEnvironmentChange;
import bio.terra.pearl.core.model.publishing.VersionedEntityChange;
import bio.terra.pearl.core.model.site.*;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.VersionedEntityService;
import bio.terra.pearl.core.service.publishing.PortalEnvPublishable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class SiteContentService extends VersionedEntityService<SiteContent, SiteContentDao> implements PortalEnvPublishable {
    private LocalizedSiteContentService localizedSiteContentService;

    public SiteContentService(SiteContentDao dao, LocalizedSiteContentService localizedSiteContentService) {
        super(dao);
        this.localizedSiteContentService = localizedSiteContentService;
    }

    /** attaches all the content (pages, sections, navbar) children for the given language to the SiteContent */
    public void attachChildContent(SiteContent siteContent, String language) {
        dao.attachChildContent(siteContent, language);
    }

    public void attachAllChildContent(SiteContent siteContent) {
        dao.attachAllChildContent(siteContent);
    }

    @Override
    public SiteContent create(SiteContent siteContent) {
        SiteContent savedSite = dao.create(siteContent);
        for (LocalizedSiteContent localSite : siteContent.getLocalizedSiteContents()) {
            localSite.setSiteContentId(savedSite.getId());
            LocalizedSiteContent savedLocal = localizedSiteContentService.create(localSite);
            savedSite.getLocalizedSiteContents().add(savedLocal);
        }
        return savedSite;
    }

    /**
     * create a new version of the siteContent with the given content.  This method ensures that everything about
     * the site content is copied, rather than referenced by id, so versions will be independent of each other.
     * Note that the passed-in object WILL BE MODIFIED to clean out any ids and set new versions.  It should be
     * discarded.
     * */
    @Transactional
    public SiteContent createNewVersion(SiteContent siteContent) {
        cleanForCopying(siteContent);
        int nextVersion = dao.getNextVersion(siteContent.getStableId(), siteContent.getPortalId());
        siteContent.setVersion(nextVersion);
        return create(siteContent);
    }

    @Override
    public void delete(UUID siteContentId,  Set<CascadeProperty> cascade) {
        List<LocalizedSiteContent> localSites = localizedSiteContentService.findBySiteContent(siteContentId);
        for (LocalizedSiteContent localSite : localSites) {
            localizedSiteContentService.delete(localSite.getId(), cascade);
        }
        dao.delete(siteContentId);
    }

    public List<SiteContent> findByPortalId(UUID portalId) {
        return dao.findByPortalId(portalId);
    }

    public void deleteByPortalId(UUID portalId) {
        List<SiteContent> siteContents = dao.findByPortalId(portalId);
        for (SiteContent siteContent : siteContents) {
            delete(siteContent.getId(), CascadeProperty.EMPTY_SET);
        }
    }

    /** strip out all ids so fresh copies of everything will be made in the DB */
    @Override
    public SiteContent cleanForCopying(SiteContent siteContent) {
        siteContent.cleanForCopying();
        siteContent.setPublishedVersion(null);
        siteContent.getLocalizedSiteContents().stream().forEach(lsc -> cleanForCopying(lsc));
        return siteContent;
    }

    protected void cleanForCopying(LocalizedSiteContent lsc) {
        lsc.cleanForCopying();
        lsc.getNavbarItems().stream().forEach(navbarItem -> cleanForCopying(navbarItem));
        lsc.getPages().stream().forEach(page -> cleanForCopying(page));
        lsc.getLanguageTextOverrides().stream().forEach(languageText -> cleanForCopying(languageText));
        cleanForCopying(lsc.getFooterSection());
        lsc.setFooterSectionId(null);
        cleanForCopying(lsc.getLandingPage());
        lsc.setLandingPageId(null);
        lsc.setSiteContentId(null);
    }

    protected void cleanForCopying(NavbarItem navbarItem) {
        navbarItem.cleanForCopying();
        if (navbarItem.getItems() != null) {
            navbarItem.getItems().stream().forEach(item -> cleanForCopying(item));
        }
    }

    protected  void cleanForCopying(HtmlPage htmlPage) {
        if (htmlPage != null) {
            htmlPage.cleanForCopying();
            htmlPage.setLocalizedSiteContentId(null);
            htmlPage.setId(null);
            htmlPage.getSections().stream().forEach(section -> cleanForCopying(section));
        }
    }

    protected void cleanForCopying(HtmlSection section) {
        if (section != null) {
            section.cleanForCopying();
            section.setHtmlPageId(null);
        }
    }

    protected void cleanForCopying(LanguageText languageText) {
        languageText.cleanForCopying();
        languageText.setLocalizedSiteContentId(null);
    }

    public List<SiteContent> findActiveContentByPortalId(UUID portalId) {
        return dao.findActiveContentByPortalId(portalId);
    }

    @Override
    public void loadForPublishing(PortalEnvironment portalEnv) {
        if (portalEnv.getSiteContentId() != null) {
            portalEnv.setSiteContent(find(portalEnv.getSiteContentId()).orElseThrow());
        }
    }

    @Override
    public void updateDiff(PortalEnvironmentChange change, PortalEnvironment sourceEnv, PortalEnvironment destEnv) {
        VersionedEntityChange<SiteContent> siteContentRecord = new VersionedEntityChange<SiteContent>(sourceEnv.getSiteContent(), destEnv.getSiteContent());
        change.setSiteContentChange(siteContentRecord);
    }

    @Override
    public void applyDiff(PortalEnvironmentChange change, PortalEnvironment destEnv) {

    }
}
