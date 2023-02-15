package bio.terra.pearl.core.service.notification;

import bio.terra.pearl.core.model.notification.EmailTemplate;
import bio.terra.pearl.core.model.notification.NotificationConfig;
import bio.terra.pearl.core.model.portal.PortalEnvironmentConfig;
import bio.terra.pearl.core.service.portal.PortalEnvironmentConfigService;
import bio.terra.pearl.core.service.rule.EnrolleeRuleData;
import com.sendgrid.*;
import java.io.IOException;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService implements NotificationSender {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    public static final String TEST_TEMPLATE="Hello and welcome to email ${profileName}";
    private PortalEnvironmentConfigService portalEnvConfigService;
    private EmailTemplateService emailTemplateService;

    public EmailService(PortalEnvironmentConfigService portalEnvConfigService) {
        this.portalEnvConfigService = portalEnvConfigService;
    }

    @Async
    public void sendNotificationAsync(NotificationConfig config, EnrolleeRuleData ruleData) {
        sendNotification(config, ruleData);
    }

    public record EmailEnvConfig(PortalEnvironmentConfig portalEnvConfig, EmailTemplate template) {}

    /**
     * loads the environment information needed to send an email (things not specific to an enrollee/user)
     * this method will almost certainly benefit from caching, especially with respect to bulk emails
     */

    public EmailEnvConfig loadEnvConfigAndTemplate(NotificationConfig config) {
        return new EmailEnvConfig(
                portalEnvConfigService.findByPortalEnvId(config.getPortalEnvironmentId()).get(),
                emailTemplateService.find(config.getEmailTemplateId()).get()
        );
    }

    public void sendNotification(NotificationConfig config, EnrolleeRuleData ruleData) {
        EmailEnvConfig emailEnvConfig = loadEnvConfigAndTemplate(config);
        Mail mail = buildEmail(emailEnvConfig.template, ruleData, emailEnvConfig.portalEnvConfig);

        SendGrid sg = new SendGrid(System.getenv("SENDGRID_API_KEY"));
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            System.out.println(response.getStatusCode());
            System.out.println(response.getBody());
            System.out.println(response.getHeaders());
        } catch (IOException ex) {
            // don't log the exception itself since the trace might have PII in it.
            logger.error("Email failed to send");
        }
    }

    public Mail buildEmail(EmailTemplate template, EnrolleeRuleData ruleData, PortalEnvironmentConfig envConfig) {
        Email from = new Email(envConfig.getEmailSourceAddress());
        String subject = template.getSubject();
        Email to = new Email(ruleData.getProfile().getContactEmail());
        Content content = new Content("text/plain", buildTemplate(ruleData, envConfig));
        return new Mail(from, subject, to, content);
    }

    public String buildTemplate(EnrolleeRuleData ruleData, PortalEnvironmentConfig envConfig) {
        Map<String, Object> valueMap = Map.of("profile", ruleData.getProfile(),
        "portalConfig", envConfig);
        StringSubstitutor sub = new StringSubstitutor(valueMap);
        String resolvedTemplate = sub.replace(TEST_TEMPLATE);
        return resolvedTemplate;
    }

}
