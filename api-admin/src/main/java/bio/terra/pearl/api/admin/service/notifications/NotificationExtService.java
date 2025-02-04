package bio.terra.pearl.api.admin.service.notifications;

import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.EnforcePortalStudyEnvPermission;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyEnvAuthContext;
import bio.terra.pearl.api.admin.service.enrollee.EnrolleeSearchExtService;
import bio.terra.pearl.core.model.notification.Trigger;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.search.EnrolleeSearchExpressionResult;
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
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class NotificationExtService {
  private final EnrolleeSearchExtService enrolleeSearchExtService;
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
          StudyService studyService, EnrolleeSearchExtService enrolleeSearchExtService) {
    this.triggerService = triggerService;
    this.notificationDispatcher = notificationDispatcher;
    this.enrolleeService = enrolleeService;
    this.enrolleeContextService = enrolleeContextService;
    this.authUtilService = authUtilService;
    this.studyEnvironmentService = studyEnvironmentService;
    this.portalEnvironmentService = portalEnvironmentService;
    this.studyService = studyService;
    this.enrolleeSearchExtService = enrolleeSearchExtService;
  }

  @EnforcePortalStudyEnvPermission(permission = "participant_data_edit")
  public Trigger sendAdHoc(
          PortalStudyEnvAuthContext authContext,
          List<String> enrolleeShortcodes,
          Map<String, String> customMessages,
          UUID configId) {

    List<Enrollee> enrollees = enrolleeService.findAllByShortcodes(enrolleeShortcodes);
    for (Enrollee enrollee : enrollees) {
      authUtilService.checkEnrolleeInStudyEnv(enrollee, authContext.getStudyEnvironment());
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

  @EnforcePortalStudyEnvPermission(permission = "participant_data_edit")
  public Trigger sendAdHoc(
          PortalStudyEnvAuthContext authContext,
          String expression,
          Map<String, String> customMessages,
          UUID configId) {

    List<EnrolleeSearchExpressionResult> enrollees = enrolleeSearchExtService.executeSearchExpression(authContext, expression, null, List.of());

    return sendAdHoc(authContext, enrollees.stream().map(result -> result.getEnrollee().getShortcode()).toList(), customMessages, configId);
  }
}
