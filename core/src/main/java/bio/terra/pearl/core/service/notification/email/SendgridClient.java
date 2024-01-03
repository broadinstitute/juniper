package bio.terra.pearl.core.service.notification.email;

import bio.terra.pearl.core.model.notification.SendgridEvent;
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


  public String sendEmail(Mail mail) throws Exception {
    if (StringUtils.isEmpty(sendGridApiKey)) {
      // if there's no API key, (likely because we're in a CI environment), don't even attempt to send an email
      log.info("Email send skipped: no sendgrid api provided");
      throw new UnsupportedOperationException("Attempted to send email without sendgrid key");
    }
    SendGrid sg = new SendGrid(sendGridApiKey);
    Request request = new Request();

    request.setMethod(Method.POST);
    request.setEndpoint("mail/send");
    request.setBody(mail.build());
    Response response = sg.api(request);

    //The X-Message-Id that's returned by SendGrid here is not the same as the Message-Id. The X-Message-Id is a unique
    //identifier that corresponds to the API request, which could have been for a batch of emails (thus, the X-Message-Id
    //could correspond to multiple emails in our system). The Message-Id is a unique identifier for the email itself, after
    //SendGrid has broken up the batch and processed the messages individually. Since we don't have that information until
    //after the email has been captured in our activity logs, the X-Message-Id is the only way to tie an API request to a
    //specific email in the system. Fortunately, the X-Message-Id is the prefix of the Message-Id (see example below).

    //We only send emails in batches of 1, so the ambiguity is not an issue for us right now. We also key off of the recipient
    //email address when utilizing this information, to try to make it a bit more airtight.

    //For example: the Message-Id XBg2anf2TqCy6WXKQFhieQ.filter0905p1mdw1-4434-59E0C6FF-3.0 would correspond
    // to X-Message-Id XBg2anf2TqCy6WXKQFhieQ
    return response.getHeaders().get("X-Message-Id");
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
                         StringSubstitutor stringSubstitutor) {
    Email from = new Email(fromAddress);
    Email to = new Email(toAddress);

    if (fromName == null) {
      fromName = "Juniper";
    }
    if (!deploymentZone.equalsIgnoreCase("prod")) {
      fromName += " (%s)".formatted(deploymentZone);
    }
    from.setName(fromName);

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
