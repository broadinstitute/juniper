package bio.terra.pearl.core.model.datarepo;

import bio.terra.datarepo.model.TableDataType;

public record TdrColumn(String columnName, TableDataType dataType) {}
