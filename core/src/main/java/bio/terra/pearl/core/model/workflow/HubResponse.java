package bio.terra.pearl.core.model.workflow;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.participant.Enrollee;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Designed for API responses which also need to keep hub content in sync.  For example,
 * a response to a consent form submission will contain an updated task list
 */
@Getter @Setter
@Builder
public class HubResponse<T extends BaseEntity> {
    private List<ParticipantTask> tasks;
    private T response;
    private Enrollee enrollee;
}
