package bio.terra.pearl.core.dao.consent;

import bio.terra.pearl.core.model.consent.ConsentResponse;
import bio.terra.pearl.core.model.consent.StudyEnvironmentConsent;
import java.util.List;

/** convenience class for grouping together a form and its configuration and responses */
public record ConsentWithResponses(StudyEnvironmentConsent studyEnvironmentConsent,
                                   List<ConsentResponse> consentResponses) {
}
