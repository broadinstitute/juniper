package bio.terra.pearl.core.service.notification.email;

import bio.terra.pearl.core.service.notification.NotificationContextInfo;
import com.sendgrid.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class SendgridClient {
  private static final Logger logger = LoggerFactory.getLogger(SendgridClient.class);
  public static final String EMAIL_REDIRECT_VAR = "env.email.redirectAllTo";
  public static final String SENDGRID_API_KEY_VAR = "env.email.sendgridApiKey";
  private final String sendGridApiKey;
  private String emailRedirectAddress = "";

  public SendgridClient(Environment env) {
    this.sendGridApiKey = env.getProperty(SENDGRID_API_KEY_VAR, "");
    this.emailRedirectAddress = env.getProperty(EMAIL_REDIRECT_VAR, "");
  }


  public void sendEmail(Mail mail) throws Exception {
    if (StringUtils.isEmpty(sendGridApiKey)) {
      // if there's no API key, (likely because we're in a CI environment), don't even attempt to send an email
      logger.info("Email send skipped: no sendgrid api provided");
      throw new UnsupportedOperationException("Attempted to send email without sendgrid key");
    }
    SendGrid sg = new SendGrid(sendGridApiKey);
    Request request = new Request();

    request.setMethod(Method.POST);
    request.setEndpoint("mail/send");
    request.setBody(mail.build());
    sg.api(request);
  }

  public Mail buildEmail(NotificationContextInfo contextInfo, String toAddress, String fromAddress,
                         StringSubstitutor stringSubstitutor) {
    Email from = new Email(fromAddress);
    Email to = new Email(toAddress);

    String fromName = "Juniper";
    if (contextInfo.portal() != null) {
      // Set the 'from' name on the email to the portal name
      from.setName(contextInfo.portal().getName());
    }

    String subject = stringSubstitutor.replace(contextInfo.template().getSubject());
    String contentString = stringSubstitutor.replace(contextInfo.template().getBody());

    if (!StringUtils.isEmpty(emailRedirectAddress)) {
      to =  new Email(emailRedirectAddress);
      contentString = "<p><i>Redirected from " + toAddress + "</i></p>" + contentString;
    }

    Content content = new Content("text/html", contentString);
    return new Mail(from, subject, to, content);
  }

}
