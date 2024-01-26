package bio.terra.pearl.core.model.participant;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.consent.ConsentResponse;
import bio.terra.pearl.core.model.survey.SurveyResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class EnrolleeRelation extends BaseEntity {
    private UUID participantUserId;
    private UUID enrolleeId;
    private UUID relationshipId;
    private RelationshipType relationshipType;
    private boolean isProxy;
    @Builder.Default
    private List<SurveyResponse> surveyResponses = new ArrayList<>();
    @Builder.Default
    private List<ConsentResponse> consentResponses = new ArrayList<>();

    public enum RelationshipType{
        PROXY, OTHER;
    }
}
