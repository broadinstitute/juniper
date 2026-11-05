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
    /** TBD: the SIDR test code that return-only kits will be ordered under */
    public static final String RETURN_ONLY_TEST_CODE = null;

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
            throw new IllegalArgumentException("The SIDR kit attestations must be accepted to request a kit");
        }
        SIDROrder order = buildKitRequestOrder(config, kitRequest, enrollee, profile);
        return sidrClientWrapper.postOrder(config, operator, order);
    }

    /** TBD: return-only kits (in-person and manually assigned kits) will be ordered under a different test code */
    public SIDROrder placeReturnOnlyKitRequestOrder(StudyEnvironmentConfig config, KitRequest kitRequest,
                                                    Enrollee enrollee, AdminUser operator) {
        throw new UnsupportedOperationException("Return-only kits are not yet supported with SIDR");
    }

    /** builds the SIDR order for a mailed kit. Does not communicate with SIDR. */
    static SIDROrder buildKitRequestOrder(StudyEnvironmentConfig config, KitRequest kitRequest, Enrollee enrollee,
                                          Profile profile) {
        if (StringUtils.isBlank(config.getSidrProject())) {
            throw new IllegalArgumentException("No SIDR project is configured for this study environment");
        }
        String enrolleeId = config.isKitUseResearchIds() ? enrollee.getResearchId() : enrollee.getShortcode();
        MailingAddress address = profile.getMailingAddress();
        if (address == null || StringUtils.isAnyBlank(address.getStreet1(), address.getCity(),
                address.getPostalCode(), address.getCountry())) {
            throw new IllegalArgumentException("Participant has an incomplete mailing address");
        }
        if (StringUtils.isAnyBlank(profile.getGivenName(), profile.getFamilyName())) {
            throw new IllegalArgumentException("Participant name is required to request a SIDR kit");
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
            default -> throw new IllegalArgumentException("Kit type not supported by SIDR: " + kitType.getName());
        };
    }

    private static String toSidrMaterialType(KitType kitType) {
        return switch (kitType.getName()) {
            case "SALIVA" -> "BODILY_FLUID_SALIVA";
            case "BLOOD" -> "WHOLE_BLOOD_CAPILLARY_BLOOD";
            default -> throw new IllegalArgumentException("Kit type not supported by SIDR: " + kitType.getName());
        };
    }

    private static String normalizeSexAtBirth(String sexAtBirth) {
        if (StringUtils.isBlank(sexAtBirth)) {
            throw new IllegalArgumentException("Sex at birth is required to request a SIDR kit");
        }
        if (sexAtBirth.equalsIgnoreCase("male") || sexAtBirth.equalsIgnoreCase("m")) {
            return "Male";
        } else if (sexAtBirth.equalsIgnoreCase("female") || sexAtBirth.equalsIgnoreCase("f")) {
            return "Female";
        }
        throw new IllegalArgumentException("Sex at birth must be male or female to request a SIDR kit: " + sexAtBirth);
    }

    private static String normalizeDateOfBirth(LocalDate dateOfBirth) {
        return dateOfBirth.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
