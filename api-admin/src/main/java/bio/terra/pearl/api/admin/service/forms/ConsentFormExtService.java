package bio.terra.pearl.api.admin.service.forms;

import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.consent.ConsentForm;
import bio.terra.pearl.core.model.consent.StudyEnvironmentConsent;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.consent.ConsentFormService;
import bio.terra.pearl.core.service.study.StudyEnvironmentConsentService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class ConsentFormExtService {
  private AuthUtilService authUtilService;
  private ConsentFormService consentFormService;
  private StudyEnvironmentConsentService studyEnvironmentConsentService;
  private StudyEnvironmentService studyEnvironmentService;

  public ConsentFormExtService(
      AuthUtilService authUtilService,
      ConsentFormService consentFormService,
      StudyEnvironmentConsentService studyEnvironmentConsentService,
      StudyEnvironmentService studyEnvironmentService) {
    this.authUtilService = authUtilService;
    this.consentFormService = consentFormService;
    this.studyEnvironmentConsentService = studyEnvironmentConsentService;
    this.studyEnvironmentService = studyEnvironmentService;
  }

  public ConsentForm createNewVersion(
      String portalShortcode, ConsentForm consentForm, AdminUser user) {
    Portal portal = authUtilService.authUserToPortal(user, portalShortcode);
    return consentFormService.createNewVersion(portal.getId(), consentForm);
  }

  public ConsentForm create(String portalShortcode, ConsentForm consentForm, AdminUser user) {
    Portal portal = authUtilService.authUserToPortal(user, portalShortcode);
    consentForm.setPortalId(portal.getId());
    return consentFormService.create(consentForm);
  }

  public StudyEnvironmentConsent updateConfiguredConsent(
      String portalShortcode,
      EnvironmentName envName,
      String studyShortcode,
      StudyEnvironmentConsent updatedObj,
      AdminUser user) {
    authConfiguredConsentRequest(portalShortcode, envName, studyShortcode, updatedObj, user);
    StudyEnvironmentConsent existing =
        studyEnvironmentConsentService.find(updatedObj.getId()).get();
    BeanUtils.copyProperties(updatedObj, existing);
    return studyEnvironmentConsentService.update(existing);
  }

  public StudyEnvironmentConsent createConfiguredConsent(
      String portalShortcode,
      EnvironmentName envName,
      String studyShortcode,
      StudyEnvironmentConsent newObj,
      AdminUser user) {
    authConfiguredConsentRequest(portalShortcode, envName, studyShortcode, newObj, user);
    return studyEnvironmentConsentService.create(newObj);
  }

  /**
   * confirms the user has access to the study and that the consent belongs to that study, and that
   * it's in the sandbox environment. Returns the study environment for which the change is being
   * made in.
   */
  protected StudyEnvironment authConfiguredConsentRequest(
      String portalShortcode,
      EnvironmentName envName,
      String studyShortcode,
      StudyEnvironmentConsent updatedObj,
      AdminUser user) {
    authUtilService.authUserToStudy(user, portalShortcode, studyShortcode);
    StudyEnvironment studyEnv = studyEnvironmentService.findByStudy(studyShortcode, envName).get();
    if (!EnvironmentName.sandbox.equals(envName)
        || !EnvironmentName.sandbox.equals(studyEnv.getEnvironmentName())) {
      throw new IllegalArgumentException(
          "Updates can only be made directly to the sandbox environment".formatted(envName));
    }
    if (!studyEnv.getId().equals(updatedObj.getStudyEnvironmentId())) {
      throw new IllegalArgumentException(
              "Study environment id in request body does belong to this study");
    }
    return studyEnv;
  }
}
