package bio.terra.pearl.core.service.address;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.model.address.AddressComponent;
import bio.terra.pearl.core.model.address.AddressValidationResultDto;
import bio.terra.pearl.core.model.address.MailingAddress;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

class AddressValidationClientStubTest extends BaseSpringBootTest {
    @Autowired
    private AddressValidationClientStub clientStub;

    @Test
    public void testValidResponse() {
        UUID sessionId = UUID.randomUUID();
        AddressValidationResultDto result = clientStub.validate(
                sessionId,
                MailingAddress
                        .builder().street1("123 Anything St").state("CO").country("USA").postalCode("12345").build());

        Assertions.assertTrue(result.isValid());
        Assertions.assertNull(result.getSuggestedAddress());
        Assertions.assertNull(result.getUnresolvedTokens());
        Assertions.assertNull(result.getMissingComponents());
        Assertions.assertEquals(sessionId, result.getSessionId());
    }

    @Test
    public void testBasicInvalidAddressResponse() {
        UUID sessionId = UUID.randomUUID();
        AddressValidationResultDto result = clientStub.validate(
                sessionId,
                MailingAddress
                        .builder().street1("123 BAD St").state("CO").country("USA").postalCode("12345").build());

        Assertions.assertFalse(result.isValid());
        Assertions.assertNull(result.getSuggestedAddress());
        Assertions.assertNotNull(result.getUnresolvedTokens());
        Assertions.assertNotNull(result.getMissingComponents());
        Assertions.assertEquals(sessionId, result.getSessionId());

        Assertions.assertEquals(List.of("123", "BAD", "St"), result.getUnresolvedTokens());
        Assertions.assertEquals(List.of(AddressComponent.CITY), result.getMissingComponents());
    }

    @Test
    public void testInvalidStreetResponse() {
        UUID sessionId = UUID.randomUUID();
        AddressValidationResultDto result = clientStub.validate(
                sessionId,
                MailingAddress
                        .builder().street1("BAD St").state("CO").country("USA").postalCode("12345").build());

        Assertions.assertFalse(result.isValid());

        Assertions.assertEquals(List.of("BAD", "St"), result.getUnresolvedTokens());
        Assertions.assertEquals(List.of(AddressComponent.HOUSE_NUMBER, AddressComponent.CITY), result.getMissingComponents());

        result = clientStub.validate(
                sessionId,
                MailingAddress
                        .builder().street1("123 BAD").state("CO").country("USA").postalCode("12345").build());

        Assertions.assertFalse(result.isValid());

        Assertions.assertEquals(List.of("123", "BAD"), result.getUnresolvedTokens());
        Assertions.assertEquals(
                List.of(AddressComponent.STREET_TYPE, AddressComponent.CITY),
                result.getMissingComponents());

        result = clientStub.validate(
                sessionId,
                MailingAddress
                        .builder().street1("").state("CO").country("USA").postalCode("12345").build());

        Assertions.assertFalse(result.isValid());

        Assertions.assertEquals(List.of(), result.getUnresolvedTokens());
        Assertions.assertEquals(List.of(
                AddressComponent.STREET_NAME, AddressComponent.STREET_TYPE,
                AddressComponent.HOUSE_NUMBER, AddressComponent.CITY
        ), result.getMissingComponents());

    }

    @Test
    public void testImprovableAddressResponse() {
        UUID sessionId = UUID.randomUUID();
        AddressValidationResultDto result = clientStub.validate(
                sessionId,
                MailingAddress
                        .builder().street1("123 IMPROVABLE St").state("CO").country("USA").postalCode("12345").build());

        Assertions.assertTrue(result.isValid());
        Assertions.assertNotNull(result.getSuggestedAddress());
        Assertions.assertNull(result.getUnresolvedTokens());
        Assertions.assertNull(result.getMissingComponents());
        Assertions.assertEquals(sessionId, result.getSessionId());

        Assertions.assertEquals(result.getSuggestedAddress().getStreet1(), "415 Main St");
        Assertions.assertEquals(result.getSuggestedAddress().getCity(), "Cambridge");
        Assertions.assertEquals(result.getSuggestedAddress().getPostalCode(), "02142");
        Assertions.assertEquals(result.getSuggestedAddress().getCountry(), "USA");
    }

    @Test
    public void testErrorValidationResponseNoPostalCode() {
        UUID sessionId = UUID.randomUUID();

        AddressValidationException thrown = Assertions.assertThrows(
                AddressValidationException.class,
                () -> clientStub.validate(sessionId, MailingAddress
                        .builder().street1("123 ERROR St").state("CO").country("USA").build()));

        Assertions.assertEquals(500, thrown.getHttpStatusCode().value());
    }

    @Test
    public void testErrorValidationResponsePostalCodeInvalidHttpStatus() {
        UUID sessionId = UUID.randomUUID();

        AddressValidationException thrown = Assertions.assertThrows(
                AddressValidationException.class,
                () -> clientStub.validate(sessionId, MailingAddress
                        .builder().street1("123 ERROR St").state("CO").country("USA").postalCode("02119").build()));

        Assertions.assertEquals(500, thrown.getHttpStatusCode().value());
    }

    @Test
    public void testErrorValidationResponsePostalCodeValidHttpStatus() {
        UUID sessionId = UUID.randomUUID();

        AddressValidationException thrown = Assertions.assertThrows(
                AddressValidationException.class,
                () -> clientStub.validate(sessionId, MailingAddress
                        .builder().street1("123 ERROR St").state("CO").country("USA").postalCode("404").build()));

        Assertions.assertEquals(404, thrown.getHttpStatusCode().value());
    }
}