package bio.terra.pearl.core.service.address;

import bio.terra.pearl.core.model.address.AddressValidationResultDto;
import bio.terra.pearl.core.model.participant.MailingAddress;

public interface AddressValidationClient {

    AddressValidationResultDto validate(MailingAddress address) throws AddressValidationException;
}
