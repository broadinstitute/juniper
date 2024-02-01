package bio.terra.pearl.api.admin.controller.address;

import bio.terra.pearl.api.admin.api.AddressValidationApi;
import bio.terra.pearl.core.model.address.MailingAddress;
import bio.terra.pearl.core.service.address.AddressValidationClient;
import bio.terra.pearl.core.service.address.AddressValidationClientProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Objects;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class AddressValidationController implements AddressValidationApi {

  ObjectMapper objectMapper;
  AddressValidationClient client;

  AddressValidationController(
      ObjectMapper objectMapper, AddressValidationClientProvider clientProvider) {
    this.client = clientProvider.get();
    this.objectMapper = objectMapper;
  }

  /**
   * Validates an address using either the stub or real client depending on configuration. Users are
   * expected to use the provided sessionId after the first call.
   */
  @Override
  public ResponseEntity<Object> validate(Object body, String sessionId) {
    UUID sessionUUID;
    if (Objects.nonNull(sessionId) && !sessionId.isEmpty()) {
      sessionUUID = UUID.fromString(sessionId);
    } else {
      sessionUUID = UUID.randomUUID();
    }

    MailingAddress mailingAddress = objectMapper.convertValue(body, MailingAddress.class);

    return ResponseEntity.ok(client.validate(sessionUUID, mailingAddress));
  }
}
