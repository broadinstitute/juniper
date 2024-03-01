package bio.terra.pearl.core.service.address;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.model.address.AddressValidationResultDto;
import bio.terra.pearl.core.model.address.MailingAddress;
import com.smartystreets.api.SmartySerializer;
import com.smartystreets.api.exceptions.SmartyException;
import com.smartystreets.api.us_street.Candidate;
import com.smartystreets.api.us_street.Lookup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.IOException;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;

class SmartyUSAddressValidationServiceTest extends BaseSpringBootTest {

    @MockBean
    private SmartyClient mockSmartyClient;

    // all the candidates below come from real responses via
    // https://www.smarty.com/products/us-address-verification#demo

    @Autowired
    private SmartyUSAddressValidationService client;

    @Test
    public void testValidResponse() throws SmartyException, IOException, InterruptedException {
        // input: 415 Main St Cambridge, MA 02142-1027
        mockResponse(
        """
                {
                    "input_index": 0,
                    "candidate_index": 0,
                    "delivery_line_1": "415 Main St",
                    "last_line": "Cambridge MA 02142-1027",
                    "delivery_point_barcode": "021421027155",
                    "smarty_key": "1034222138",
                    "components": {
                        "primary_number": "415",
                        "street_name": "Main",
                        "street_suffix": "St",
                        "city_name": "Cambridge",
                        "default_city_name": "Cambridge",
                        "state_abbreviation": "MA",
                        "zipcode": "02142",
                        "plus4_code": "1027",
                        "delivery_point": "15",
                        "delivery_point_check_digit": "5"
                    },
                    "metadata": {
                        "record_type": "S",
                        "zip_type": "Standard",
                        "county_fips": "25017",
                        "county_name": "Middlesex",
                        "carrier_route": "C037",
                        "congressional_district": "07",
                        "rdi": "Commercial",
                        "elot_sequence": "0166",
                        "elot_sort": "A",
                        "latitude": 42.36298,
                        "longitude": -71.088737,
                        "coordinate_license": 1,
                        "precision": "Rooftop",
                        "time_zone": "Eastern",
                        "utc_offset": -5,
                        "dst": true
                    },
                    "analysis": {
                        "dpv_match_code": "Y",
                        "dpv_footnotes": "AABB",
                        "dpv_cmra": "N",
                        "dpv_vacant": "N",
                        "dpv_no_stat": "N",
                        "active": "Y",
                        "enhanced_match": "postal-match,missing-secondary"
                    }
                }
                """
        );
        AddressValidationResultDto result = client.validate(new MailingAddress());

        //Assertions.assertTrue(result.isValid());
        Assertions.assertEquals("415 Main St", result.getSuggestedAddress().getStreet1());
        Assertions.assertNull(result.getInvalidComponents());
        Assertions.assertFalse(result.getHasInferredComponents());
    }

    @Test
    public void testInvalidResponse() throws SmartyException, IOException, InterruptedException {
        // input: 123456 Asdf Rd Boston NY 12345
        mockResponse(
                """
                        {
                            "input_index": 0,
                            "candidate_index": 0,
                            "delivery_line_1": "123456 Asdf Rd",
                            "last_line": "Boston NY 14025",
                            "delivery_point_barcode": "14025",
                            "components": {
                                "primary_number": "123456",
                                "street_name": "Asdf",
                                "street_suffix": "Rd",
                                "city_name": "Boston",
                                "default_city_name": "Boston",
                                "state_abbreviation": "NY",
                                "zipcode": "14025"
                            },
                            "metadata": {
                                "zip_type": "Standard",
                                "county_fips": "36029",
                                "county_name": "Erie",
                                "latitude": 42.63101,
                                "longitude": -78.73634,
                                "precision": "Zip5",
                                "time_zone": "Eastern",
                                "utc_offset": -5,
                                "dst": true
                            },
                            "analysis": {
                                "dpv_footnotes": "A1",
                                "active": "Y",
                                "footnotes": "A#F#",
                                "enhanced_match": "none"
                            }
                        }
                        """
        );
        AddressValidationResultDto result = client.validate(new MailingAddress());

        Assertions.assertFalse(result.isValid());
        Assertions.assertNotNull(result.getInvalidComponents());
    }

    @Test
    public void testInferredResponse() throws SmartyException, IOException, InterruptedException {
        // input (misspelled): 415 Mian St Cambridge, MA 02142-1027
        mockResponse(
                """
                        {
                            "input_index": 0,
                            "candidate_index": 0,
                            "delivery_line_1": "415 Main St",
                            "last_line": "Cambridge MA 02142-1027",
                            "delivery_point_barcode": "021421027155",
                            "smarty_key": "1034222138",
                            "components": {
                                "primary_number": "415",
                                "street_name": "Main",
                                "street_suffix": "St",
                                "city_name": "Cambridge",
                                "default_city_name": "Cambridge",
                                "state_abbreviation": "MA",
                                "zipcode": "02142",
                                "plus4_code": "1027",
                                "delivery_point": "15",
                                "delivery_point_check_digit": "5"
                            },
                            "metadata": {
                                "record_type": "S",
                                "zip_type": "Standard",
                                "county_fips": "25017",
                                "county_name": "Middlesex",
                                "carrier_route": "C037",
                                "congressional_district": "07",
                                "rdi": "Commercial",
                                "elot_sequence": "0166",
                                "elot_sort": "A",
                                "latitude": 42.36298,
                                "longitude": -71.088737,
                                "coordinate_license": 1,
                                "precision": "Rooftop",
                                "time_zone": "Eastern",
                                "utc_offset": -5,
                                "dst": true
                            },
                            "analysis": {
                                "dpv_match_code": "Y",
                                "dpv_footnotes": "AABB",
                                "dpv_cmra": "N",
                                "dpv_vacant": "N",
                                "dpv_no_stat": "N",
                                "active": "Y",
                                "footnotes": "M#",
                                "enhanced_match": "postal-match,missing-secondary"
                            }
                        }
                        """
        );
        AddressValidationResultDto result = client.validate(new MailingAddress());

        Assertions.assertTrue(result.isValid());
        Assertions.assertEquals("415 Main St", result.getSuggestedAddress().getStreet1());
        Assertions.assertNull(result.getInvalidComponents());
        Assertions.assertTrue(result.getHasInferredComponents());
    }

    @Test
    public void testBetterAddress() throws SmartyException, IOException, InterruptedException {
        // input (LaFayette not official spelling): 150 Ken Visage Ln LaFayette GA
        mockResponse(
                """
                        {
                            "input_index": 0,
                            "candidate_index": 0,
                            "delivery_line_1": "150 Ken Visage Ln",
                            "last_line": "La Fayette GA 30728-4981",
                            "delivery_point_barcode": "307284981503",
                            "smarty_key": "2037752728",
                            "components": {
                                "primary_number": "150",
                                "street_name": "Ken Visage",
                                "street_suffix": "Ln",
                                "city_name": "La Fayette",
                                "default_city_name": "La Fayette",
                                "state_abbreviation": "GA",
                                "zipcode": "30728",
                                "plus4_code": "4981",
                                "delivery_point": "50",
                                "delivery_point_check_digit": "3"
                            },
                            "metadata": {
                                "record_type": "S",
                                "zip_type": "Standard",
                                "county_fips": "13295",
                                "county_name": "Walker",
                                "carrier_route": "R006",
                                "congressional_district": "14",
                                "rdi": "Residential",
                                "elot_sequence": "0399",
                                "elot_sort": "A",
                                "latitude": 34.714897,
                                "longitude": -85.218483,
                                "coordinate_license": 1,
                                "precision": "Rooftop",
                                "time_zone": "Eastern",
                                "utc_offset": -5,
                                "dst": true
                            },
                            "analysis": {
                                "dpv_match_code": "Y",
                                "dpv_footnotes": "AABB",
                                "dpv_cmra": "N",
                                "dpv_vacant": "N",
                                "dpv_no_stat": "N",
                                "active": "Y",
                                "footnotes": "U#",
                                "enhanced_match": "postal-match"
                            }
                        }
                        """
        );
        AddressValidationResultDto result = client.validate(new MailingAddress());

        Assertions.assertTrue(result.isValid());
        Assertions.assertEquals("La Fayette", result.getSuggestedAddress().getCity());
        Assertions.assertNull(result.getInvalidComponents());
        Assertions.assertFalse(result.getHasInferredComponents());
    }

    void mockResponse(String jsonResponse) throws SmartyException, IOException, InterruptedException {

        SmartySerializer serializer = new SmartySerializer();
        Candidate candidate = serializer.deserialize(jsonResponse.getBytes(), Candidate.class);

        Answer<Void> answer = invocation -> {

            Lookup arg = invocation.getArgument(0);
            arg.setResult(List.of(candidate));
            return null;
        };

        Mockito.doAnswer(answer).when(mockSmartyClient).send(any(Lookup.class));
    }
}