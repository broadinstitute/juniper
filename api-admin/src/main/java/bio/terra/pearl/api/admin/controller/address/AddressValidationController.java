package bio.terra.pearl.api.admin.controller.address;

import bio.terra.pearl.api.admin.api.AddressValidationApi;
import bio.terra.pearl.core.model.address.AddressAutocompleteSearchDto;
import bio.terra.pearl.core.model.address.AddressValidationResultDto;
import bio.terra.pearl.core.model.address.MailingAddress;
import bio.terra.pearl.core.service.address.AddressValidationClient;
import bio.terra.pearl.core.service.address.AddressValidationClientProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
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

  /** Validates an address using either the stub or real client depending on configuration. */
  @Override
  public ResponseEntity<Object> validate(Object body) {
    MailingAddress mailingAddress = objectMapper.convertValue(body, MailingAddress.class);

    AddressValidationResultDto result = client.validate(mailingAddress);
    return ResponseEntity.ok(result);
  }

  /** Searches for possible valid addresses given a search parameter. */
  @Override
  public ResponseEntity<Object> autocomplete(Object body) {
    AddressAutocompleteSearchDto autocompleteSearchParams =
        objectMapper.convertValue(body, AddressAutocompleteSearchDto.class);

    List<MailingAddress> searchResults = client.autocomplete(autocompleteSearchParams.getSearch());
    return ResponseEntity.ok(searchResults);
  }
}
