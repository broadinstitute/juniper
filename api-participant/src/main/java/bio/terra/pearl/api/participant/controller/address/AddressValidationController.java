package bio.terra.pearl.api.participant.controller.address;

import bio.terra.pearl.api.participant.api.AddressValidationApi;
import bio.terra.pearl.core.model.address.AddressValidationResultDto;
import bio.terra.pearl.core.model.address.MailingAddress;
import bio.terra.pearl.core.service.address.AddressValidationServiceProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class AddressValidationController implements AddressValidationApi {

  ObjectMapper objectMapper;
  AddressValidationServiceProvider serviceProvider;

  AddressValidationController(
      ObjectMapper objectMapper, AddressValidationServiceProvider serviceProvider) {
    this.serviceProvider = serviceProvider;
    this.objectMapper = objectMapper;
  }

  /** Validates an address using either the stub or real client depending on configuration. */
  @Override
  public ResponseEntity<Object> validate(Object body) {
    MailingAddress mailingAddress = objectMapper.convertValue(body, MailingAddress.class);

    AddressValidationResultDto result = serviceProvider.get().validate(mailingAddress);
    return ResponseEntity.ok(result);
  }
}
