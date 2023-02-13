package bio.terra.pearl.core.service.notification;

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

    @Async
    public void sendNotificationAsync() {
        sendNotification();
    }

    public void sendNotification() {
        Mail mail = buildEmail();
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

    public Mail buildEmail() {
        Email from = new Email("dbush@broadinstitute.org");
        String subject = "Test D2P email";
        Email to = new Email("dbush@broadinstitute.org");
        Content content = new Content("text/plain", buildTemplate());
        return new Mail(from, subject, to, content);
    }

    public String buildTemplate() {
        Map<String, String> valueMap = Map.of("profileName", "Murf Hippo");
        StringSubstitutor sub = new StringSubstitutor(valueMap);
        String resolvedTemplate = sub.replace(TEST_TEMPLATE);
        return resolvedTemplate;
    }

}
