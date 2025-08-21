package bio.terra.pearl.core.model.workflow;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.Profile;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

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
    /** the hub response should include an updated profile so surveys can branch based on it */
    private Profile profile;

    private Enrollee proxyEnrollee;
    private Profile proxyProfile;
}
