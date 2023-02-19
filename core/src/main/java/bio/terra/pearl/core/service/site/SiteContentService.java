package bio.terra.pearl.core.service.site;

import bio.terra.pearl.core.dao.site.SiteContentDao;
import bio.terra.pearl.core.model.site.LocalizedSiteContent;
import bio.terra.pearl.core.model.site.SiteContent;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.CrudService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class SiteContentService extends CrudService<SiteContent, SiteContentDao> {
    private LocalizedSiteContentService localizedSiteContentService;

    public SiteContentService(SiteContentDao dao, LocalizedSiteContentService localizedSiteContentService) {
        super(dao);
        this.localizedSiteContentService = localizedSiteContentService;
    }

    public Optional<SiteContent> findOne(String stableId, int version) {
        return dao.findOne(stableId, version);
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
}
