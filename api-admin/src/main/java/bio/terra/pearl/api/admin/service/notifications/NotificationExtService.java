package bio.terra.pearl.api.admin.service.notifications;

import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.notification.Trigger;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.notification.NotificationContextInfo;
import bio.terra.pearl.core.service.notification.NotificationDispatcher;
import bio.terra.pearl.core.service.notification.TriggerService;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentConfigService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.rule.EnrolleeRuleData;
import bio.terra.pearl.core.service.rule.EnrolleeRuleService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.study.StudyService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class NotificationExtService {
  private TriggerService triggerService;
  private NotificationDispatcher notificationDispatcher;
  private EnrolleeService enrolleeService;
  private EnrolleeRuleService enrolleeRuleService;
  private AuthUtilService authUtilService;
  private StudyEnvironmentService studyEnvironmentService;
  private PortalEnvironmentService portalEnvironmentService;
  private PortalEnvironmentConfigService portalEnvironmentConfigService;
  private StudyService studyService;

  public NotificationExtService(
      TriggerService triggerService,
      NotificationDispatcher notificationDispatcher,
      EnrolleeService enrolleeService,
      EnrolleeRuleService enrolleeRuleService,
      AuthUtilService authUtilService,
      StudyEnvironmentService studyEnvironmentService,
      PortalEnvironmentService portalEnvironmentService,
      StudyService studyService) {
    this.triggerService = triggerService;
    this.notificationDispatcher = notificationDispatcher;
    this.enrolleeService = enrolleeService;
    this.enrolleeRuleService = enrolleeRuleService;
    this.authUtilService = authUtilService;
    this.studyEnvironmentService = studyEnvironmentService;
    this.portalEnvironmentService = portalEnvironmentService;
    this.studyService = studyService;
  }

  public Trigger sendAdHoc(
      AdminUser user,
      String portalShortcode,
      String studyShortcode,
      EnvironmentName envName,
      List<String> enrolleeShortcodes,
      Map<String, String> customMessages,
      UUID configId) {
    authUtilService.authUserToPortal(user, portalShortcode);
    authUtilService.authUserToStudy(user, portalShortcode, studyShortcode);
    StudyEnvironment studyEnv = studyEnvironmentService.findByStudy(studyShortcode, envName).get();

    List<Enrollee> enrollees = enrolleeService.findAllByShortcodes(enrolleeShortcodes);
    for (Enrollee enrollee : enrollees) {
      authUtilService.checkEnrolleeInStudyEnv(enrollee, studyEnv);
    }

    Trigger config = triggerService.find(configId).get();

    // bulk load the enrollees
    List<EnrolleeRuleData> enrolleeRuleData =
            enrolleeRuleService.fetchAllWithProfile(
            enrollees.stream().map(enrollee -> enrollee.getId()).toList());
    NotificationContextInfo context = notificationDispatcher.loadContextInfo(config);
    for (EnrolleeRuleData enrolleeRuleDatum : enrolleeRuleData) {
      notificationDispatcher.dispatchNotification(
          config, enrolleeRuleDatum, context, customMessages);
    }
    return config;
  }
}
