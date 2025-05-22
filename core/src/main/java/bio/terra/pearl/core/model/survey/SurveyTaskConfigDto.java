package bio.terra.pearl.core.model.survey;

import bio.terra.pearl.core.model.workflow.RecurOn;
import bio.terra.pearl.core.model.workflow.RecurrenceType;
import bio.terra.pearl.core.service.workflow.TaskConfig;
import lombok.Getter;

import java.util.UUID;

@Getter
public class SurveyTaskConfigDto implements TaskConfig {
    private final StudyEnvironmentSurvey studyEnvironmentSurvey;
    private final Survey survey;

    public SurveyTaskConfigDto(StudyEnvironmentSurvey studyEnvironmentSurvey, Survey survey) {
        if (studyEnvironmentSurvey == null || survey == null) {
            throw new IllegalArgumentException("studyEnvironmentSurvey and survey must be non-null");
        }
        if (!studyEnvironmentSurvey.getSurveyId().equals(survey.getId())) {
            throw new IllegalArgumentException("studyEnvironmentSurvey and survey must be for the same survey");
        }

        this.studyEnvironmentSurvey = studyEnvironmentSurvey;
        this.survey = survey;
    }

    public SurveyTaskConfigDto(StudyEnvironmentSurvey studyEnvironmentSurvey) {
        this(studyEnvironmentSurvey, studyEnvironmentSurvey.getSurvey());
    }

    @Override
    public String getStableId() {
        return survey.getStableId();
    }

    @Override
    public Integer getVersion() {
        return survey.getVersion();
    }

    @Override
    public String getName() {
        return survey.getName();
    }

    @Override
    public UUID getStudyEnvironmentId() {
        return studyEnvironmentSurvey.getStudyEnvironmentId();
    }

    @Override
    public RecurrenceType getRecurrenceType() {
        return survey.getRecurrenceType();
    }

    @Override
    public RecurOn getRecurOn() {
        return survey.getRecurOn();
    }

    @Override
    public Integer getDaysAfterEligible() {
        return survey.getDaysAfterEligible();
    }

    @Override
    public Integer getRecurrenceIntervalDays() {
        return survey.getRecurrenceIntervalDays();
    }

    @Override
    public String getEligibilityRule() {
        return survey.getEligibilityRule();
    }

    @Override
    public Boolean isAutoAssign() {
        return survey.isAutoAssign();
    }

    @Override
    public Boolean isAutoUpdateTaskAssignments() {
        return survey.isAutoUpdateTaskAssignments();
    }

    @Override
    public Boolean isAssignToExistingEnrollees() {
        return survey.isAssignToExistingEnrollees();
    }

    @Override
    public Boolean isRequired() {
        return survey.isRequired();
    }

    @Override
    public Integer getTaskOrder() {
        return studyEnvironmentSurvey.getSurveyOrder();
    }
}
