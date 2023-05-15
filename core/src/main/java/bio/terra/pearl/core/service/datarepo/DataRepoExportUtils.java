package bio.terra.pearl.core.service.datarepo;

import bio.terra.datarepo.model.TableDataType;
import bio.terra.pearl.core.service.export.formatters.DataValueExportType;

public class DataRepoExportUtils {

    private TableDataType exportTypeToColumnType(DataValueExportType foo) {
        return switch(foo) {
            case DATE -> TableDataType.DATE;
            case DATE_TIME -> TableDataType.DATETIME;
            case STRING -> TableDataType.STRING;
            case NUMBER -> TableDataType.INTEGER;
            case BOOLEAN -> TableDataType.BOOLEAN;
        };
    }

}
