package bio.terra.pearl.core.model.survey;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.PortalAttached;
import bio.terra.pearl.core.model.Versioned;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import bio.terra.pearl.core.model.form.VersionedForm;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter @Setter @NoArgsConstructor @SuperBuilder
public class Survey extends VersionedForm {
    @Builder.Default
    private SurveyType surveyType = SurveyType.RESEARCH;
    private String blurb; // brief description of the survey for, e.g., showing in a dashboard
    @Builder.Default
    private List<AnswerMapping> answerMappings = new ArrayList<>();
    // markdown to be displayed below every page of the survey
    private String footer;
    @Builder.Default
    private List<AnswerMapping> triggers = new ArrayList<>();

    @Builder.Default
    private boolean recur = false;
    @Builder.Default
    private boolean required = false; // whether this is required before other non-required surveys can be taken
    // how many days between offerings of this survey (e.g. 365 for one year)
    private Integer recurrenceIntervalDays;
    // how many days after being eligible (e.g. after consent, or rule triggering) to offer the survey
    private Integer daysAfterEligible;
    private String eligibilityRule;
    @Builder.Default
    private boolean allowAdminEdit = true; // whether study staff can edit this
    @Builder.Default
    private boolean allowParticipantStart = true; // whether this survey can be completed by participants
    @Builder.Default
    private boolean allowParticipantReedit = true; // whether participants can change answers after submission
    @Builder.Default
    private boolean prepopulate = false; // whether to bring forward answers from prior completions (if recur is true)
    @Builder.Default
    private boolean assignToAllNewEnrollees = true; // whether to assign the survey to all new enrollees by default
    @Builder.Default
    private boolean assignToExistingEnrollees = false; // whether to assign the survey automatically to existing enrollees
    @Builder.Default
    private boolean autoUpdateTaskAssignments = false; // whether to auto-update all tasks to the latest version on publish
}

