package bio.terra.pearl.populate.dto.consent;

import bio.terra.pearl.core.model.consent.StudyEnvironmentConsent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Dto so that we can specify the survey by stableId/Version rather than id */
@Getter
@Setter
@NoArgsConstructor
public class StudyEnvironmentConsentPopDto extends StudyEnvironmentConsent {
    private String consentStableId;
    private int consentVersion;
}
