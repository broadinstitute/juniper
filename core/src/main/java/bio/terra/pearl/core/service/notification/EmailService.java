package bio.terra.pearl.core.service.notification;

import bio.terra.pearl.core.model.notification.Notification;
import bio.terra.pearl.core.model.notification.NotificationConfig;
import bio.terra.pearl.core.model.notification.NotificationDeliveryStatus;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.rule.EnrolleeRuleData;
import bio.terra.pearl.core.service.study.StudyService;
import com.sendgrid.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService implements NotificationSender {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    public static final String EMAIL_REDIRECT_VAR = "env.email.redirectAllTo";
    public static final String SENDGRID_API_KEY_VAR = "env.email.sendgridApiKey";
    private final String sendGridApiKey;
    private String emailRedirectAddress = "";
    private NotificationService notificationService;
    private PortalEnvironmentService portalEnvService;
    private PortalService portalService;
    private StudyService studyService;
    private EmailTemplateService emailTemplateService;

    public EmailService(Environment env, NotificationService notificationService,
                        PortalEnvironmentService portalEnvService, PortalService portalService,
                        StudyService studyService, EmailTemplateService emailTemplateService) {
        this.emailRedirectAddress = env.getProperty(EMAIL_REDIRECT_VAR, "");
        this.sendGridApiKey = env.getProperty(SENDGRID_API_KEY_VAR, "");
        this.notificationService = notificationService;
        this.portalEnvService = portalEnvService;
        this.portalService = portalService;
        this.studyService = studyService;
        this.emailTemplateService = emailTemplateService;
    }
    
    @Async
    @Override
    public void processNotificationAsync(Notification notification, NotificationConfig config, EnrolleeRuleData ruleData) {
        NotificationContextInfo contextInfo = loadContextInfo(config);
        processNotification(notification, config, ruleData, contextInfo);
    }

    public void processNotification(Notification notification, NotificationConfig config, EnrolleeRuleData ruleData,
                                    NotificationContextInfo contextInfo) {
        if (!shouldSendEmail(config, ruleData, contextInfo)) {
            notification.setDeliveryStatus(NotificationDeliveryStatus.SKIPPED);
        } else {
            notification.setSentTo(ruleData.profile().getContactEmail());
            try {
                buildAndSendEmail(contextInfo, ruleData);
                logger.info("Email sent: config: {}, enrollee: {}", config.getId(),
                        ruleData.enrollee().getShortcode());
                notification.setDeliveryStatus(NotificationDeliveryStatus.SENT);
            } catch (Exception e) {
                notification.setDeliveryStatus(NotificationDeliveryStatus.FAILED);
                // don't log the exception itself since the trace might have PII in it.
                logger.error("Email failed to send: config: {}, enrollee: {}", config.getId(),
                        ruleData.enrollee().getShortcode());
            }
        }
        if (notification.getId() != null) {
            // the notification might have been saved, but in a transaction not-yet completed (if, for example,
            // study enrollment transaction is taking a long time). So retry the update if it fails
            RetryTemplate retryTemplate = RetryTemplate.defaultInstance();
            FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
            backOffPolicy.setBackOffPeriod(2000);  // this will retry once every two seconds for 3 tries
            retryTemplate.setBackOffPolicy(backOffPolicy);
            retryTemplate.execute(arg -> notificationService.update(notification));
        } else {
            notificationService.create(notification);
        }
    }

    /**
     * skips processing, checks, and logging, and just sends the email. Should only be used for debugging and
     * test emails, since we want all regular emails to be logged via notifications in standard ways.
     * */
    @Override
    public void sendTestNotification(NotificationConfig config, EnrolleeRuleData ruleData) throws Exception {
        NotificationContextInfo contextInfo = loadContextInfo(config);
        buildAndSendEmail(contextInfo, ruleData);
    }

    protected void buildAndSendEmail(NotificationContextInfo contextInfo, EnrolleeRuleData ruleData) throws Exception {
        Mail mail = buildEmail(contextInfo, ruleData);
        sendEmail(mail);
    }


    protected void sendEmail(Mail mail) throws Exception {
        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();

        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());
        sg.api(request);
    }

    public boolean shouldSendEmail(NotificationConfig config,
                                   EnrolleeRuleData ruleData,
                                   NotificationContextInfo contextInfo) {
        if (ruleData.profile() != null && ruleData.profile().isDoNotEmail()) {
            logger.info("skipping email, enrollee {} is doNotEmail: notificationConfig: {}, portalEnv: {}",
                    ruleData.enrollee().getShortcode(), config.getId(), config.getPortalEnvironmentId());
            return false;
        }
        if (config.getEmailTemplateId() == null) {
            logger.error("no email template configured: notificationConfig: {}, portalEnv: {}",
                    config.getId(), config.getPortalEnvironmentId());
            return false;
        }
        if (StringUtils.isEmpty(sendGridApiKey)) {
            // we'll usually want to avoid sending emails from CI environments
            logger.info("Email send skipped: no sendgrid api provided");
            return false;
        }
        if (contextInfo == null) {
            // the environment hasn't finished populating yet, skip
            logger.info("Email send skipped: no environment context could be loaded");
            return false;
        }
        return true;
    }

    public Mail buildEmail(NotificationContextInfo contextInfo, EnrolleeRuleData ruleData) {
        Email from = new Email(contextInfo.portalEnv().getPortalEnvironmentConfig().getEmailSourceAddress());
        Email to = new Email(ruleData.profile().getContactEmail());

        StringSubstitutor stringSubstitutor = EnrolleeEmailSubstitutor
                .newSubstitutor(ruleData, contextInfo);
        String subject = stringSubstitutor.replace(contextInfo.template().getSubject());
        String contentString = stringSubstitutor.replace(contextInfo.template().getBody());

        if (!StringUtils.isEmpty(emailRedirectAddress)) {
            to =  new Email(emailRedirectAddress);
            contentString = "<p><i>Redirected from " + ruleData.profile().getContactEmail()
                    + "</i></p>" + contentString;
        }

        Content content = new Content("text/html", contentString);
        return new Mail(from, subject, to, content);
    }

    /**
     * loads the context information needed to send a notification (things not specific to an enrollee/user)
     * this method will almost certainly benefit from caching, especially with respect to bulk emails.
     *
     * This can return null if called in an async context where the notificationConfig points to an
     * environment that either no longer exists or has not yet been populated (e.g. during a populate_portal.sh call)
     */
    @Override
    public NotificationContextInfo loadContextInfo(NotificationConfig config) {
        PortalEnvironment portalEnvironment = portalEnvService.loadWithEnvConfig(config.getPortalEnvironmentId()).orElse(null);
        if (portalEnvironment == null) {
            return null;
        }

        Study study = studyService.findByStudyEnvironmentId(config.getStudyEnvironmentId()).get();

        Portal portal = portalService.find(portalEnvironment.getPortalId()).get();
        return new NotificationContextInfo(
                portal,
                portalEnvironment,
                study,
                emailTemplateService.find(config.getEmailTemplateId()).orElse(null)
        );
    }
}
