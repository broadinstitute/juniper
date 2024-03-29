package bio.terra.pearl.core.service.address;

import bio.terra.pearl.core.model.address.AddressValidationResultDto;
import bio.terra.pearl.core.model.address.MailingAddress;

public interface AddressValidationService {

    AddressValidationResultDto validate(MailingAddress address) throws AddressValidationException;
}
