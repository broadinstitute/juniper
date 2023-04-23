package bio.terra.pearl.core.service.export;

import bio.terra.pearl.core.model.survey.QuestionChoice;
import bio.terra.pearl.core.service.export.instance.ItemExportInfo;
import bio.terra.pearl.core.service.export.instance.ModuleExportInfo;
import java.util.Map;

public interface ExportFormatter {
    Map<String, String> toStringMap(EnrolleeExportData enrolleeData, ModuleExportInfo moduleInfo) throws Exception;

    /** the header is the guaranteed unique key for the column.  In simple cases, this will just be the
     * itemExportInfo.getBaseColumnKey.  But for cases involving split options, repeats, and other descriptions,
     * the key will be more complex */
    String getColumnKey(ModuleExportInfo moduleExportInfo, ItemExportInfo itemExportInfo,
                           boolean isOtherDescription, QuestionChoice choice);

    /** the header is a guaranteed unique header for the column, in a format suitable for scripting */
    String getColumnHeader(ModuleExportInfo moduleExportInfo, ItemExportInfo itemExportInfo,
                           boolean isOtherDescription, QuestionChoice choice);

    /** the subheader is a more human-readable format for the column */
    String getColumnSubHeader(ModuleExportInfo moduleExportInfo, ItemExportInfo itemExportInfo,
                           boolean isOtherDescription, QuestionChoice choice);
}
