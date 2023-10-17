package bio.terra.pearl.api.admin.service.siteContent;

import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.site.SiteImage;
import bio.terra.pearl.core.service.site.SiteImageService;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class SiteImageExtService {
  private SiteImageService siteImageService;
  private AuthUtilService authUtilService;

  public SiteImageExtService(SiteImageService siteImageService, AuthUtilService authUtilService) {
    this.siteImageService = siteImageService;
    this.authUtilService = authUtilService;
  }

  public Optional<SiteImage> findOne(String portalShortcode, String cleanFileName, int version) {
    /**
     * NOTE: No auth check here, since this is used for admin preview mode which does not send auth
     * headers with image requests. It is not expected that images will ever need to be secret. That
     * said, if a way was found to include an auth header in preview mode image requests, it
     * wouldn't hurt to make this authenticated so that people don't have access to images that
     * might not be published on a public-facing portal yet.
     */
    return siteImageService.findOne(portalShortcode, cleanFileName, version);
  }

  public List<SiteImage> list(String portalShortcode, AdminUser operator) {
    authUtilService.authUserToPortal(operator, portalShortcode);
    return siteImageService.findByPortalWithoutDataColumn(portalShortcode);
  }
}
