package bio.terra.pearl.populate.dto;

import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.populate.dto.notifications.TriggerPopDto;
import bio.terra.pearl.populate.dto.survey.PreEnrollmentResponsePopDto;
import bio.terra.pearl.populate.dto.survey.StudyEnvironmentSurveyPopDto;
import bio.terra.pearl.populate.dto.survey.SurveyPopDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class StudyEnvironmentPopDto extends StudyEnvironment {
    @Builder.Default
    private List<StudyEnvironmentSurveyPopDto> configuredSurveyDtos = new ArrayList<>();
    @Builder.Default
    private List<TriggerPopDto> triggerDtos = new ArrayList<>();
    @Builder.Default
    private List<PreEnrollmentResponsePopDto> preEnrollmentResponseDtos = new ArrayList<>();
    @Builder.Default
    private List<String> kitTypeNames = new ArrayList<>();
    @Builder.Default
    private List<String> enrolleeFiles = new ArrayList<>();
    @Builder.Default
    private List<String> familyFiles = new ArrayList<>();

    private SurveyPopDto preEnrollSurveyDto;
}
