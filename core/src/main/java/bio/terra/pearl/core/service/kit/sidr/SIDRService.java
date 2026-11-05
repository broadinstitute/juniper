package bio.terra.pearl.core.service.kit.sidr;

import bio.terra.pearl.core.model.address.MailingAddress;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.kit.KitType;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import bio.terra.pearl.core.service.kit.sidr.models.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SIDRService {

    public SIDROrder placeKitRequestOrder(StudyEnvironmentConfig config, KitRequest kitRequest, Enrollee enrollee, Profile profile) {
        return null;
    }

    private static SIDROrder buildKitRequestOrder(StudyEnvironmentConfig config, KitRequest kitRequest, Enrollee enrollee, Profile profile) {

        SIDRTest test = SIDRTest.builder().testCode("R_DTP_REC_AND_STORE").build();
        String enrolleeId = config.isKitUseResearchIds() ? enrollee.getResearchId() : enrollee.getShortcode();

        MailingAddress address = profile.getMailingAddress();
        SIDRShippingInformation shippingInfo = SIDRShippingInformation
                .builder()
                .kitType(toSidrKitType(kitRequest.getKitType()))
                .patientAddress(
                        SIDRKitAddress
                                .builder()
                                .line1(address.getStreet1())
                                .line2(address.getStreet2())
                                .city(address.getCity())
                                .zip(address.getPostalCode())
                                .country(address.getCountry())
                                .build()
                )
                .build();

        SIDRPatient patient = SIDRPatient
                .builder()
                .patientId(enrolleeId)
                .metadata(List.of(
                        SIDRMetadata.of("first_name", profile.getGivenName()),
                        SIDRMetadata.of("last_name", profile.getFamilyName()),
                        SIDRMetadata.of("sex_at_birth", normalizeSexAtBirth(profile.getSexAtBirth())),
                        SIDRMetadata.of("date_of_birth", normalizeDateOfBirth(profile.getBirthDate()))
                ))
                .build();

        SIDRSample sample = SIDRSample
                .builder()
                .shippingInformation(shippingInfo)
                .materialType(toSidrMaterialType(kitRequest.getKitType()))
                .patient(patient)
                .build();


        return SIDROrder
                .builder()
                .projectKey(config.getSidrProject())
                .numberOfSamples(1)
                .sampleKitRequested(true)
                .clinicalOrderAttestation(true)
                .externalOrderId(kitRequest.getExternalOrderId())
                .tests(List.of(test))
                .samples(List.of(sample))
                .build();
    }

    private static SIDROrder buildReturnOnlyKitRequestOrder(StudyEnvironmentConfig config, KitRequest kitRequest, Enrollee enrollee) {
        return null;
    }

    private static String toSidrKitType(KitType kitType) {
        if (kitType.getName().equals("SALIVA")) {
            return "SALIVA";
        } else if (kitType.getName().equals("BLOOD")) {
            return "CAPBLOOD";
        }

        throw new IllegalArgumentException("Unknown Kit Type: " + kitType);
    }

    private static String normalizeSexAtBirth(String sexAtBirth) {
        if (sexAtBirth != null) {
            if (sexAtBirth.equalsIgnoreCase("male") || sexAtBirth.equalsIgnoreCase("m")) {
                return "Male";
            } else if (sexAtBirth.equalsIgnoreCase("female") || sexAtBirth.equalsIgnoreCase("f")) {
                return "Female";
            }
            throw new IllegalArgumentException("Invalid Sex at Birth: " + sexAtBirth);
        }
        throw new IllegalArgumentException("Sex at Birth is required");
    }

    private static String toSidrMaterialType(KitType kitType) {
        if (kitType.getName().equals("SALIVA")) {
            return "BODILY_FLUID_SALIVA";
        } else if (kitType.getName().equals("BLOOD")) {
            return "WHOLE_BLOOD_CAPILLARY_BLOOD";
        }

        throw new IllegalArgumentException("Unknown Kit Type: " + kitType);
    }

    private static String normalizeDateOfBirth(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            return "";
        }
        // ISO_LOCAL_DATE = YYYY-MM-DD
        return dateOfBirth.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}

