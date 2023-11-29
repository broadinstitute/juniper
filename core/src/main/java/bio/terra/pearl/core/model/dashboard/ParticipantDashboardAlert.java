package bio.terra.pearl.core.model.dashboard;

import java.util.UUID;

import bio.terra.pearl.core.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

//TODO: should these be versioned as well?
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ParticipantDashboardAlert extends BaseEntity {
    private UUID portalEnvironmentId;
    private UUID studyEnvironmentId; //nullable. if this is set for an alert, it takes precedence over an equivalent portal alert in the UI

    private String title;
    private String detail;
    private AlertTrigger trigger;
    private AlertType type;
}
