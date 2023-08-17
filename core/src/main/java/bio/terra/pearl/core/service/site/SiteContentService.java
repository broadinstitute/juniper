package bio.terra.pearl.core.service.site;

import bio.terra.pearl.core.dao.site.SiteContentDao;
import bio.terra.pearl.core.model.site.LocalizedSiteContent;
import bio.terra.pearl.core.model.site.SiteContent;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.ImmutableEntityService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class SiteContentService extends ImmutableEntityService<SiteContent, SiteContentDao> {
    private LocalizedSiteContentService localizedSiteContentService;

    public SiteContentService(SiteContentDao dao, LocalizedSiteContentService localizedSiteContentService) {
        super(dao);
        this.localizedSiteContentService = localizedSiteContentService;
    }

    public Optional<SiteContent> findByStableId(String stableId, int version) {
        return dao.findOne(stableId, version);
    }

    public List<SiteContent> findByStableId(String stableId) {
        return dao.findByStableId(stableId);
    }

    /** attaches all the content (pages, sections, navbar) children for the given language to the SiteContent */
    public void attachChildContent(SiteContent siteContent, String language) {
        dao.attachChildContent(siteContent, language);
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

    @Override
    public void delete(UUID siteContentId,  Set<CascadeProperty> cascade) {
        List<LocalizedSiteContent> localSites = localizedSiteContentService.findBySiteContent(siteContentId);
        for (LocalizedSiteContent localSite : localSites) {
            localizedSiteContentService.delete(localSite.getId(), cascade);
        }
        dao.delete(siteContentId);
    }

    public void deleteByPortalId(UUID portalId) {
        List<SiteContent> siteContents = dao.findByPortalId(portalId);
        for (SiteContent siteContent : siteContents) {
            delete(siteContent.getId(), CascadeProperty.EMPTY_SET);
        }
    }

    public int getNextVersion(String stableId) {
        return dao.getNextVersion(stableId);
    }
}
