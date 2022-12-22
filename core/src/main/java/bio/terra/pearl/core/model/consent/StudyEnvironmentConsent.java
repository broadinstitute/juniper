package bio.terra.pearl.core.model.consent;

import bio.terra.pearl.core.model.BaseEntity;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class StudyEnvironmentConsent extends BaseEntity {
    private UUID studyEnvironmentId;
    private UUID consentFormId;
    private ConsentForm consentForm;

    private int consentOrder; // what order the survey will be given in, compared to other surveys triggered at the same time
    private String eligibilityRule;
    @Builder.Default
    private boolean allowAdminEdit = false; // whether study staff can edit this
    @Builder.Default
    private boolean allowParticipantStart = true; // whether this survey can be completed by participants
    @Builder.Default
    private boolean allowParticipantReedit = false; // whether participants can change answers after submission
    @Builder.Default
    private boolean prepopulate = false; // whether to bring forward answers from prior completions (if recur is true)
}
