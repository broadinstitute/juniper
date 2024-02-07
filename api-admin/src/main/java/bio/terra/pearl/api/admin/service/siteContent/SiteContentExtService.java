package bio.terra.pearl.api.admin.service.siteContent;

import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.site.*;
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
      String portalShortcode, String stableId, Integer version, String language, AdminUser user) {
    Portal portal = authUtilService.authUserToPortal(user, portalShortcode);
    Optional<SiteContent> siteContentOpt = siteContentService.findByStableId(stableId, version);
    if (siteContentOpt.isPresent() && siteContentOpt.get().getPortalId().equals(portal.getId())) {
      siteContentService.attachChildContent(siteContentOpt.get(), language);
      return siteContentOpt;
    }
    return Optional.empty();
  }

  public SiteContent create(
      String portalShortcode, String stableId, SiteContent siteContent, AdminUser user) {
    //TODO: Updating content for one version shouldn't destroy all other localizedSiteContents
    //      We should carry forward the existing and unmodified localizedSiteContents
    Portal portal = authUtilService.authUserToPortal(user, portalShortcode);
    siteContent.setPortalId(portal.getId());
    siteContent.setStableId(stableId);
    return siteContentService.createNewVersion(siteContent);
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
