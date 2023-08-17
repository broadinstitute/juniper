package bio.terra.pearl.api.admin.service.siteContent;

import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.site.SiteContent;
import bio.terra.pearl.core.service.site.SiteContentService;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class SiteContentExtService {
  private AuthUtilService authUtilService;
  private SiteContentService siteContentService;

  public SiteContentExtService(
      AuthUtilService authUtilService, SiteContentService siteContentService) {
    this.authUtilService = authUtilService;
    this.siteContentService = siteContentService;
  }

  public Optional<SiteContent> get(
      String portalShortcode, String stableId, Integer version, AdminUser user) {
    Portal portal = authUtilService.authUserToPortal(user, portalShortcode);
    Optional<SiteContent> siteContent = siteContentService.findByStableId(stableId, version);
    if (siteContent.isPresent() && !siteContent.get().getPortalId().equals(portal.getId())) {
      return Optional.empty();
    }
    return siteContent;
  }

  public List<SiteContent> versionList(String portalShortcode, String stableId, AdminUser user) {
    Portal portal = authUtilService.authUserToPortal(user, portalShortcode);
    List<SiteContent> contents = siteContentService.findByStableId(stableId);
    // filter out any that aren't associated with this portal
    List<SiteContent> contentsInPortal =
        contents.stream().filter(content -> content.getPortalId().equals(portal.getId())).toList();
    return contentsInPortal;
  }
}
