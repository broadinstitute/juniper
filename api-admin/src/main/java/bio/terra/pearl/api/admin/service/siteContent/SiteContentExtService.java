package bio.terra.pearl.api.admin.service.siteContent;

import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.portal.PortalEnvironmentLanguage;
import bio.terra.pearl.core.model.site.SiteContent;
import bio.terra.pearl.core.service.portal.PortalEnvironmentLanguageService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.site.SiteContentService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class SiteContentExtService {
  private final AuthUtilService authUtilService;
  private final SiteContentService siteContentService;
  private final PortalEnvironmentLanguageService portalEnvironmentLanguageService;
  private final PortalEnvironmentService portalEnvironmentService;

  public SiteContentExtService(
      AuthUtilService authUtilService,
      SiteContentService siteContentService,
      PortalEnvironmentLanguageService portalEnvironmentLanguageService,
      PortalEnvironmentService portalEnvironmentService) {
    this.authUtilService = authUtilService;
    this.siteContentService = siteContentService;
    this.portalEnvironmentLanguageService = portalEnvironmentLanguageService;
    this.portalEnvironmentService = portalEnvironmentService;
  }

  public Optional<SiteContent> get(
      String portalShortcode, String stableId, Integer version, AdminUser user) {
    Portal portal = authUtilService.authUserToPortal(user, portalShortcode);
    PortalEnvironment portalEnv =
        portalEnvironmentService
            .findOne(portal.getShortcode(), EnvironmentName.sandbox)
            .orElseThrow();

    Optional<SiteContent> siteContentOpt =
        siteContentService.findByStableId(stableId, version, portal.getId());
    if (siteContentOpt.isEmpty()) {
      return Optional.empty();
    }
    return loadSiteContent(siteContentOpt.get().getId(), portalEnv);
  }

  public Optional<SiteContent> getCurrent(
      String portalShortcode, EnvironmentName environmentName, AdminUser operator) {
    Portal portal = authUtilService.authUserToPortal(operator, portalShortcode);
    PortalEnvironment portalEnv =
        portalEnvironmentService.findOne(portal.getShortcode(), environmentName).orElseThrow();
    return loadSiteContent(portalEnv.getSiteContentId(), portalEnv);
  }

  public Optional<SiteContent> loadSiteContent(UUID siteContentId, PortalEnvironment portalEnv) {
    Optional<SiteContent> siteContentOpt = siteContentService.find(siteContentId);
    List<PortalEnvironmentLanguage> languages =
        portalEnvironmentLanguageService.findByPortalEnvId(portalEnv.getId());
    if (siteContentOpt.isPresent()
        && siteContentOpt.get().getPortalId().equals(portalEnv.getPortalId())) {
      for (PortalEnvironmentLanguage lang : languages) {
        siteContentService.attachChildContent(siteContentOpt.get(), lang.getLanguageCode());
      }
      return siteContentOpt;
    }
    return Optional.empty();
  }

  public SiteContent create(
      String portalShortcode, String stableId, SiteContent siteContent, AdminUser user) {
    Portal portal = authUtilService.authUserToPortal(user, portalShortcode);
    siteContent.setPortalId(portal.getId());
    siteContent.setStableId(stableId);
    return siteContentService.createNewVersion(siteContent);
  }

  public List<SiteContent> versionList(String portalShortcode, String stableId, AdminUser user) {
    Portal portal = authUtilService.authUserToPortal(user, portalShortcode);
    List<SiteContent> contents = siteContentService.findByStableId(stableId, portal.getId());
    // filter out any that aren't associated with this portal
    List<SiteContent> contentsInPortal =
        contents.stream().filter(content -> content.getPortalId().equals(portal.getId())).toList();
    return contentsInPortal;
  }
}
