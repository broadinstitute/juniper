package bio.terra.pearl.api.admin.controller.siteContent;

import bio.terra.pearl.api.admin.api.SiteContentApi;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.api.admin.service.siteContent.SiteContentExtService;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.site.SiteContent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
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
  public ResponseEntity<Object> get(
      String portalShortcode, String stableId, Integer version, String language) {
    if (StringUtils.isBlank(language)) {
      language = "en";
    }
    AdminUser adminUser = authUtilService.requireAdminUser(request);
    Optional<SiteContent> siteContent =
        siteContentExtService.get(portalShortcode, stableId, version, language, adminUser);
    return ResponseEntity.of(siteContent.map(content -> content));
  }

  @Override
  public ResponseEntity<Object> versionList(String portalShortcode, String stableId) {
    AdminUser adminUser = authUtilService.requireAdminUser(request);
    List<SiteContent> contents =
        siteContentExtService.versionList(portalShortcode, stableId, adminUser);
    return ResponseEntity.ok(contents);
  }
}
