package bio.terra.pearl.core.service.notification.email;

import bio.terra.pearl.core.model.notification.*;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.notification.NotificationContextInfo;
import bio.terra.pearl.core.service.notification.NotificationSender;
import bio.terra.pearl.core.service.notification.NotificationService;
import bio.terra.pearl.core.service.notification.substitutors.EnrolleeEmailSubstitutor;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.rule.EnrolleeContext;
import bio.terra.pearl.core.service.study.StudyService;
import bio.terra.pearl.core.shared.ApplicationRoutingPaths;
import com.sendgrid.Mail;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
public class EnrolleeEmailService implements NotificationSender {
    private final NotificationService notificationService;
    private final PortalEnvironmentService portalEnvService;
    private final PortalService portalService;
    private final StudyService studyService;
    private final EmailTemplateService emailTemplateService;
    private final ApplicationRoutingPaths routingPaths;
    private final SendgridClient sendgridClient;

    public EnrolleeEmailService(NotificationService notificationService,
                                PortalEnvironmentService portalEnvService, PortalService portalService,
                                StudyService studyService, EmailTemplateService emailTemplateService,
                                ApplicationRoutingPaths routingPaths, SendgridClient sendgridClient) {
        this.notificationService = notificationService;
        this.portalEnvService = portalEnvService;
        this.portalService = portalService;
        this.studyService = studyService;
        this.emailTemplateService = emailTemplateService;
        this.routingPaths = routingPaths;
        this.sendgridClient = sendgridClient;
    }

    @Async
    @Override
    public void processNotificationAsync(Notification notification, Trigger config, EnrolleeContext ruleData) {
        NotificationContextInfo contextInfo = loadContextInfo(config);
        processNotification(notification, config, ruleData, contextInfo);
    }

    public void processNotification(Notification notification, Trigger config, EnrolleeContext ruleData,
                                    NotificationContextInfo contextInfo) {
        if (!shouldSendEmail(config, ruleData, contextInfo)) {
            notification.setDeliveryStatus(NotificationDeliveryStatus.SKIPPED);
        } else {
            notification.setSentTo(ruleData.getProfile().getContactEmail());
            try {
                String sendGridApiRequestId = buildAndSendEmail(contextInfo, ruleData, notification);
                log.info("Email sent: config: {}, enrollee: {}, language: {}", config.getId(),
                        ruleData.getEnrollee().getShortcode(), ruleData.getProfile().getPreferredLanguage());
                notification.setDeliveryStatus(NotificationDeliveryStatus.SENT);
                notification.setSendgridApiRequestId(sendGridApiRequestId);
            } catch (Exception e) {
                notification.setDeliveryStatus(NotificationDeliveryStatus.FAILED);
                // don't log the exception itself since the trace might have PII in it.
                log.error("Email failed to send: config: {}, enrollee: {}, language: {}", config.getId(),
                        ruleData.getEnrollee().getShortcode(), ruleData.getProfile().getPreferredLanguage());
            }
        }
        // now save/update the Notification object
        if (notification.getId() != null) {
            // the notification might have been saved, but in a transaction not-yet completed (if, for example,
            // study enrollment transaction is taking a long time). So retry the update if it fails
            RetryTemplate retryTemplate = RetryTemplate.defaultInstance();
            // exponential backoff with a max interval of 32 seconds, so we'll make retry attempts after 4, 8, 16, and 32 seconds
            ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
            backOffPolicy.setInitialInterval(4000);
            backOffPolicy.setMaxInterval(32000);
            retryTemplate.setBackOffPolicy(backOffPolicy);
            try {
                retryTemplate.execute(arg -> notificationService.update(notification));
            } catch (Exception e) {
                if (routingPaths.getDeploymentZone().equals("local") &&
                        ruleData.getEnrollee().getShortcode().endsWith("GONE")) {
                    // for these participants, they are deleted before the async process to send out the welcome
                    // email starts, so the notification update will fail. This is expected and not a problem.
                    log.info("notification update failed for populated withdrawn participant -- this is expected");
                } else {
                    log.error("failed to update notification: {}, portal: {}, trigger: {}, error: {}",
                            notification.getId(), contextInfo.portal().getShortcode(), config.getId(), e.getMessage());
                }
            }

        } else {
            notificationService.create(notification);
        }
    }

    /**
     * skips processing, checks, and logging, and just sends the email. Should only be used for debugging and
     * test emails, since we want all regular emails to be logged via notifications in standard ways.
     */
    @Override
    public void sendTestNotification(Trigger config, EnrolleeContext ruleData) {
        NotificationContextInfo contextInfo = loadContextInfo(config);
        buildAndSendEmail(contextInfo, ruleData, new Notification());
    }

    protected String buildAndSendEmail(NotificationContextInfo contextInfo, EnrolleeContext ruleData,
                                       Notification notification) {
        Mail mail = buildEmail(contextInfo, ruleData, notification);
        return sendgridClient.sendEmail(mail);
    }

    protected Mail buildEmail(NotificationContextInfo contextInfo, EnrolleeContext ruleData, Notification notification) {
        String preferredLanguage = ruleData.getProfile().getPreferredLanguage();
        LocalizedEmailTemplate localizedEmailTemplate = getPreferredTemplateWithDefault(contextInfo.template(), preferredLanguage);

        StringSubstitutor substitutor = EnrolleeEmailSubstitutor
                .newSubstitutor(ruleData, contextInfo, routingPaths, notification.getCustomMessagesMap());
        String fromAddress = contextInfo.portalEnvConfig().getEmailSourceAddress();
        if (fromAddress == null) {
            // if this portal environment hasn't been configured with a specific email, just send from the support address
            fromAddress = routingPaths.getSupportEmailAddress();
        }
        String fromName = "Juniper";
        if (contextInfo.portal().getName() != null) {
            fromName = contextInfo.portal().getName();
        }

        if (!contextInfo.portalEnv().getEnvironmentName().isLive()) {
            fromName += " (%s)".formatted(contextInfo.portalEnv().getEnvironmentName());
        }

        Mail mail = sendgridClient.buildEmail(
                localizedEmailTemplate,
                ruleData.getProfile().getContactEmail(),
                fromAddress,
                fromName,
                substitutor);
        return mail;
    }

    public boolean shouldSendEmail(Trigger config,
                                   EnrolleeContext enrolleeContext,
                                   NotificationContextInfo contextInfo) {
        if (enrolleeContext.getProfile() == null || StringUtils.isBlank(enrolleeContext.getProfile().getContactEmail())) {
            return false;  // no address to send email to
        }
        if (enrolleeContext.getProfile().isDoNotEmail()) {
            log.info("skipping email, enrollee {} is doNotEmail: triggerId: {}, portalEnv: {}",
                    enrolleeContext.getEnrollee().getShortcode(), config.getId(), config.getPortalEnvironmentId());
            return false;
        }
        if (enrolleeContext.getProfile().isDoNotEmailSolicit() && Objects.nonNull(config.getTaskType()) && config.getTaskType().equals(TaskType.OUTREACH)) {
            log.info("skipping email, enrollee {} is doNotEmailSolicit: triggerId: {}, portalEnv: {}",
                    enrolleeContext.getEnrollee().getShortcode(), config.getId(), config.getPortalEnvironmentId());
            return false;
        }
        if (config.getEmailTemplateId() == null) {
            log.error("no email template configured: triggerId: {}, portalEnv: {}",
                    config.getId(), config.getPortalEnvironmentId());
            return false;
        }
        if (contextInfo == null) {
            // the environment hasn't finished populating yet, skip
            log.info("Email send skipped: no environment context could be loaded");
            return false;
        }
        return true;
    }

    /**
     * loads the context information needed to send a notification (things not specific to an enrollee/user)
     * this method will almost certainly benefit from caching, especially with respect to bulk emails.
     * This can return null if called in an async context where the trigger points to an
     * environment that either no longer exists or has not yet been populated (e.g. during a populate_portal.sh call)
     */
    @Override
    public NotificationContextInfo loadContextInfo(Trigger config) {
        PortalEnvironment portalEnvironment = portalEnvService.loadWithEnvConfig(config.getPortalEnvironmentId()).orElse(null);
        if (portalEnvironment == null) {
            return null;
        }

        Study study = studyService.findByStudyEnvironmentId(config.getStudyEnvironmentId()).get();

        EmailTemplate emailTemplate = emailTemplateService.find(config.getEmailTemplateId()).orElse(null);
        if (emailTemplate != null) {
            emailTemplateService.attachLocalizedTemplates(emailTemplate);
        }

        Portal portal = portalService.find(portalEnvironment.getPortalId()).get();
        return new NotificationContextInfo(
                portal,
                portalEnvironment,
                portalEnvironment.getPortalEnvironmentConfig(),
                study,
                emailTemplate
        );
    }

    public LocalizedEmailTemplate getPreferredTemplateWithDefault(EmailTemplate template, String preferredLanguage) {
        //TODO JN-863 eventually this will take in a portalEnvironment which will have a defaultLanguage
        // attached to it. We should use that here instead of hard-coding English
        return template.getTemplateForLanguage(preferredLanguage).orElseGet(() -> template.getTemplateForLanguage("en").get());
    }
}
