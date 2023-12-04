package bio.terra.pearl.core.model.publishing;

import bio.terra.pearl.core.model.dashboard.AlertTrigger;

import java.util.List;

public record ParticipantDashboardAlertChange(AlertTrigger trigger, List<ConfigChange> changes) {
}
