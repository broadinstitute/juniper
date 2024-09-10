package bio.terra.pearl.core.model.workflow;

import bio.terra.pearl.core.model.audit.ParticipantDataChange;

import java.util.List;

/** trivial container for an object and associated changes */
public record ObjectWithChangeLog<T>(T obj, List<ParticipantDataChange> changeRecords) { }
