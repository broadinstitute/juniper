package bio.terra.pearl.core.model.workflow;

import java.util.List;

/** trivial container for an object and associated changes */
public record ObjectWithChangeLog<T>(T obj, List<DataChangeRecord> changeRecords) { }
