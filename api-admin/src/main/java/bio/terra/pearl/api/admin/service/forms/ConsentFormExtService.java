package bio.terra.pearl.api.admin.service.forms;

import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.consent.ConsentForm;
import bio.terra.pearl.core.model.consent.StudyEnvironmentConsent;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.service.consent.ConsentFormService;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import bio.terra.pearl.core.service.study.StudyEnvironmentConsentService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class ConsentFormExtService {
  private AuthUtilService authUtilService;
  private ConsentFormService consentFormService;
  private StudyEnvironmentConsentService studyEnvironmentConsentService;

  public ConsentFormExtService(
      AuthUtilService authUtilService, ConsentFormService consentFormService) {
    this.authUtilService = authUtilService;
    this.consentFormService = consentFormService;
  }

  public ConsentForm createNewVersion(
      String portalShortcode, ConsentForm consentForm, AdminUser user) {
    Portal portal = authUtilService.authUserToPortal(user, portalShortcode);
    return consentFormService.createNewVersion(portal.getId(), consentForm);
  }

  public StudyEnvironmentConsent updateConfiguredConsent(
      String portalShortcode,
      EnvironmentName envName,
      StudyEnvironmentConsent updatedObj,
      AdminUser user) {
    authUtilService.authUserToPortal(user, portalShortcode);
    if (user.isSuperuser() || EnvironmentName.sandbox.equals(envName)) {
      StudyEnvironmentConsent existing =
          studyEnvironmentConsentService.find(updatedObj.getId()).get();
      BeanUtils.copyProperties(updatedObj, existing);
      return studyEnvironmentConsentService.update(existing);
    }
    throw new PermissionDeniedException(
        "You do not have permission to update the {} environment".formatted(envName));
  }
}
