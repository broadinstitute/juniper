package bio.terra.pearl.core.service.site;

import bio.terra.pearl.core.dao.site.SiteMediaDao;
import bio.terra.pearl.core.model.site.SiteMedia;
import bio.terra.pearl.core.model.site.SiteMediaMetadata;
import bio.terra.pearl.core.service.ImmutableEntityService;
import bio.terra.pearl.core.service.portal.PortalService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class SiteMediaService extends ImmutableEntityService<SiteMedia, SiteMediaDao> {
    public static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("png", "jpeg", "jpg", "svg", "gif", "webp", "ico", "pdf", "json", "txt", "csv");
    private PortalService portalService;

    public SiteMediaService(SiteMediaDao dao, @Lazy PortalService portalService) {
        super(dao);
        this.portalService = portalService;
    }

    public void deleteByPortalShortcode(String portalShortcode) {
        dao.deleteByPortalShortcode(portalShortcode);
    }

    public Optional<SiteMedia> findOne(String portalShortcode, String cleanFileName, int version) {
        return dao.findOne(portalShortcode, cleanFileName, version);
    }

    public Optional<SiteMedia> findOneLatestVersion(String portalShortcode, String cleanFileName) {
        return dao.findOneLatestVersion(portalShortcode, cleanFileName);
    }

    public List<SiteMedia> findByPortal(String portalShortcode) {
        return dao.findByPortal(portalShortcode);
    }

    public List<SiteMediaMetadata> findMetadataByPortal(String portalShortcode) {
        return dao.findMetadataByPortal(portalShortcode);
    }

    @Override
    public SiteMedia create(SiteMedia image) {

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

    public int getNextVersion(String cleanFileName, String portalShortcode) {
        return dao.getNextVersion(cleanFileName, portalShortcode);
    }
}
