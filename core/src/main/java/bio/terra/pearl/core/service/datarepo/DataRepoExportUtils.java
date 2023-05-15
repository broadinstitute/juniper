package bio.terra.pearl.core.service.datarepo;

import bio.terra.datarepo.model.TableDataType;
import bio.terra.pearl.core.service.export.formatters.DataValueExportType;

public class DataRepoExportUtils {

    //Converts a Juniper column type into a Data Repo column type
    public static TableDataType juniperToDataRepoColumnType(DataValueExportType columnType) {
        return switch(columnType) {
            case DATE -> TableDataType.DATE;
            case DATE_TIME -> TableDataType.DATETIME;
            case STRING -> TableDataType.STRING;
            case NUMBER -> TableDataType.INTEGER;
            case BOOLEAN -> TableDataType.BOOLEAN;
        };
    }

    //Converts a Juniper column name into a TDR-accepted column name
    public static String juniperToDataRepoColumnName(String columnName) {
        return columnName
                .substring(0, Math.min(columnName.length(), 63)) //TDR accepts a maximum column name length of 64 chars
                .replace('.', '_'); //Juniper uses periods, but those are invalid in TDR

    }

}
