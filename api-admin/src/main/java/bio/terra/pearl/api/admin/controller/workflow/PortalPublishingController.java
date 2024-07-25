package bio.terra.pearl.api.admin.controller.workflow;

import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.context.PortalAuthContext;
import bio.terra.pearl.api.admin.service.auth.context.PortalEnvAuthContext;
import bio.terra.pearl.api.admin.service.portal.PortalExtService;
import bio.terra.pearl.api.admin.service.portal.PortalPublishingExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.publishing.PortalEnvironmentChange;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class PortalPublishingController {
    private final HttpServletRequest request;
    private final AuthUtilService authUtilService;
    private final PortalPublishingExtService portalPublishingExtService;
    private final PortalExtService portalExtService;
    private final ObjectMapper objectMapper;

    public PortalPublishingController(HttpServletRequest request,
                                      AuthUtilService authUtilService,
                                      PortalPublishingExtService portalPublishingExtService,
                                      PortalExtService portalExtService,
                                      ObjectMapper objectMapper) {
        this.request = request;
        this.authUtilService = authUtilService;
        this.portalPublishingExtService = portalPublishingExtService;
        this.portalExtService = portalExtService;
        this.objectMapper = objectMapper;
    }


    @Override
    public ResponseEntity<Object> diff(String portalShortcode, String destEnv, String sourceEnv) {
        AdminUser operator = authUtilService.requireAdminUser(request);
        return ResponseEntity.ok(
                portalPublishingExtService.diff(PortalAuthContext.of(operator, portalShortcode),
                        EnvironmentName.valueOfCaseInsensitive(sourceEnv),
                        EnvironmentName.valueOfCaseInsensitive(destEnv));
    }

    @Override
    public ResponseEntity<Object> apply(
            String portalShortcode, String destEnv, Object body) {
        AdminUser operator = authUtilService.requireAdminUser(request);
        PortalEnvironmentChange change = objectMapper.convertValue(body, PortalEnvironmentChange.class);
        return ResponseEntity.ok(
                portalPublishingExtService.update(PortalEnvAuthContext.of(operator, portalShortcode, EnvironmentName.valueOfCaseInsensitive(destEnv)), change));
    }

    @Override
    public ResponseEntity<Object> getChangeRecords(String portalShortcode) {
        AdminUser operator = authUtilService.requireAdminUser(request);
        PortalEnvironment portalEnv = portalPublishingExtService.getChangeRecords(PortalAuthContext.of(operator, portalShortcode));
        return ResponseEntity.ok(portalEnv);
    }
}
