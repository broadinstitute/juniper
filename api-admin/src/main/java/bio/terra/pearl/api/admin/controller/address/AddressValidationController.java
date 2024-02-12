package bio.terra.pearl.api.admin.controller.address;

import bio.terra.pearl.api.admin.api.AddressValidationApi;
import bio.terra.pearl.core.model.address.AddressValidationResultDto;
import bio.terra.pearl.core.model.address.MailingAddress;
import bio.terra.pearl.core.service.address.AddressValidationService;
import bio.terra.pearl.core.service.address.AddressValidationServiceProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class AddressValidationController implements AddressValidationApi {

  ObjectMapper objectMapper;
  AddressValidationService client;

  AddressValidationController(
          ObjectMapper objectMapper, AddressValidationServiceProvider clientProvider) {
    this.client = clientProvider.get();
    this.objectMapper = objectMapper;
  }

  /** Validates an address using either the stub or real client depending on configuration. */
  @Override
  public ResponseEntity<Object> validate(Object body) {
    MailingAddress mailingAddress = objectMapper.convertValue(body, MailingAddress.class);

    AddressValidationResultDto result = client.validate(mailingAddress);
    return ResponseEntity.ok(result);
  }
}
