package bio.terra.pearl.core.service.export;

import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.survey.Answer;
import bio.terra.pearl.core.model.survey.SurveyResponse;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.service.kit.KitRequestDto;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Builder @AllArgsConstructor
public class EnrolleeExportData {
    private Enrollee enrollee;
    private Profile profile;
    private List<Answer> answers;
    private List<ParticipantTask> tasks;
    private List<SurveyResponse> responses;
    private List<KitRequestDto> kitRequests;
}
