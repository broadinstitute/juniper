package bio.terra.pearl.core.service.kit.sidr;

import bio.terra.pearl.core.model.address.MailingAddress;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.kit.KitType;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import bio.terra.pearl.core.service.kit.sidr.models.SIDROrder;
import bio.terra.pearl.core.service.kit.sidr.models.SIDRSample;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SIDRServiceTest {
    private final StudyEnvironmentConfig config = StudyEnvironmentConfig.builder()
            .sidrProjectKey("PROJ").build();
    private final Enrollee enrollee = Enrollee.builder().shortcode("HDSALK").researchId("RESEARCH1").build();

    private KitRequest kit(String kitTypeName, boolean skipValidation) {
        return KitRequest.builder().id(UUID.randomUUID())
                .kitType(KitType.builder().name(kitTypeName).build())
                .skipAddressValidation(skipValidation).build();
    }

    private Profile profile() {
        return Profile.builder().givenName("Jonas").familyName("Salk").sexAtBirth("male")
                .birthDate(LocalDate.of(1914, 10, 28))
                .mailingAddress(MailingAddress.builder().street1("1 Main").street2("")
                        .city("Cambridge").state("MA").postalCode("02142").country("US").build())
                .build();
    }

    @Test
    void buildsDtpKitOrder() {
        KitRequest kit = kit("SALIVA", false);
        SIDROrder order = SIDRService.buildKitRequestOrder(config, kit, enrollee, profile());
        assertEquals("PROJ", order.getProjectKey());
        assertEquals(kit.getId().toString(), order.getExternalOrderId());
        assertTrue(order.getSampleKitRequested());
        assertTrue(order.getClinicalOrderAttestation());
        assertEquals("R_DTP_KIT_REC_AND_STORE", order.getTests().get(0).getTestCode());
        SIDRSample sample = order.getSamples().get(0);
        assertEquals("BODILY_FLUID_SALIVA", sample.getMaterialType());
        assertEquals("SALIVA", sample.getShippingInformation().getKitType());
        var address = sample.getShippingInformation().getPatientAddress();
        assertEquals("MA", address.getRegion());
        assertNull(address.getLine2(), "blank line 2 should be omitted");
        assertNull(address.getVerifyStrict());
        assertEquals("HDSALK", sample.getPatient().getPatientId());
        var metadata = sample.getPatient().getMetadata();
        assertTrue(metadata.stream().anyMatch(m -> m.getKey().equals("sex_at_birth") && m.getValue().contains("Male")));
        assertTrue(metadata.stream().anyMatch(m -> m.getKey().equals("date_of_birth") && m.getValue().contains("1914-10-28")));
    }

    @Test
    void bloodMapsToCapillaryBloodAndResearchIdsAreUsed() {
        config.setKitUseResearchIds(true);
        SIDROrder order = SIDRService.buildKitRequestOrder(config, kit("BLOOD", true), enrollee, profile());
        SIDRSample sample = order.getSamples().get(0);
        assertEquals("CAPBLOOD", sample.getShippingInformation().getKitType());
        assertEquals("WHOLE_BLOOD_CAPILLARY_BLOOD", sample.getMaterialType());
        assertEquals("RESEARCH1", sample.getPatient().getPatientId());
        assertEquals(false, sample.getShippingInformation().getPatientAddress().getVerifyStrict());
    }

    @Test
    void omitsMissingBirthDate() {
        Profile profile = profile();
        profile.setBirthDate(null);
        SIDROrder order = SIDRService.buildKitRequestOrder(config, kit("SALIVA", false), enrollee, profile);
        assertTrue(order.getSamples().get(0).getPatient().getMetadata().stream()
                .noneMatch(m -> m.getKey().equals("date_of_birth")));
    }

    /** these messages are shown to study staff, who don't need to know which system we order kits from */
    @Test
    void validationMessagesDoNotMentionSidr() {
        Profile noSex = profile();
        noSex.setSexAtBirth(null);
        Profile oddSex = profile();
        oddSex.setSexAtBirth("other");
        Profile noName = profile();
        noName.setGivenName(null);
        Profile noCity = profile();
        noCity.getMailingAddress().setCity(null);
        List<Executable> invalidOrders = List.of(
                () -> SIDRService.buildKitRequestOrder(config, kit("STOOL", false), enrollee, profile()),
                () -> SIDRService.buildKitRequestOrder(config, kit("SALIVA", false), enrollee, noSex),
                () -> SIDRService.buildKitRequestOrder(config, kit("SALIVA", false), enrollee, oddSex),
                () -> SIDRService.buildKitRequestOrder(config, kit("SALIVA", false), enrollee, noName),
                () -> SIDRService.buildKitRequestOrder(config, kit("SALIVA", false), enrollee, noCity),
                () -> SIDRService.buildKitRequestOrder(StudyEnvironmentConfig.builder().build(), kit("SALIVA", false), enrollee, profile()),
                () -> new SIDRService(mock(SIDRClientWrapper.class)).placeKitRequestOrder(
                        config, kit("SALIVA", false), enrollee, profile(), new AdminUser(), false),
                () -> new SIDRService(mock(SIDRClientWrapper.class)).placeReturnOnlyKitRequestOrder(
                        config, returnOnlyKit("SALIVA", "BARCODE-1"), enrollee, profile(), new AdminUser(), false),
                () -> SIDRService.buildReturnOnlyKitRequestOrder(config, returnOnlyKit("STOOL", "BARCODE-1"), enrollee, profile()),
                () -> SIDRService.buildReturnOnlyKitRequestOrder(config, returnOnlyKit("SALIVA", null), enrollee, profile()),
                () -> SIDRService.buildReturnOnlyKitRequestOrder(config, returnOnlyKit("SALIVA", "BARCODE-1"), enrollee, noSex));
        for (Executable invalidOrder : invalidOrders) {
            RuntimeException e = assertThrows(RuntimeException.class, invalidOrder);
            assertFalse(e.getMessage().toLowerCase().contains("sidr"), e.getMessage());
            assertFalse(e.getMessage().contains("GPO"), e.getMessage());
        }
    }

    @Test
    void rejectsOrdersSidrCouldNotAccept() {
        assertThrows(IllegalArgumentException.class, () ->
                SIDRService.buildKitRequestOrder(config, kit("STOOL", false), enrollee, profile()));
        Profile noSex = profile();
        noSex.setSexAtBirth(null);
        assertThrows(IllegalArgumentException.class, () ->
                SIDRService.buildKitRequestOrder(config, kit("SALIVA", false), enrollee, noSex));
        assertThrows(IllegalArgumentException.class, () ->
                SIDRService.buildKitRequestOrder(StudyEnvironmentConfig.builder().build(), kit("SALIVA", false), enrollee, profile()));
        Profile noAddress = profile();
        noAddress.getMailingAddress().setCity(null);
        assertThrows(IllegalArgumentException.class, () ->
                SIDRService.buildKitRequestOrder(config, kit("SALIVA", false), enrollee, noAddress));
    }

    @Test
    void requiresAttestationAndPlacesOrderAsOperator() {
        SIDRClientWrapper wrapper = mock(SIDRClientWrapper.class);
        SIDROrder created = SIDROrder.builder().orderId("SDOR-1").build();
        when(wrapper.postOrder(any(), any(), any())).thenReturn(created);
        SIDRService service = new SIDRService(wrapper);
        AdminUser operator = AdminUser.builder().username("staff@example.com").build();

        assertThrows(IllegalArgumentException.class, () ->
                service.placeKitRequestOrder(config, kit("SALIVA", false), enrollee, profile(), operator, false));
        verifyNoInteractions(wrapper);

        SIDROrder result = service.placeKitRequestOrder(config, kit("SALIVA", false), enrollee, profile(), operator, true);
        assertSame(created, result);
        ArgumentCaptor<SIDROrder> captor = ArgumentCaptor.forClass(SIDROrder.class);
        verify(wrapper).postOrder(eq(config), eq(operator), captor.capture());
        assertEquals("PROJ", captor.getValue().getProjectKey());
    }

    private KitRequest returnOnlyKit(String kitTypeName, String barcode) {
        KitRequest kit = kit(kitTypeName, false);
        kit.setKitLabel(barcode);
        return kit;
    }

    @Test
    void buildsReturnOnlyOrderWithMinimalParticipantInfo() throws Exception {
        KitRequest kit = returnOnlyKit("BLOOD", "BARCODE-1");
        kit.setTrackingNumber("OUTBOUND-1");
        kit.setReturnTrackingNumber("RETURN-1");
        SIDROrder order = SIDRService.buildReturnOnlyKitRequestOrder(config, kit, enrollee, profile());

        assertEquals("PROJ", order.getProjectKey());
        assertEquals(kit.getId().toString(), order.getExternalOrderId());
        assertTrue(order.getClinicalOrderAttestation());
        assertNull(order.getSampleKitRequested(), "return-only orders are not DTP orders");
        assertEquals("R_KIT_REC_AND_STORE", order.getTests().get(0).getTestCode());
        SIDRSample sample = order.getSamples().get(0);
        assertEquals("BARCODE-1", sample.getClientBarcode());
        assertNull(sample.getMaterialType(), "SIDR derives the material type from the kit type");
        assertEquals("CAPBLOOD", sample.getShippingInformation().getKitType());
        assertEquals("OUTBOUND-1", sample.getShippingInformation().getTrackingIdToPatient());
        assertEquals("RETURN-1", sample.getShippingInformation().getTrackingIdToBroad());
        assertEquals("HDSALK", sample.getPatient().getPatientId());
        assertEquals(1, sample.getPatient().getMetadata().size());
        assertEquals("sex_at_birth", sample.getPatient().getMetadata().get(0).getKey());
        assertEquals(List.of("Male"), sample.getPatient().getMetadata().get(0).getValue());

        // nothing identifying beyond that goes over the wire
        String json = new ObjectMapper().registerModule(new JavaTimeModule()).writeValueAsString(order);
        for (String phi : List.of("Jonas", "Salk", "1914", "first_name", "last_name", "date_of_birth", "Main", "Cambridge",
                "02142", "patient_address", "line_1")) {
            assertFalse(json.contains(phi), "should not send " + phi);
        }
    }

    @Test
    void returnOnlyOrderOmitsMissingTrackingNumbers() {
        KitRequest kit = returnOnlyKit("SALIVA", "BARCODE-1");
        kit.setTrackingNumber(" ");
        SIDROrder order = SIDRService.buildReturnOnlyKitRequestOrder(config, kit, enrollee, profile());
        var shipping = order.getSamples().get(0).getShippingInformation();
        assertNull(shipping.getTrackingIdToPatient());
        assertNull(shipping.getTrackingIdToBroad());
        assertEquals("SALIVA", shipping.getKitType());
    }

    @Test
    void returnOnlyOrderDoesNotNeedAMailingAddressOrName() {
        Profile bare = profile();
        bare.setGivenName(null);
        bare.setFamilyName(null);
        bare.setBirthDate(null);
        bare.setMailingAddress(null);
        assertNotNull(SIDRService.buildReturnOnlyKitRequestOrder(config, returnOnlyKit("SALIVA", "BARCODE-1"), enrollee, bare));
    }

    @Test
    void placesReturnOnlyOrderAsOperatorWithAttestation() {
        SIDRClientWrapper wrapper = mock(SIDRClientWrapper.class);
        SIDROrder created = SIDROrder.builder().orderId("SDOR-2").build();
        when(wrapper.postOrder(any(), any(), any())).thenReturn(created);
        SIDRService service = new SIDRService(wrapper);
        AdminUser operator = AdminUser.builder().username("staff@example.com").build();
        KitRequest kit = returnOnlyKit("SALIVA", "BARCODE-1");

        assertThrows(IllegalArgumentException.class, () ->
                service.placeReturnOnlyKitRequestOrder(config, kit, enrollee, profile(), operator, false));
        verifyNoInteractions(wrapper);

        assertSame(created, service.placeReturnOnlyKitRequestOrder(config, kit, enrollee, profile(), operator, true));
        ArgumentCaptor<SIDROrder> captor = ArgumentCaptor.forClass(SIDROrder.class);
        verify(wrapper).postOrder(eq(config), eq(operator), captor.capture());
        assertEquals("R_KIT_REC_AND_STORE", captor.getValue().getTests().get(0).getTestCode());
    }
}
