package bio.terra.pearl.core.service.address;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.model.address.AddressComponent;
import bio.terra.pearl.core.model.address.AddressValidationResultDto;
import bio.terra.pearl.core.model.address.MailingAddress;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

class AddressValidationServiceStubTest extends BaseSpringBootTest {
    @Autowired
    private AddressValidationServiceStub clientStub;

    @Test
    public void testValidResponse() {
        AddressValidationResultDto result = clientStub.validate(
                MailingAddress
                        .builder().street1("123 Anything St").state("CO").country("USA").postalCode("12345").build());

        Assertions.assertTrue(result.isValid());
        Assertions.assertNull(result.getSuggestedAddress());
        Assertions.assertNull(result.getInvalidComponents());
    }

    @Test
    public void testBasicInvalidAddressResponse() {
        AddressValidationResultDto result = clientStub.validate(
                MailingAddress
                        .builder().street1("123 BAD St").state("CO").country("USA").postalCode("12345").build());

        Assertions.assertFalse(result.isValid());
        Assertions.assertNull(result.getSuggestedAddress());
        Assertions.assertNotNull(result.getInvalidComponents());

        Assertions.assertEquals(List.of(), result.getInvalidComponents());
    }

    @Test
    public void testInvalidAddressComponents() {
        AddressValidationResultDto result = clientStub.validate(
                MailingAddress
                        .builder().street1("123 BAD INVALID_STREET_NAME INVALID_CITY St").state("CO").country("USA").postalCode("12345").build());

        Assertions.assertFalse(result.isValid());
        Assertions.assertNull(result.getSuggestedAddress());
        Assertions.assertNotNull(result.getInvalidComponents());

        Assertions.assertEquals(List.of(AddressComponent.STREET_NAME, AddressComponent.CITY), result.getInvalidComponents());
    }

    @Test
    public void testImprovableAddressResponse() {
        AddressValidationResultDto result = clientStub.validate(
                MailingAddress
                        .builder().street1("123 IMPROVABLE St").state("CO").country("USA").postalCode("12345").build());

        Assertions.assertTrue(result.isValid());
        Assertions.assertNotNull(result.getSuggestedAddress());
        Assertions.assertNull(result.getInvalidComponents());

        Assertions.assertEquals(result.getSuggestedAddress().getStreet1(), "415 Main St");
        Assertions.assertEquals(result.getSuggestedAddress().getCity(), "Cambridge");
        Assertions.assertEquals(result.getSuggestedAddress().getPostalCode(), "02142-1027");
        Assertions.assertEquals(result.getSuggestedAddress().getCountry(), "US");
    }

    @Test
    public void testInferredAddressResponse() {
        AddressValidationResultDto result = clientStub.validate(
                MailingAddress
                        .builder().street1("123 INFERENCE St").state("CO").country("USA").postalCode("12345").build());

        Assertions.assertTrue(result.isValid());
        Assertions.assertNotNull(result.getSuggestedAddress());
        Assertions.assertNull(result.getInvalidComponents());

        Assertions.assertEquals(result.getSuggestedAddress().getStreet1(), "415 Main St");
        Assertions.assertEquals(result.getSuggestedAddress().getCity(), "Cambridge");
        Assertions.assertEquals(result.getSuggestedAddress().getPostalCode(), "02142-1027");
        Assertions.assertEquals(result.getSuggestedAddress().getCountry(), "US");

        Assertions.assertTrue(result.getHasInferredComponents());
    }

    @Test
    public void testVacant() {
        AddressValidationResultDto result = clientStub.validate(
                MailingAddress
                        .builder().street1("123 VACANT St").state("CO").country("USA").postalCode("12345").build());

        Assertions.assertTrue(result.isValid());
        Assertions.assertTrue(result.getVacant());
    }

    @Test
    public void testErrorValidationResponseNoPostalCode() {
        AddressValidationException thrown = Assertions.assertThrows(
                AddressValidationException.class,
                () -> clientStub.validate(MailingAddress
                        .builder().street1("123 ERROR St").state("CO").country("USA").build()));

        Assertions.assertEquals(500, thrown.getHttpStatusCode().value());
    }

    @Test
    public void testErrorValidationResponsePostalCodeInvalidHttpStatus() {
        AddressValidationException thrown = Assertions.assertThrows(
                AddressValidationException.class,
                () -> clientStub.validate(MailingAddress
                        .builder().street1("123 ERROR St").state("CO").country("USA").postalCode("02119").build()));

        Assertions.assertEquals(500, thrown.getHttpStatusCode().value());
    }

    @Test
    public void testErrorValidationResponsePostalCodeValidHttpStatus() {
        AddressValidationException thrown = Assertions.assertThrows(
                AddressValidationException.class,
                () -> clientStub.validate(MailingAddress
                        .builder().street1("123 ERROR St").state("CO").country("USA").postalCode("404").build()));

        Assertions.assertEquals(404, thrown.getHttpStatusCode().value());
    }

}