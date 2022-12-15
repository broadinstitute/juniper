package bio.terra.pearl.core.service.site;

import bio.terra.pearl.core.dao.site.SiteImageDao;
import bio.terra.pearl.core.model.site.SiteContent;
import bio.terra.pearl.core.model.site.SiteImage;
import bio.terra.pearl.core.service.CrudService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SiteImageService extends CrudService<SiteImage, SiteImageDao> {
    private SiteContentService siteContentService;
    public SiteImageService(SiteImageDao dao, @Lazy SiteContentService siteContentService) {
        super(dao);
        this.siteContentService = siteContentService;
    }

    public void deleteBySiteContent(UUID siteContentId) {
        dao.deleteBySiteContentId(siteContentId);
    }

    @Override
    public SiteImage create(SiteImage image) {
        if (image.getShortcode() == null) {
            SiteContent content = siteContentService.find(image.getSiteContentId()).get();
            image.setShortcode(generateShortcode(
                    image.getUploadFileName(),
                    content.getStableId(),
                    content.getVersion()
            ));
        }
        return dao.create(image);
    }

    public static String generateShortcode(String uploadFileName,
                                           String siteContentStableId, int siteContentVersion) {
        String cleanFileName = uploadFileName.toLowerCase().replaceAll("[^a-z0-9]", "");
        return  siteContentStableId + "_" + siteContentVersion + "_" + cleanFileName;
    }
}
