package bio.terra.pearl.api.admin.service.siteContent;

import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.site.SiteMedia;
import bio.terra.pearl.core.model.site.SiteMediaMetadata;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.site.SiteMediaService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class SiteMediaExtService {
  private SiteMediaService siteMediaService;
  private AuthUtilService authUtilService;

  public SiteMediaExtService(SiteMediaService siteMediaService, AuthUtilService authUtilService) {
    this.siteMediaService = siteMediaService;
    this.authUtilService = authUtilService;
  }

  public Optional<SiteMedia> findOne(String portalShortcode, String cleanFileName, int version) {
    /**
     * NOTE: No auth check here, since this is used for admin preview mode which does not send auth
     * headers with image requests. It is not expected that images will ever need to be secret. That
     * said, if a way was found to include an auth header in preview mode image requests, it
     * wouldn't hurt to make this authenticated so that people don't have access to images that
     * might not be published on a public-facing portal yet.
     */
    return siteMediaService.findOne(portalShortcode, cleanFileName, version);
  }

  public Optional<SiteMedia> findLatest(String portalShortcode, String cleanFileName) {
    return siteMediaService.findOneLatestVersion(portalShortcode, cleanFileName);
  }

  public List<SiteMediaMetadata> list(String portalShortcode, AdminUser operator) {
    authUtilService.authUserToPortal(operator, portalShortcode);
    return siteMediaService.findMetadataByPortal(portalShortcode);
  }

  public SiteMedia upload(
      String portalShortcode, String uploadFileName, byte[] imageData, AdminUser operator) {
    authUtilService.authUserToPortal(operator, portalShortcode);
    String cleanFileName = SiteMediaService.cleanFileName(uploadFileName);
    int version = siteMediaService.getNextVersion(cleanFileName, portalShortcode);
    SiteMedia image =
        SiteMedia.builder()
            .portalShortcode(portalShortcode)
            .version(version)
            .data(imageData)
            .uploadFileName(uploadFileName)
            .cleanFileName(cleanFileName)
            .build();
    // the create method handles cleaning and converting the uploadFileName to a cleanFileName
    return siteMediaService.create(image);
  }

  public void delete(String portalShortcode, UUID id, AdminUser operator) {
    authUtilService.authUserToPortal(operator, portalShortcode);
    siteMediaService.delete(id, CascadeProperty.EMPTY_SET);
  }
}
