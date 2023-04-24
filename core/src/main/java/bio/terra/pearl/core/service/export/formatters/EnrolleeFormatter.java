package bio.terra.pearl.core.service.export.formatters;

import bio.terra.pearl.core.model.survey.QuestionChoice;
import bio.terra.pearl.core.service.export.EnrolleeExportData;
import bio.terra.pearl.core.service.export.instance.ExportOptions;
import bio.terra.pearl.core.service.export.instance.ItemExportInfo;
import bio.terra.pearl.core.service.export.instance.ModuleExportInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EnrolleeFormatter implements ExportFormatter {
    public static final String ENROLLEE_MODULE_NAME = "enrollee";

    @Override
    public Map<String, String> toStringMap(EnrolleeExportData enrolleeData, ModuleExportInfo moduleExportInfo) throws Exception {
        return ExportFormatUtils.mapBeanForExport(enrolleeData.getEnrollee(), moduleExportInfo);
    }

    @Override
    public String getColumnKey(ModuleExportInfo moduleExportInfo, ItemExportInfo itemExportInfo, boolean isOtherDescription, QuestionChoice choice) {
        return itemExportInfo.getBaseColumnKey();
    }

    @Override
    public String getColumnHeader(ModuleExportInfo moduleExportInfo, ItemExportInfo itemExportInfo, boolean isOtherDescription, QuestionChoice choice) {
        return itemExportInfo.getBaseColumnKey();
    }

    @Override
    public String getColumnSubHeader(ModuleExportInfo moduleExportInfo, ItemExportInfo itemExportInfo, boolean isOtherDescription, QuestionChoice choice) {
        if ("createdAt".equals(itemExportInfo.getPropertyAccessor())) {
            return "Registration date";
        }
        return ExportFormatUtils.camelToWordCase(itemExportInfo.getPropertyAccessor());
    }

    public ModuleExportInfo getModuleExportInfo(ExportOptions exportOptions) throws Exception {
        List<ItemExportInfo> itemInfo = new ArrayList<>();
        itemInfo.add(ExportFormatUtils.getItemInfoForBeanProp(ENROLLEE_MODULE_NAME, "shortcode"));
        itemInfo.add(ExportFormatUtils.getItemInfoForBeanProp(ENROLLEE_MODULE_NAME, "consented"));
        itemInfo.add(ExportFormatUtils.getItemInfoForBeanProp(ENROLLEE_MODULE_NAME, "createdAt"));
        return ModuleExportInfo.builder()
                .maxNumRepeats(1)
                .items(itemInfo)
                .formatter(this)
                .build();
    }
}
