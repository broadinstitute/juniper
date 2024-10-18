package bio.terra.pearl.core.service.participant.merge;


import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.participant.ParticipantNote;
import bio.terra.pearl.core.model.survey.SurveyResponse;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class EnrolleeMerge {
    private List<MergeAction<ParticipantTask, ?>> tasks = new ArrayList<>();
}
