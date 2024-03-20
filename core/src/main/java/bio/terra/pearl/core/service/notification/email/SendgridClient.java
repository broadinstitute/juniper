package bio.terra.pearl.core.service.notification.email;

import bio.terra.pearl.core.model.notification.LocalizedEmailTemplate;
import bio.terra.pearl.core.model.notification.SendgridEvent;
import bio.terra.pearl.core.service.exception.internal.IOInternalException;
import bio.terra.pearl.core.service.notification.NotificationContextInfo;
import bio.terra.pearl.core.shared.ApplicationRoutingPaths;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sendgrid.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Service
@Slf4j
public class SendgridClient {
  public static final String EMAIL_REDIRECT_VAR = "env.email.redirectAllTo";
  public static final String SENDGRID_API_KEY_VAR = "env.email.sendgridApiKey";
  private final String sendGridApiKey;
  private String emailRedirectAddress = "";
  private final String deploymentZone;
  public SendgridClient(Environment env, ApplicationRoutingPaths applicationRoutingPaths) {
    this.sendGridApiKey = env.getProperty(SENDGRID_API_KEY_VAR, "");
    this.emailRedirectAddress = env.getProperty(EMAIL_REDIRECT_VAR, "");
    deploymentZone = applicationRoutingPaths.getDeploymentZone();
  }


  public void sendEmail(Mail mail) {
    if (StringUtils.isEmpty(sendGridApiKey)) {
      // if there's no API key, (likely because we're in a CI environment), don't even attempt to send an email
      log.info("Email send skipped: no sendgrid api provided");
      throw new UnsupportedOperationException("Attempted to send email without sendgrid key");
    }
    SendGrid sg = new SendGrid(sendGridApiKey);
    Request request = new Request();

    request.setMethod(Method.POST);
    request.setEndpoint("mail/send");
    try {
      request.setBody(mail.build());
      sg.api(request);
    } catch (IOException ex) {
      // this likely means the network failed, not that the email failed to send
      throw new IOInternalException("Error sending email", ex);
    }
  }

  public List<SendgridEvent> getEvents(Instant startDate, Instant endDate, int queryLimit) throws Exception {
    SendGrid sg = new SendGrid(sendGridApiKey);
    Request request = new Request();
    request.setMethod(Method.GET);

    String query = "(last_event_time " +
            "BETWEEN " +
              "TIMESTAMP \"" + startDate + "\" " +
            "AND " +
              "TIMESTAMP \"" + endDate + "\") ";

    request.setEndpoint("messages");
    request.addQueryParam("limit", Integer.toString(queryLimit));
    request.addQueryParam("query", query);
    Response response = sg.api(request);

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    JsonNode jsonNode = objectMapper.readTree(response.getBody());

    List<SendgridEvent> events = objectMapper.convertValue(jsonNode.get("messages"), new TypeReference<List<SendgridEvent>>() {});

    return events;
  }

  public Mail buildEmail(NotificationContextInfo contextInfo, String toAddress, String fromAddress, String fromName,
                         StringSubstitutor stringSubstitutor, String language) {
    Email from = new Email(fromAddress);
    Email to = new Email(toAddress);

    if (fromName == null) {
      fromName = "Juniper";
    }
    if (!deploymentZone.equalsIgnoreCase("prod")) {
      fromName += " (%s)".formatted(deploymentZone);
    }
    from.setName(fromName);

    LocalizedEmailTemplate localizedEmailTemplate = contextInfo.template().getLocalizedEmailTemplates().stream().filter(
        template -> template.getLanguage().equals(language)).findFirst().get(); //todo: handle missing language

    String subject = stringSubstitutor.replace(localizedEmailTemplate.getSubject());
    String contentString = stringSubstitutor.replace(localizedEmailTemplate.getBody());

    if (!StringUtils.isEmpty(emailRedirectAddress)) {
      to =  new Email(emailRedirectAddress);
      contentString = "<p><i>Redirected from " + toAddress + "</i></p>" + contentString;
    }

    Content content = new Content("text/html", contentString);
    return new Mail(from, subject, to, content);
  }

}
