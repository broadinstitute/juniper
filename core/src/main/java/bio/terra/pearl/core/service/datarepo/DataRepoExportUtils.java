package bio.terra.pearl.core.service.datarepo;

import bio.terra.datarepo.model.TableDataType;
import bio.terra.pearl.core.service.export.DataValueExportType;

public class DataRepoExportUtils {

    //Converts a Juniper column type into a Data Repo column type
    public static TableDataType juniperToDataRepoColumnType(DataValueExportType columnType) {
        return switch(columnType) {
            //TODO: Properly format DATETIMEs instead of using STRING.
            case DATE_TIME -> TableDataType.STRING;
            case DATE -> TableDataType.DATE;
            case STRING, OBJECT_STRING -> TableDataType.STRING;
            case NUMBER -> TableDataType.INTEGER;
            case BOOLEAN -> TableDataType.BOOLEAN;
        };
    }

    //Converts a Juniper column name into a TDR-accepted column name
    //Note that if two columns have the same initial 64 characters, this will cause
    //TDR to reject the schema creation request with a validation error. See
    //discussion here: https://github.com/broadinstitute/pearl/pull/376/files#r1198272411
    public static String juniperToDataRepoColumnName(String columnName) {
        return columnName
                .substring(0, Math.min(columnName.length(), 63)) //TDR accepts a maximum column name length of 64 chars
                .replace('.', '_'); //Juniper uses periods, but those are invalid in TDR

    }

}
