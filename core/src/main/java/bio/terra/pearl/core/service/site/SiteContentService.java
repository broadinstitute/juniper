package bio.terra.pearl.core.service.site;

import bio.terra.pearl.core.dao.site.SiteContentDao;
import bio.terra.pearl.core.model.site.LocalizedSiteContent;
import bio.terra.pearl.core.model.site.SiteContent;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.CrudService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class SiteContentService extends CrudService<SiteContent, SiteContentDao> {
    private LocalizedSiteContentService localizedSiteContentService;
    private SiteImageService siteImageService;

    public SiteContentService(SiteContentDao dao,
                              LocalizedSiteContentService localizedSiteContentService,
                              SiteImageService siteImageService) {
        super(dao);
        this.localizedSiteContentService = localizedSiteContentService;
        this.siteImageService = siteImageService;
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
        siteImageService.deleteBySiteContent(siteContentId);
        dao.delete(siteContentId);
    }
}
