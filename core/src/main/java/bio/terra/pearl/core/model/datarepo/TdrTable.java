package bio.terra.pearl.core.model.datarepo;

import java.util.Set;

public record TdrTable(String tableName, String primaryKey, Set<TdrColumn> columns) {}
