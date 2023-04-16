package bio.terra.pearl.core.model.survey;

import bio.terra.pearl.core.model.BaseEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * container for response data from a given survey instance.
 */
@Getter
@Setter
@SuperBuilder @NoArgsConstructor
public class SurveyResponse extends BaseEntity {
    private UUID enrolleeId;
    private UUID creatingParticipantUserId;
    private UUID creatingAdminUserId;
    private UUID surveyId;
    @Builder.Default
    private List<Answer> answers = new ArrayList<>();
    @Builder.Default
    private boolean complete = false;
    // json map of userId -> { currentPageNo }
    private String resumeData;
}
