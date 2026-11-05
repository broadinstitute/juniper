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
            .sidrProject("PROJ").build();
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
                        config, kit("SALIVA", false), enrollee, new AdminUser()));
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

    @Test
    void returnOnlyOrdersAreNotYetSupported() {
        SIDRService service = new SIDRService(mock(SIDRClientWrapper.class));
        assertThrows(UnsupportedOperationException.class, () ->
                service.placeReturnOnlyKitRequestOrder(config, kit("SALIVA", false), enrollee, new AdminUser()));
    }
}
