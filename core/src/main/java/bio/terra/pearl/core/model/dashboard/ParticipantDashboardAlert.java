package bio.terra.pearl.core.model.dashboard;

import java.util.UUID;

import bio.terra.pearl.core.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ParticipantDashboardAlert extends BaseEntity {
    private UUID portalEnvironmentId;
    private String title;
    private String detail;
    private AlertTrigger trigger;
    private AlertType type;
}
