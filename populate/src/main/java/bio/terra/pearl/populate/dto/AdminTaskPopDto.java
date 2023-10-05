package bio.terra.pearl.populate.dto;

import bio.terra.pearl.core.model.workflow.AdminTask;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminTaskPopDto extends AdminTask implements TimeShiftable {
    private Integer submittedHoursAgo;
    private String assignedToUsername;
    private String creatingAdminUsername;
}
