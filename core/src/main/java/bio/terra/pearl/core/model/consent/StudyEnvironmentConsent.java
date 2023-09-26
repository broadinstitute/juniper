package bio.terra.pearl.core.model.consent;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.Versioned;
import bio.terra.pearl.core.model.publishing.VersionedEntityConfig;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/** See https://broadworkbench.atlassian.net/wiki/spaces/PEARL/pages/2669281289/Consent+forms */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class StudyEnvironmentConsent extends BaseEntity implements VersionedEntityConfig {
    private UUID studyEnvironmentId;
    private UUID consentFormId;
    private ConsentForm consentForm;

    private int consentOrder; // what order the consent will be given in, compared to other consents triggered at the same time
    private String eligibilityRule;
    @Builder.Default
    private boolean studyRequired = true;
    @Builder.Default
    private boolean allowAdminEdit = false; // whether study staff can edit this
    @Builder.Default
    private boolean allowParticipantStart = true; // whether this survey can be completed by participants
    @Builder.Default
    private boolean allowParticipantReedit = false; // whether participants can change answers after submission

    @Override
    public Versioned versionedEntity() {
        return consentForm;
    }
    @Override
    public UUID versionedEntityId() { return consentFormId; }
    @Override
    public void updateVersionedEntityId(UUID consentId) {
        setConsentFormId(consentId);
    }
}
