package bio.terra.pearl.core.service.export.formatters.module;

import bio.terra.pearl.core.model.export.ExportOptions;
import bio.terra.pearl.core.model.survey.QuestionChoice;
import bio.terra.pearl.core.service.export.EnrolleeExportData;
import bio.terra.pearl.core.service.export.formatters.ExportFormatUtils;
import bio.terra.pearl.core.service.export.formatters.item.PropertyItemFormatter;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
/** ModuleFormatter for just listing properties of a thing -- e.g. fields from a profile */
public abstract class BeanListModuleFormatter<T> extends ModuleFormatter<T, PropertyItemFormatter<T>> {

    public BeanListModuleFormatter(ExportOptions options, String moduleName, String displayName) {
        super(options, moduleName, displayName);
    }

    @Override
    public String getColumnSubHeader(PropertyItemFormatter itemFormatter, boolean isOtherDescription, QuestionChoice choice, int moduleRepeatNum) {
        return ExportFormatUtils.camelToWordCase(itemFormatter.getPropertyName());
    }

    public abstract List<T> getBeans(EnrolleeExportData enrolleeExportData);

    public abstract Comparator<T> getComparator();

    @Override
    public Map<String, String> toStringMap(EnrolleeExportData enrolleeExportData) {
        List<T> beanList = getBeans(enrolleeExportData);

        beanList = beanList.stream().sorted(getComparator()).toList();

        Map<String, String> valueMap = new HashMap<>();
        for (int i = 0; i < beanList.size(); i++) {
            for (PropertyItemFormatter<T> itemInfo : getItemFormatters()) {
                String value = itemInfo.getExportString(beanList.get(i));
                String columnName = getColumnKey(itemInfo, false, null, i+1);
                valueMap.put(columnName, value);
            }
        }
        maxNumRepeats = Math.max(maxNumRepeats, beanList.size());
        return valueMap;
    }

}
