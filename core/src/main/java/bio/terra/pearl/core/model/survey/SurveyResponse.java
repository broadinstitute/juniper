package bio.terra.pearl.core.model.survey;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.audit.ResponsibleEntity;
import bio.terra.pearl.core.model.file.ParticipantFile;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    private List<ParticipantFile> participantFiles = new ArrayList<>();
    @Builder.Default
    private boolean complete = false;
    // a json map of userId -> an object with information about where that particular user left off
    // currently, the only property stored on that object is currentPageNo
    private String resumeData;
    // a json blob containing metadata about the response
    // for pre-enrollment responses this tracks referralSource information
    // future use could include things like how long the participant spent on the response, etc
    private String responseMetadata;

    public void setResponsibleUser(ResponsibleEntity responsibleEntity) {
        if (responsibleEntity == null) {
            return;
        }
        if (responsibleEntity.getParticipantUser() != null) {
            this.creatingParticipantUserId = responsibleEntity.getParticipantUser().getId();
        }
        if (responsibleEntity.getAdminUser() != null) {
            this.creatingAdminUserId = responsibleEntity.getAdminUser().getId();
        }
    }
}
