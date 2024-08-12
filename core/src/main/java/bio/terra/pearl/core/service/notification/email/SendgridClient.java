package bio.terra.pearl.core.service.notification.email;

import bio.terra.pearl.core.model.notification.LocalizedEmailTemplate;
import bio.terra.pearl.core.model.notification.SendgridEvent;
import bio.terra.pearl.core.service.exception.internal.IOInternalException;
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


  public String sendEmail(Mail mail) {
    if (StringUtils.isEmpty(sendGridApiKey)) {
      // if there's no API key, (likely because we're in a CI environment), don't even attempt to send an email
      log.warn("Email send skipped: no sendgrid api provided");
      if (deploymentZone.equalsIgnoreCase("prod")) {
        throw new UnsupportedOperationException("Attempted to send email without sendgrid key");
      }
      return null;
    }
    SendGrid sg = new SendGrid(sendGridApiKey);
    Request request = new Request();

    request.setMethod(Method.POST);
    request.setEndpoint("mail/send");
    try {
      request.setBody(mail.build());
      Response response = sg.api(request);
      // X-Message-Id identifies an individual SendGrid API request
      // We need to track this so we can correlate SendGrid events with notifications
      // Note that X-Message-Id is not guaranteed to be 1:1 with an individual message.
      // Messages can be sent as a batch, and the X-Message-Id will be the same for all of them.
      // Currently, Juniper only sends messages one at a time, so this is a 1:1 mapping for now.
      // If we ever start batching emails, we'll need to update this to be a bit more sophisticated
      // and use the Sendgrid Event Webhook. But in either case, we still need to track this ID.
      return response.getHeaders().get("X-Message-Id");
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

  public Mail buildEmail(LocalizedEmailTemplate localizedEmailTemplate,
                         String toAddress,
                         String fromAddress,
                         String fromName,
                         StringSubstitutor stringSubstitutor) {
    Email from = buildFrom(fromAddress, fromName);
    Email to = buildTo(toAddress);


    String subject = buildSubject(localizedEmailTemplate, stringSubstitutor);
    String contentString = buildContent(localizedEmailTemplate, stringSubstitutor, toAddress);


    Content content = new Content("text/html", contentString);
    return new Mail(from, subject, to, content);
  }

  public Mail buildMultiRecipientMail(LocalizedEmailTemplate localizedEmailTemplate,
                                      List<String> ccAddresses,
                                      String fromAddress,
                                      String fromName,
                                      StringSubstitutor stringSubstitutor) {

    Email from = buildFrom(fromAddress, fromName);


    String subject = buildSubject(localizedEmailTemplate, stringSubstitutor);
    String contentString = buildContent(localizedEmailTemplate, stringSubstitutor, StringUtils.join(ccAddresses, ", "));

    Content content = new Content("text/html", contentString);

    Mail mail = new Mail();
    mail.setFrom(from);
    mail.setSubject(subject);
    mail.addContent(content);


    if (!StringUtils.isEmpty(emailRedirectAddress)) {
      ccAddresses = List.of(emailRedirectAddress);
    }
    Personalization to = new Personalization();
    for (String ccAddress : ccAddresses) {
      Email ccEmail = new Email(ccAddress);
      to.addTo(ccEmail);
    }

    mail.addPersonalization(to);

    return mail;
  }

  private Email buildFrom(String fromAddress, String fromName) {
    Email from = new Email(fromAddress);

    if (fromName == null) {
      fromName = "Juniper";
    }
    if (!deploymentZone.equalsIgnoreCase("prod")) {
      fromName += " (%s)".formatted(deploymentZone);
    }
    from.setName(fromName);

    return from;
  }

  private Email buildTo(String toAddress) {
    if (!StringUtils.isEmpty(emailRedirectAddress)) {
      return new Email(emailRedirectAddress);
    }

    return new Email(toAddress);
  }

  private String buildSubject(LocalizedEmailTemplate localizedEmailTemplate, StringSubstitutor stringSubstitutor) {
    return stringSubstitutor.replace(localizedEmailTemplate.getSubject());
  }

  private String buildContent(LocalizedEmailTemplate localizedEmailTemplate,
                              StringSubstitutor stringSubstitutor,
                              String toAddress) {
    String contentString = stringSubstitutor.replace(localizedEmailTemplate.getBody());
    if (!StringUtils.isEmpty(emailRedirectAddress)) {
      contentString = "<p><i>Redirected from " + toAddress + "</i></p>" + contentString;
    }

    return contentString;
  }

}
