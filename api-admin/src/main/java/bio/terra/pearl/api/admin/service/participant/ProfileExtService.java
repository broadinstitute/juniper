package bio.terra.pearl.api.admin.service.participant;

import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.EnforcePortalEnrolleePermission;
import bio.terra.pearl.api.admin.service.auth.context.PortalEnrolleeAuthContext;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.participant.Enrollee;
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

    verifyProfileBelongsToEnrollee(authContext.getEnrollee(), profile);

    return this.profileService.updateWithMailingAddress(
        profile,
        DataAuditInfo.builder()
            .responsibleAdminUserId(operator.getId())
            .enrolleeId(authContext.getEnrollee().getId())
            .justification(justification)
            .build());
  }

  private void verifyProfileBelongsToEnrollee(Enrollee enrollee, Profile profile) {

    Profile existingProfile =
        profileService
            .loadWithMailingAddress(enrollee.getProfileId())
            .orElseThrow(() -> new IllegalStateException("Invalid enrollee profile"));

    // make sure the profile id is the profile id for the enrollee
    if (Objects.nonNull(profile.getId())) {
      if (!profile.getId().equals(existingProfile.getId())) {
        throw new IllegalArgumentException("Profile does not belong to the enrollee");
      }
    } else {
      profile.setId(existingProfile.getId());
    }

    // make sure the mailing address id is the mailing address id for the enrollee
    if (Objects.nonNull(profile.getMailingAddress())) {
      if (Objects.nonNull(profile.getMailingAddress().getId())) {
        if (!profile.getMailingAddress().getId().equals(existingProfile.getMailingAddressId())) {
          throw new IllegalArgumentException("Mailing address does not belong to the enrollee");
        }
      } else {
        profile.getMailingAddress().setId(existingProfile.getMailingAddressId());
      }
    }
  }
}
