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
    // https://www.smarty.com/products/international-address-verification

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
        AddressValidationResultDto result = client.validate(MailingAddress.builder().country("MX").build());

        Assertions.assertTrue(result.isValid());
        Assertions.assertEquals("14 Frenchman Dr", result.getSuggestedAddress().getStreet1());
        Assertions.assertEquals("", result.getSuggestedAddress().getStreet2());
        Assertions.assertNull(result.getInvalidComponents());
        Assertions.assertFalse(result.getHasInferredComponents());
    }

    @Test
    public void testRequiredInference() throws SmartyException, IOException, InterruptedException {
        // input: 14 Frenchman Dr SA 5212 AUS (no city)
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
                                "locality": "Added",
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
        AddressValidationResultDto result = client.validate(MailingAddress.builder().country("MX").build());

        Assertions.assertTrue(result.isValid());
        Assertions.assertEquals("14 Frenchman Dr", result.getSuggestedAddress().getStreet1());
        Assertions.assertNull(result.getInvalidComponents());
        Assertions.assertTrue(result.getHasInferredComponents());
    }

    @Test
    public void testInvalid() throws SmartyException, IOException, InterruptedException {
        // input: 14 Frenchman Dr AUS (no city/state/post)
        mockResponse(
                """
                          {
                            "address1": "14 Frenchman Dr",
                            "components": {
                              "country_iso_3": "AUS",
                              "premise": "14",
                              "premise_number": "14",
                              "thoroughfare": "Frenchman Dr",
                              "thoroughfare_name": "Frenchman",
                              "thoroughfare_trailing_type": "Dr"
                            },
                            "metadata": {
                              "geocode_precision": "None",
                              "max_geocode_precision": "Premise",
                              "address_format": "premise thoroughfare"
                            },
                            "analysis": {
                              "verification_status": "None",
                              "address_precision": "None",
                              "max_address_precision": "DeliveryPoint",
                              "changes": {
                                "components": {
                                  "country_iso_3": "Added",
                                  "premise": "Identified-NoChange",
                                  "premise_number": "Identified-NoChange",
                                  "thoroughfare": "Identified-NoChange",
                                  "thoroughfare_name": "Identified-ContextChange",
                                  "thoroughfare_trailing_type": "Identified-NoChange"
                                }
                              }
                            }
                          }
                        """
        );
        AddressValidationResultDto result = client.validate(MailingAddress.builder().country("MX").build());

        Assertions.assertFalse(result.isValid());
        Assertions.assertNull(result.getSuggestedAddress());
        Assertions.assertTrue(result.getInvalidComponents().isEmpty());
    }

    @Test
    public void tryGbPoBox() throws SmartyException, IOException, InterruptedException {
        mockResponse(
                """
                         {
                          "address1": "P O BOX 789",
                          "address2": "London",
                          "components": {
                            "administrative_area": "London",
                            "country_iso_3": "GBR",
                            "locality": "London",
                            "postal_code": "W5 5YZ",
                            "post_box": "P O BOX 789",
                            "post_box_number": "789",
                            "post_box_type": "P O BOX 789"
                          },
                          "metadata": {
                            "address_format": "post_box|locality|"
                          },
                          "analysis": {
                            "verification_status": "Ambiguous",
                            "address_precision": "Locality",
                            "max_address_precision": "DeliveryPoint",
                            "changes": {
                              "components": {
                                "administrative_area": "Added",
                                "country_iso_3": "Added",
                                "locality": "Verified-NoChange",
                                "postal_code": "Identified-ContextChange",
                                "post_box": "Identified-AliasChange",
                                "post_box_number": "Identified-NoChange",
                                "post_box_type": "Identified-AliasChange"
                              }
                            }
                          }
                        }
                         """
        );
        AddressValidationResultDto result = client.validate(MailingAddress.builder().country("GB").build());

        Assertions.assertTrue(result.isValid());
        Assertions.assertEquals("P O BOX 789", result.getSuggestedAddress().getStreet1());
        Assertions.assertEquals("", result.getSuggestedAddress().getStreet2());
    }

    @Test
    public void tryGbWithFlat() throws SmartyException, IOException, InterruptedException {
        mockResponse(
                """
                        {
                           "address1": "Flat 9 Wheatstone House",
                           "address2": "650-654 Chiswick High Road",
                           "address3": "London",
                           "address4": "W4 5BB",
                           "components": {
                             "administrative_area": "London",
                             "country_iso_3": "GBR",
                             "locality": "London",
                             "postal_code": "W4 5BB",
                             "postal_code_short": "W4 5BB",
                             "premise": "650-654",
                             "premise_number": "650-654",
                             "thoroughfare": "Chiswick High Road",
                             "building": "Wheatstone House",
                             "sub_building": "Flat 9"
                           },
                           "metadata": {
                             "address_format": "sub_building building|premise thoroughfare|locality|postal_code"
                           },
                           "analysis": {
                             "verification_status": "Verified",
                             "address_precision": "DeliveryPoint",
                             "max_address_precision": "DeliveryPoint",
                             "changes": {
                               "components": {
                                 "administrative_area": "Added",
                                 "country_iso_3": "Added",
                                 "locality": "Verified-NoChange",
                                 "postal_code": "Verified-NoChange",
                                 "postal_code_short": "Verified-NoChange",
                                 "premise": "Verified-NoChange",
                                 "premise_number": "Verified-NoChange",
                                 "thoroughfare": "Verified-NoChange",
                                 "building": "Verified-NoChange",
                                 "sub_building": "Verified-NoChange"
                               }
                             }
                           }
                         }
                         """
        );
        AddressValidationResultDto result = client.validate(MailingAddress.builder().country("GB").build());

        Assertions.assertTrue(result.isValid());
        Assertions.assertEquals("Flat 9 Wheatstone House", result.getSuggestedAddress().getStreet1());
        Assertions.assertEquals("650-654 Chiswick High Road", result.getSuggestedAddress().getStreet2());
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

    private static final String MORE_RESPONSES = """
                  {
                    "address1": "Şehreküstü Mah.",
                    "address2": "Cimek Sokak 3/1",
                    "address3": "37300 Tosya/Kastamonu",
                    "components": {
                      "super_administrative_area": "Karadeniz",
                      "administrative_area": "Kastamonu",
                      "sub_administrative_area": "Tosya",
                      "country_iso_3": "TUR",
                      "locality": "Tosya",
                      "dependent_locality": "Şehreküstü Mah.",
                      "postal_code": "37300",
                      "postal_code_short": "37300",
                      "premise": "3/1",
                      "premise_number": "3/1",
                      "thoroughfare": "Cimek Sokak"
                    },
                    "metadata": {
                      "address_format": "dependent_locality|thoroughfare premise|postal_code locality/administrative_area"
                    },
                    "analysis": {
                      "verification_status": "Verified",
                      "address_precision": "DeliveryPoint",
                      "max_address_precision": "DeliveryPoint",
                      "changes": {
                        "components": {
                          "super_administrative_area": "Added",
                          "administrative_area": "Verified-NoChange",
                          "sub_administrative_area": "Added",
                          "country_iso_3": "Added",
                          "locality": "Verified-NoChange",
                          "dependent_locality": "Verified-NoChange",
                          "postal_code": "Verified-NoChange",
                          "postal_code_short": "Verified-NoChange",
                          "premise": "Verified-NoChange",
                          "premise_number": "Verified-NoChange",
                          "thoroughfare": "Verified-NoChange"
                        }
                      }
                    }
                  }
            """;
}
