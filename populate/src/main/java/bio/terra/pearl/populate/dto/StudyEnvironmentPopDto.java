package bio.terra.pearl.populate.dto;

import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.populate.dto.consent.StudyEnvironmentConsentPopDto;
import bio.terra.pearl.populate.dto.notifications.TriggerPopDto;
import bio.terra.pearl.populate.dto.survey.PreEnrollmentResponsePopDto;
import bio.terra.pearl.populate.dto.survey.StudyEnvironmentSurveyPopDto;
import bio.terra.pearl.populate.dto.survey.SurveyPopDto;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class StudyEnvironmentPopDto extends StudyEnvironment {
    @Builder.Default
    private List<StudyEnvironmentSurveyPopDto> configuredSurveyDtos = new ArrayList<>();
    @Builder.Default
    private List<StudyEnvironmentConsentPopDto> configuredConsentDtos = new ArrayList<>();
    @Builder.Default
    private List<TriggerPopDto> triggerDtos = new ArrayList<>();
    @Builder.Default
    private List<PreEnrollmentResponsePopDto> preEnrollmentResponseDtos = new ArrayList<>();
    @Builder.Default
    private List<String> kitTypeNames = new ArrayList<>();
    @Builder.Default
    private List<String> enrolleeFiles = new ArrayList<>();

    private SurveyPopDto preEnrollSurveyDto;
}
