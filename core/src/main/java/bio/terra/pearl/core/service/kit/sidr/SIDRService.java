package bio.terra.pearl.core.service.kit.sidr;

import bio.terra.pearl.core.model.address.MailingAddress;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.kit.KitType;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import bio.terra.pearl.core.service.kit.sidr.models.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Translates Juniper KitRequests into SIDR orders, and places them using the SIDRClient appropriate for the
 * study environment (stub or live).
 */
@Service
public class SIDRService {
    /** the SIDR test code for ordering a research kit that is received and stored on return */
    public static final String KIT_REC_AND_STORE_TEST_CODE = "R_DTP_KIT_REC_AND_STORE";
    /**
     * the SIDR test code for a kit that was not mailed by SIDR: the barcode is already on the kit, and it is
     * only being received and stored on return
     */
    public static final String RETURN_ONLY_TEST_CODE = "R_KIT_REC_AND_STORE";

    private final SIDRClientWrapper sidrClientWrapper;

    public SIDRService(SIDRClientWrapper sidrClientWrapper) {
        this.sidrClientWrapper = sidrClientWrapper;
    }

    /**
     * Places a mailed-kit order in SIDR on behalf of the operator.
     *
     * @param operator            the study staff member requesting the kit; SIDR needs to know who is placing the order
     * @param attestationAccepted whether the operator has agreed to the SIDR research kit attestations. SIDR won't
     *                            accept an order without it
     * @return the order as created by SIDR
     * @throws IllegalArgumentException if the kit can't be ordered, e.g. required participant info is missing
     * @throws SIDRApiException if SIDR rejects the order
     */
    public SIDROrder placeKitRequestOrder(StudyEnvironmentConfig config, KitRequest kitRequest, Enrollee enrollee,
                                          Profile profile, AdminUser operator, boolean attestationAccepted)
            throws SIDRApiException {
        if (!attestationAccepted) {
            throw new IllegalArgumentException("Order acknowledgements must be accepted to request a kit");
        }
        SIDROrder order = buildKitRequestOrder(config, kitRequest, enrollee, profile);
        return sidrClientWrapper.postOrder(config, operator, order);
    }

    /**
     * Places an order in SIDR for a kit that already has a barcode: an in-person kit that staff have collected,
     * or a kit that staff assigned by hand. SIDR only needs to know the barcode and kit type, the participant's
     * identifier and sex at birth, and the tracking numbers if we have them, so no name, date of birth or address
     * is sent.
     *
     * @param profile             the participant's profile, for sex at birth
     * @param attestationAccepted whether the operator has agreed to the research kit attestation
     * @return the order as created by SIDR
     * @throws IllegalArgumentException if the kit can't be ordered, e.g. it has no barcode
     * @throws SIDRApiException if SIDR rejects the order
     */
    public SIDROrder placeReturnOnlyKitRequestOrder(StudyEnvironmentConfig config, KitRequest kitRequest,
                                                    Enrollee enrollee, Profile profile, AdminUser operator,
                                                    boolean attestationAccepted) throws SIDRApiException {
        if (!attestationAccepted) {
            throw new IllegalArgumentException("Order acknowledgements must be accepted to place a kit order");
        }
        SIDROrder order = buildReturnOnlyKitRequestOrder(config, kitRequest, enrollee, profile);
        return sidrClientWrapper.postOrder(config, operator, order);
    }

    /** builds the SIDR order for a kit that already has a barcode. Does not communicate with SIDR. */
    static SIDROrder buildReturnOnlyKitRequestOrder(StudyEnvironmentConfig config, KitRequest kitRequest,
                                                    Enrollee enrollee, Profile profile) {
        requireProject(config);
        if (StringUtils.isBlank(kitRequest.getKitLabel())) {
            throw new IllegalArgumentException("A kit label is required to place a kit order");
        }
        SIDRSample sample = SIDRSample.builder()
                // SIDR uses the barcode as the sample id when it isn't given one
                .clientBarcode(kitRequest.getKitLabel())
                .shippingInformation(SIDRShippingInformation.builder()
                        .kitType(toSidrKitType(kitRequest.getKitType()))
                        // the tracking numbers are optional for SIDR, and we may not have either one
                        .trackingIdToPatient(StringUtils.trimToNull(kitRequest.getTrackingNumber()))
                        .trackingIdToBroad(StringUtils.trimToNull(kitRequest.getReturnTrackingNumber()))
                        .build())
                // the material type is derived from the kit type
                .patient(SIDRPatient.builder()
                        .patientId(getEnrolleeId(config, enrollee))
                        .metadata(List.of(SIDRMetadata.of("sex_at_birth", normalizeSexAtBirth(profile.getSexAtBirth()))))
                        .build())
                .build();

        return SIDROrder.builder()
                .projectKey(config.getSidrProject())
                .numberOfSamples(1)
                .clinicalOrderAttestation(true)
                .externalOrderId(kitRequest.getId().toString())
                // SIDR attaches every sample to every test when no sample ids are given
                .tests(List.of(SIDRTest.builder().testCode(RETURN_ONLY_TEST_CODE).build()))
                .samples(List.of(sample))
                .build();
    }

    private static void requireProject(StudyEnvironmentConfig config) {
        if (StringUtils.isBlank(config.getSidrProject())) {
            throw new IllegalArgumentException("Kit ordering is not configured for this study environment");
        }
    }

    private static String getEnrolleeId(StudyEnvironmentConfig config, Enrollee enrollee) {
        return config.isKitUseResearchIds() ? enrollee.getResearchId() : enrollee.getShortcode();
    }

    /** builds the SIDR order for a mailed kit. Does not communicate with SIDR. */
    static SIDROrder buildKitRequestOrder(StudyEnvironmentConfig config, KitRequest kitRequest, Enrollee enrollee,
                                          Profile profile) {
        requireProject(config);
        String enrolleeId = getEnrolleeId(config, enrollee);
        MailingAddress address = profile.getMailingAddress();
        if (address == null || StringUtils.isAnyBlank(address.getStreet1(), address.getCity(),
                address.getPostalCode(), address.getCountry())) {
            throw new IllegalArgumentException("Participant has an incomplete mailing address");
        }
        if (StringUtils.isAnyBlank(profile.getGivenName(), profile.getFamilyName())) {
            throw new IllegalArgumentException("Participant name is required to request a kit");
        }

        SIDRShippingInformation shippingInfo = SIDRShippingInformation.builder()
                .kitType(toSidrKitType(kitRequest.getKitType()))
                .patientAddress(SIDRKitAddress.builder()
                        .line1(address.getStreet1())
                        .line2(StringUtils.trimToNull(address.getStreet2()))
                        .city(address.getCity())
                        .region(StringUtils.trimToNull(address.getState()))
                        .zip(address.getPostalCode())
                        .country(address.getCountry())
                        // SIDR validates strictly unless told otherwise; mirror the "override address validation" option
                        .verifyStrict(kitRequest.isSkipAddressValidation() ? false : null)
                        .build())
                .build();

        List<SIDRMetadata> patientMetadata = new ArrayList<>(List.of(
                SIDRMetadata.of("first_name", profile.getGivenName()),
                SIDRMetadata.of("last_name", profile.getFamilyName()),
                SIDRMetadata.of("sex_at_birth", normalizeSexAtBirth(profile.getSexAtBirth()))));
        if (profile.getBirthDate() != null) {
            patientMetadata.add(SIDRMetadata.of("date_of_birth", normalizeDateOfBirth(profile.getBirthDate())));
        }

        SIDRSample sample = SIDRSample.builder()
                .shippingInformation(shippingInfo)
                .materialType(toSidrMaterialType(kitRequest.getKitType()))
                .patient(SIDRPatient.builder().patientId(enrolleeId).metadata(patientMetadata).build())
                .build();

        return SIDROrder.builder()
                .projectKey(config.getSidrProject())
                .numberOfSamples(1)
                .sampleKitRequested(true)
                .clinicalOrderAttestation(true)
                // our kit id, so that SIDR events can be matched back to the kit
                .externalOrderId(kitRequest.getId().toString())
                // for a DTP order, SIDR attaches every sample to every test, so no sample ids are needed here
                .tests(List.of(SIDRTest.builder().testCode(KIT_REC_AND_STORE_TEST_CODE).build()))
                .samples(List.of(sample))
                .build();
    }

    private static String toSidrKitType(KitType kitType) {
        return switch (kitType.getName()) {
            case "SALIVA" -> "SALIVA";
            case "BLOOD" -> "CAPBLOOD";
            default -> throw new IllegalArgumentException("Kit type cannot be ordered: " + kitType.getName());
        };
    }

    private static String toSidrMaterialType(KitType kitType) {
        return switch (kitType.getName()) {
            case "SALIVA" -> "BODILY_FLUID_SALIVA";
            case "BLOOD" -> "WHOLE_BLOOD_CAPILLARY_BLOOD";
            default -> throw new IllegalArgumentException("Kit type cannot be ordered: " + kitType.getName());
        };
    }

    private static String normalizeSexAtBirth(String sexAtBirth) {
        if (StringUtils.isBlank(sexAtBirth)) {
            throw new IllegalArgumentException("Sex at birth is required to request a kit");
        }
        if (sexAtBirth.equalsIgnoreCase("male") || sexAtBirth.equalsIgnoreCase("m")) {
            return "Male";
        } else if (sexAtBirth.equalsIgnoreCase("female") || sexAtBirth.equalsIgnoreCase("f")) {
            return "Female";
        }
        throw new IllegalArgumentException("Sex at birth must be male or female to request a kit: " + sexAtBirth);
    }

    private static String normalizeDateOfBirth(LocalDate dateOfBirth) {
        return dateOfBirth.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
