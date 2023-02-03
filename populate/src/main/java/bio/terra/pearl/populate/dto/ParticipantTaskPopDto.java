package bio.terra.pearl.populate.dto;

import bio.terra.pearl.core.model.workflow.ParticipantTask;

public class ParticipantTaskPopDto extends ParticipantTask {
    // e.g. now-1d
    private String completedAtOffset;
}
