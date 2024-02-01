package bio.terra.pearl.core.service.address;

import bio.terra.pearl.core.model.address.AddressValidationResultDto;
import bio.terra.pearl.core.model.address.MailingAddress;

import java.util.UUID;

public interface AddressValidationClient {

    AddressValidationResultDto validate(UUID sessionId, MailingAddress address) throws AddressValidationException;
}
