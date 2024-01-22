package bio.terra.pearl.api.admin.service.participant;

import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.workflow.DataAuditInfo;
import bio.terra.pearl.core.service.participant.ProfileService;
import org.springframework.stereotype.Service;

@Service
public class ProfileExtService {
  private AuthUtilService authUtilService;
  private ProfileService profileService;

  public ProfileExtService(AuthUtilService authUtilService, ProfileService profileService) {
    this.authUtilService = authUtilService;
    this.profileService = profileService;
  }

  /**
   * Updates the profile on behalf of the enrollee; requires a justification for auditing purposes.
   */
  public Profile updateProfileForEnrollee(
      AdminUser operator, String enrolleeShortcode, String justification, Profile profile) {
    Enrollee enrollee = authUtilService.authAdminUserToEnrollee(operator, enrolleeShortcode);
    return this.profileService.updateWithMailingAddress(
        profile,
        DataAuditInfo.builder()
            .responsibleAdminUserId(operator.getId())
            .enrolleeId(enrollee.getId())
            .justification(justification)
            .build());
  }
}
