package bio.terra.pearl.api.admin.service.participant;

import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import java.util.List;

public record ParticipantUsersWithEnrollees(
    List<ParticipantUser> participantUsers, List<Enrollee> enrollees) {}
