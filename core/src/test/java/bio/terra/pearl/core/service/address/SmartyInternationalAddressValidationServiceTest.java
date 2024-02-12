package bio.terra.pearl.core.service.address;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.model.address.AddressValidationResultDto;
import bio.terra.pearl.core.model.address.MailingAddress;
import com.smartystreets.api.SmartySerializer;
import com.smartystreets.api.exceptions.SmartyException;
import com.smartystreets.api.international_street.Candidate;
import com.smartystreets.api.international_street.Lookup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;

class SmartyInternationalAddressValidationServiceTest extends BaseSpringBootTest {

    @MockBean
    private SmartyClient mockSmartyClient;

    // all the candidates below come from real responses via
    // https://www.smarty.com/products/us-address-verification#demo

    @Autowired
    private SmartyInternationalAddressValidationService client;

    @Test
    public void testValidResponse() throws SmartyException, IOException, InterruptedException {
        // input: 14 Frenchman Dr Port Elliot SA 5212 AUS
        mockResponse(
        """
                   {
                     "address1": "14 Frenchman Dr",
                     "address2": "Port Elliot SA 5212",
                     "components": {
                       "administrative_area": "SA",
                       "country_iso_3": "AUS",
                       "locality": "Port Elliot",
                       "postal_code": "5212",
                       "postal_code_short": "5212",
                       "premise": "14",
                       "premise_number": "14",
                       "thoroughfare": "Frenchman Dr",
                       "thoroughfare_name": "Frenchman",
                       "thoroughfare_trailing_type": "Dr"
                     },
                     "metadata": {
                       "latitude": -35.528348,
                       "longitude": 138.685215,
                       "geocode_precision": "Premise",
                       "max_geocode_precision": "Premise",
                       "address_format": "premise thoroughfare|locality administrative_area postal_code"
                     },
                     "analysis": {
                       "verification_status": "Verified",
                       "address_precision": "DeliveryPoint",
                       "max_address_precision": "DeliveryPoint",
                       "changes": {
                         "components": {
                           "administrative_area": "Verified-NoChange",
                           "country_iso_3": "Added",
                           "locality": "Verified-NoChange",
                           "postal_code": "Verified-NoChange",
                           "postal_code_short": "Verified-NoChange",
                           "premise": "Verified-NoChange",
                           "premise_number": "Verified-NoChange",
                           "thoroughfare": "Verified-NoChange",
                           "thoroughfare_name": "Identified-ContextChange",
                           "thoroughfare_trailing_type": "Identified-NoChange"
                         }
                       }
                     }
                   }
                """
        );
        AddressValidationResultDto result = client.validate(new MailingAddress());

        Assertions.assertTrue(result.isValid());
        Assertions.assertEquals("14 Frenchman Dr", result.getSuggestedAddress().getStreet1());
        Assertions.assertNull(result.getInvalidComponents());
        Assertions.assertFalse(result.getHasInferredComponents());
    }


    void mockResponse(String jsonResponse) throws SmartyException, IOException, InterruptedException {

        SmartySerializer serializer = new SmartySerializer();
        Candidate candidate = serializer.deserialize(jsonResponse.getBytes(), Candidate.class);

        Answer<Void> answer = invocation -> {

            Lookup arg = invocation.getArgument(0);
            Candidate[] candidates = {candidate};
            arg.setResult(candidates);
            return null;
        };

        Mockito.doAnswer(answer).when(mockSmartyClient).send(any(Lookup.class));
    }
}