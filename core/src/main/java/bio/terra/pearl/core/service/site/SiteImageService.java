package bio.terra.pearl.core.service.site;

import bio.terra.pearl.core.dao.site.SiteImageDao;
import bio.terra.pearl.core.model.site.SiteContent;
import bio.terra.pearl.core.model.site.SiteImage;
import bio.terra.pearl.core.service.CrudService;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class SiteImageService extends CrudService<SiteImage, SiteImageDao> {
    public static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("png", "jpeg", "jpg", "svg", "gif");
    private SiteContentService siteContentService;
    public SiteImageService(SiteImageDao dao, @Lazy SiteContentService siteContentService) {
        super(dao);
        this.siteContentService = siteContentService;
    }

    public void deleteBySiteContent(UUID siteContentId) {
        dao.deleteBySiteContentId(siteContentId);
    }

    public Optional<SiteImage> findOne(String shortcode) {
        return dao.findOne(shortcode);
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

    public boolean isAllowedFileName(String uploadFileName) {
        Optional<String> ext =  getExtension(uploadFileName);
        return ext.isPresent() && ALLOWED_EXTENSIONS.contains(ext.get().toLowerCase());
    }

    /**
     * shortcode is the upload filename stripped of special characters and whitespace except ".", then lowercased
     */
    public static String generateShortcode(String uploadFileName,
                                           String siteContentStableId, int siteContentVersion) {
        String cleanFileName = uploadFileName.toLowerCase().replaceAll("[^a-z\\d\\.]", "");
        return  siteContentStableId + "_" + siteContentVersion + "_" + cleanFileName;
    }

    // from https://www.baeldung.com/java-file-extension
    public Optional<String> getExtension(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".") + 1));
    }
}
