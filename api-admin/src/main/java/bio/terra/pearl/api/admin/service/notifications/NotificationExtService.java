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
import bio.terra.pearl.core.service.rule.EnrolleeContext;
import bio.terra.pearl.core.service.rule.EnrolleeContextService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.study.StudyService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class NotificationExtService {
  private TriggerService triggerService;
  private NotificationDispatcher notificationDispatcher;
  private EnrolleeService enrolleeService;
  private EnrolleeContextService enrolleeContextService;
  private AuthUtilService authUtilService;
  private StudyEnvironmentService studyEnvironmentService;
  private PortalEnvironmentService portalEnvironmentService;
  private PortalEnvironmentConfigService portalEnvironmentConfigService;
  private StudyService studyService;

  public NotificationExtService(
      TriggerService triggerService,
      NotificationDispatcher notificationDispatcher,
      EnrolleeService enrolleeService,
      EnrolleeContextService enrolleeContextService,
      AuthUtilService authUtilService,
      StudyEnvironmentService studyEnvironmentService,
      PortalEnvironmentService portalEnvironmentService,
      StudyService studyService) {
    this.triggerService = triggerService;
    this.notificationDispatcher = notificationDispatcher;
    this.enrolleeService = enrolleeService;
    this.enrolleeContextService = enrolleeContextService;
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
    List<EnrolleeContext> enrolleeRuleData =
            enrolleeContextService.fetchData(
            enrollees.stream().map(enrollee -> enrollee.getId()).toList());
    NotificationContextInfo context = notificationDispatcher.loadContextInfo(config);
    for (EnrolleeContext enrolleeRuleDatum : enrolleeRuleData) {
      notificationDispatcher.dispatchNotification(
          config, enrolleeRuleDatum, context, customMessages);
    }
    return config;
  }
}
