package bio.terra.pearl.api.admin.service.portal;

import bio.terra.pearl.api.admin.service.auth.*;
import bio.terra.pearl.api.admin.service.auth.context.OperatorAuthContext;
import bio.terra.pearl.api.admin.service.auth.context.PortalAuthContext;
import bio.terra.pearl.api.admin.service.auth.context.PortalEnvAuthContext;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.portal.PortalEnvironmentConfig;
import bio.terra.pearl.core.model.portal.PortalEnvironmentLanguage;
import bio.terra.pearl.core.service.portal.PortalEnvironmentConfigService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentLanguageService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.portal.exception.PortalConfigMissing;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class PortalExtService {
  private final PortalService portalService;
  private final PortalEnvironmentService portalEnvironmentService;
  private final PortalEnvironmentConfigService portalEnvironmentConfigService;
  private final PortalEnvironmentLanguageService portalEnvironmentLanguageService;
  private final AuthUtilService authUtilService;

  public PortalExtService(
      PortalService portalService,
      PortalEnvironmentService portalEnvironmentService,
      PortalEnvironmentConfigService portalEnvironmentConfigService,
      PortalEnvironmentLanguageService portalEnvironmentLanguageService,
      AuthUtilService authUtilService) {
    this.portalService = portalService;
    this.portalEnvironmentService = portalEnvironmentService;
    this.portalEnvironmentConfigService = portalEnvironmentConfigService;
    this.portalEnvironmentLanguageService = portalEnvironmentLanguageService;
    this.authUtilService = authUtilService;
  }

  @EnforcePortalPermission(permission = AuthUtilService.BASE_PERMISSION)
  public Portal fullLoad(PortalAuthContext authContext, String language) {
    return portalService.fullLoad(authContext.getPortal(), language);
  }

  /** gets all the portals the user has access to, and attaches the corresponding studies */
  @AnyAdminUser
  public List<Portal> getAll(OperatorAuthContext authContext) {
    // no additional auth checks needed -- the underlying service filters out portals the user does
    // not have access to
    List<Portal> portals = portalService.findByAdminUser(authContext.getOperator());
    portalService.attachPortalEnvironments(portals);
    portalService.attachStudies(portals);
    return portals;
  }

  @EnforcePortalEnvPermission(permission = "study_settings_edit")
  @SuperuserOnly // superuser only since this could change, e.g. the destination url which might
  // impact other portals
  public PortalEnvironmentConfig updateConfig(
      PortalEnvAuthContext authContext, PortalEnvironmentConfig newConfig) {
    PortalEnvironmentConfig config =
        portalEnvironmentConfigService
            .find(authContext.getPortalEnvironment().getPortalEnvironmentConfigId())
            .orElseThrow(PortalConfigMissing::new);
    BeanUtils.copyProperties(newConfig, config, "id", "createdAt");
    if (StringUtils.isBlank(config.getParticipantHostname())) {
      config.setParticipantHostname(null);
    }
    if (StringUtils.isBlank(config.getMixpanelToken())) {
      config.setMixpanelToken(null);
    }
    config = portalEnvironmentConfigService.update(config);
    return config;
  }

  /**
   * updates a portal environment, currently only supports updating the siteContent and preReg
   * survey. Does not update any nested lists (e.g. portal languages)
   */
  @EnforcePortalEnvPermission(permission = "study_settings_edit")
  @SandboxOnly
  public PortalEnvironment updateEnvironment(
      PortalEnvAuthContext authContext, PortalEnvironment updatedEnv) {

    if (updatedEnv.getPreRegSurveyId() != null) {
      authUtilService.authSurveyToPortal(authContext.getPortal(), updatedEnv.getPreRegSurveyId());
    }
    PortalEnvironment portalEnv = authContext.getPortalEnvironment();
    portalEnv.setSiteContentId(updatedEnv.getSiteContentId());
    portalEnv.setPreRegSurveyId(updatedEnv.getPreRegSurveyId());
    portalEnv = portalEnvironmentService.update(portalEnv);
    return portalEnv;
  }

  @EnforcePortalEnvPermission(permission = "study_settings_edit")
  @SandboxOnly
  public List<PortalEnvironmentLanguage> setLanguages(
      PortalEnvAuthContext authContext, List<PortalEnvironmentLanguage> languages) {
    List<PortalEnvironmentLanguage> updatedLangs =
        portalEnvironmentLanguageService.setPortalEnvLanguages(
            authContext.getPortalEnvironment().getId(), languages);
    return updatedLangs;
  }
}
