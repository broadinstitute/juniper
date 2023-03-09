package bio.terra.pearl.core.service.notification;

import bio.terra.pearl.core.model.notification.EmailTemplate;
import bio.terra.pearl.core.model.notification.Notification;
import bio.terra.pearl.core.model.notification.NotificationConfig;
import bio.terra.pearl.core.model.notification.NotificationDeliveryStatus;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.rule.EnrolleeRuleData;
import com.sendgrid.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService implements NotificationSender {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    public static final String EMAIL_REDIRECT_VAR = "env.email.redirectAllTo";
    public static final String SENDGRID_API_KEY_VAR = "env.email.sendgridApiKey";
    private PortalEnvironmentService portalEnvService;
    private PortalService portalService;
    private EmailTemplateService emailTemplateService;
    private final String sendGridApiKey;
    private String emailRedirectAddress = "";
    private NotificationService notificationService;

    public EmailService(PortalEnvironmentService portalEnvService,
                        PortalService portalService, EmailTemplateService emailTemplateService, Environment env,
                        NotificationService notificationService) {
        this.portalEnvService = portalEnvService;
        this.portalService = portalService;
        this.emailTemplateService = emailTemplateService;
        this.emailRedirectAddress = env.getProperty(EMAIL_REDIRECT_VAR, "");
        this.sendGridApiKey = env.getProperty(SENDGRID_API_KEY_VAR, "");
        this.notificationService = notificationService;
    }

    /** wrapper clas for all the config info we need to know to send emails */
    public record EmailEnvInfo(Portal portal, PortalEnvironment portalEnv, EmailTemplate template) {}

    /**
     * loads the environment information needed to send an email (things not specific to an enrollee/user)
     * this method will almost certainly benefit from caching, especially with respect to bulk emails
     */


    public EmailEnvInfo loadEnvConfigAndTemplate(NotificationConfig config) {
        PortalEnvironment portalEnvironment = portalEnvService.loadWithEnvConfig(config.getPortalEnvironmentId()).get();
        Portal portal = portalService.find(portalEnvironment.getPortalId()).get();
        return new EmailEnvInfo(
                portal,
                portalEnvironment,
                emailTemplateService.find(config.getEmailTemplateId()).get()
        );
    }

    @Async
    @Override
    public void processNotificationAsync(Notification notification, NotificationConfig config, EnrolleeRuleData ruleData) {
        processNotification(notification, config, ruleData);
    }

    public void processNotification(Notification notification, NotificationConfig config, EnrolleeRuleData ruleData) {
        if (!shouldSendEmail(config, ruleData)) {
            notification.setDeliveryStatus(NotificationDeliveryStatus.SKIPPED);
        } else {
            try {
                sendNotification(config, ruleData);
                notification.setDeliveryStatus(NotificationDeliveryStatus.SENT);
            } catch (Exception e) {
                notification.setDeliveryStatus(NotificationDeliveryStatus.FAILED);
                // don't log the exception itself since the trace might have PII in it.
                logger.error("Email failed to send: config: {}, enrollee: {}", config.getId(),
                        ruleData.enrollee().getShortcode());
            }
        }
        notificationService.update(notification);
    }

    /** skips processing, checks, and logging, and just sends the email */
    @Override
    public void sendTestNotification(NotificationConfig config, EnrolleeRuleData ruleData) throws Exception {
        sendNotification(config, ruleData);
    }

    protected void sendNotification(NotificationConfig config, EnrolleeRuleData ruleData) throws Exception {
        EmailEnvInfo emailEnv = loadEnvConfigAndTemplate(config);
        Mail mail = buildEmail(emailEnv.template, ruleData,
                emailEnv.portalEnv, emailEnv.portal.getShortcode());

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();

        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());
        sg.api(request);
    }

    public boolean shouldSendEmail(NotificationConfig config, EnrolleeRuleData ruleData) {
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
        return true;
    }

    public Mail buildEmail(EmailTemplate template, EnrolleeRuleData ruleData, PortalEnvironment portalEnv,
                           String portalShortcode) {
        Email from = new Email(portalEnv.getPortalEnvironmentConfig().getEmailSourceAddress());
        Email to = new Email(ruleData.profile().getContactEmail());

        StringSubstitutor stringSubstitutor = EnrolleeEmailSubstitutor.newSubstitutor(ruleData, portalEnv, portalShortcode);
        String subject = stringSubstitutor.replace(template.getSubject());
        String contentString = stringSubstitutor.replace(template.getBody());

        if (!StringUtils.isEmpty(emailRedirectAddress)) {
            to =  new Email(emailRedirectAddress);
            contentString = "<p><i>Redirected from " + ruleData.profile().getContactEmail()
                    + "</i></p>" + contentString;
        }

        Content content = new Content("text/html", contentString);
        return new Mail(from, subject, to, content);
    }

}
