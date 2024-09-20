package bio.terra.pearl.core.model.export.datarepo;

import bio.terra.datarepo.model.TableDataType;

public record TdrColumn(String columnName, TableDataType dataType) {}
