package bio.terra.pearl.core.service.site;

import bio.terra.pearl.core.dao.site.SiteImageDao;
import bio.terra.pearl.core.model.site.SiteImage;
import bio.terra.pearl.core.service.ImmutableEntityService;
import bio.terra.pearl.core.service.portal.PortalService;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class SiteImageService extends ImmutableEntityService<SiteImage, SiteImageDao> {
    public static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("png", "jpeg", "jpg", "svg", "gif", "webp");
    private PortalService portalService;
    public SiteImageService(SiteImageDao dao, @Lazy PortalService portalService) {
        super(dao);
        this.portalService = portalService;
    }

    public void deleteByPortalShortcode(String portalShortcode) {
        dao.deleteByPortalShortcode(portalShortcode);
    }

    public Optional<SiteImage> findOne(String portalShortcode, String cleanFileName, int version) {
        return dao.findOne(portalShortcode, cleanFileName, version);
    }

    @Override
    public SiteImage create(SiteImage image) {
        if (!isAllowedFileName(image.getUploadFileName())) {
            throw new IllegalArgumentException("Allowed extensions are: " +
                    ALLOWED_EXTENSIONS.stream().collect(Collectors.joining(", ")));
        }
        if (image.getCleanFileName() == null) {
            image.setCleanFileName(cleanFileName(image.getUploadFileName()));
        } else {
            // confirm the clean file name is actually clean
            image.setCleanFileName(cleanFileName(image.getCleanFileName()));
        }
        return dao.create(image);
    }

    public boolean isAllowedFileName(String uploadFileName) {
        Optional<String> ext =  getExtension(uploadFileName);
        return ext.isPresent() && ALLOWED_EXTENSIONS.contains(ext.get().toLowerCase());
    }

    // from https://www.baeldung.com/java-file-extension
    public Optional<String> getExtension(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".") + 1));
    }

    /**
     * A cleanFileName is a portal-scoped-unique, URL-safe identifier.
     * cleanFileName is the upload filename with whitespace replaced with _,
     * stripped of special characters and whitespace except "." "_" or "-", then lowercased.
     */
    public static String cleanFileName(String uploadFileName) {
        return uploadFileName.toLowerCase()
                .replaceAll("\\s", "_")
                .replaceAll("[^a-z\\d\\._\\-]", "");
    }
}
