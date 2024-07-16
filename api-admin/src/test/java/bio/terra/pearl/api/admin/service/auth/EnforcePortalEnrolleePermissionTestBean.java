package bio.terra.pearl.api.admin.service.auth;

import bio.terra.pearl.api.admin.service.auth.context.PortalEnrolleeAuthContext;
import org.springframework.stereotype.Component;

@Component
public class EnforcePortalEnrolleePermissionTestBean {

  @EnforcePortalEnrolleePermission(permission = "participant_data_view")
  public void requiresParticipantDataView(PortalEnrolleeAuthContext authContext) {}

  @EnforcePortalEnrolleePermission(permission = AuthUtilService.BASE_PERMISSON)
  public void baseMethod(PortalEnrolleeAuthContext authContext) {}

  @EnforcePortalEnrolleePermission(permission = "participant_data_edit")
  public void noAuthContextMethod(String foo) {}
}
