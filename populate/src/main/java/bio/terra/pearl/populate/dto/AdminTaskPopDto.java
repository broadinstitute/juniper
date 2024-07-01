package bio.terra.pearl.populate.dto;

import bio.terra.pearl.core.model.workflow.ParticipantTask;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminTaskPopDto extends ParticipantTask implements TimeShiftable {
    private Integer submittedHoursAgo;
    private String assignedToUsername;
    private String creatingAdminUsername;
}
