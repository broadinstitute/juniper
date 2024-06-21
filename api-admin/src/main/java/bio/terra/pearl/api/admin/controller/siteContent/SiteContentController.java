package bio.terra.pearl.api.admin.controller.siteContent;

import bio.terra.pearl.api.admin.api.SiteContentApi;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.siteContent.SiteContentExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.site.SiteContent;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class SiteContentController implements SiteContentApi {
  private AuthUtilService authUtilService;
  private HttpServletRequest request;
  private ObjectMapper objectMapper;
  private SiteContentExtService siteContentExtService;

  public SiteContentController(
      AuthUtilService authUtilService,
      HttpServletRequest request,
      ObjectMapper objectMapper,
      SiteContentExtService siteContentExtService) {
    this.authUtilService = authUtilService;
    this.request = request;
    this.objectMapper = objectMapper;
    this.siteContentExtService = siteContentExtService;
  }

  @Override
  public ResponseEntity<Object> get(String portalShortcode, String stableId, Integer version) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    Optional<SiteContent> siteContent =
        siteContentExtService.get(portalShortcode, stableId, version, operator);
    return ResponseEntity.of(siteContent.map(content -> content));
  }

  @Override
  public ResponseEntity<Object> getCurrent(String portalShortcode, String envName) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    Optional<SiteContent> siteContent =
        siteContentExtService.getCurrent(portalShortcode, environmentName, operator);
    return ResponseEntity.of(siteContent.map(content -> content));
  }

  @Override
  public ResponseEntity<Object> create(String portalShortcode, String stableId, Object body) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    SiteContent siteContent = objectMapper.convertValue(body, SiteContent.class);
    SiteContent savedContent =
        siteContentExtService.create(portalShortcode, stableId, siteContent, operator);
    return ResponseEntity.ok(savedContent);
  }

  @Override
  public ResponseEntity<Object> versionList(String portalShortcode, String stableId) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    List<SiteContent> contents =
        siteContentExtService.versionList(portalShortcode, stableId, operator);
    return ResponseEntity.ok(contents);
  }
}
