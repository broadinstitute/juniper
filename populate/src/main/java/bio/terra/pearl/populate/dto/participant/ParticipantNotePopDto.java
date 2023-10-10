package bio.terra.pearl.populate.dto.participant;

import bio.terra.pearl.core.model.participant.ParticipantNote;
import bio.terra.pearl.populate.dto.AdminTaskPopDto;
import bio.terra.pearl.populate.dto.TimeShiftable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ParticipantNotePopDto extends ParticipantNote implements TimeShiftable {
  private Integer submittedHoursAgo;
  private String creatingAdminUsername;
  private AdminTaskPopDto task;
  private Integer kitRequestIndex; // index in the populate file of the kit this note corresponds to -- null if it doesn't correspond to a kit
}
