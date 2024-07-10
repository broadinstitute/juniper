package bio.terra.pearl.api.admin.service.participant;

import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.EnforcePortalEnrolleePermission;
import bio.terra.pearl.api.admin.service.auth.context.PortalEnrolleeAuthContext;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.service.participant.ProfileService;
import bio.terra.pearl.core.service.portal.PortalService;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class ProfileExtService {
  private final AuthUtilService authUtilService;
  private final ProfileService profileService;
  private final PortalService portalService;

  public ProfileExtService(
      AuthUtilService authUtilService, ProfileService profileService, PortalService portalService) {
    this.authUtilService = authUtilService;
    this.profileService = profileService;
    this.portalService = portalService;
  }

  /**
   * Updates the profile on behalf of the enrollee; requires a justification for auditing purposes.
   */
  @EnforcePortalEnrolleePermission(permission = "participant_data_edit")
  public Profile updateProfileForEnrollee(
      PortalEnrolleeAuthContext authContext, String justification, Profile profile) {
    AdminUser operator = authContext.getOperator();
    // make sure the profile is for the enrollee
    profile.setId(authContext.getEnrollee().getProfileId());
    if (Objects.nonNull(profile.getMailingAddress())) {
      // make sure we're updating the mailing address for the enrollee
      profile
          .getMailingAddress()
          .setId(authContext.getEnrollee().getProfile().getMailingAddressId());
    }

    return this.profileService.updateWithMailingAddress(
        profile,
        DataAuditInfo.builder()
            .responsibleAdminUserId(operator.getId())
            .enrolleeId(authContext.getEnrollee().getId())
            .justification(justification)
            .build());
  }
}
