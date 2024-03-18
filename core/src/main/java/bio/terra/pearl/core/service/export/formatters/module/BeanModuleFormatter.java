package bio.terra.pearl.core.service.export.formatters.module;

import bio.terra.pearl.core.model.survey.QuestionChoice;
import bio.terra.pearl.core.service.export.EnrolleeExportData;
import bio.terra.pearl.core.service.export.formatters.ExportFormatUtils;
import bio.terra.pearl.core.service.export.formatters.item.PropertyItemFormatter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
/** ModuleFormatter for just listing properties of a thing -- e.g. fields from a profile */
public abstract class BeanModuleFormatter<T> extends ModuleFormatter<T, PropertyItemFormatter<T>> {

    @Override
    public String getColumnSubHeader(PropertyItemFormatter itemFormatter, boolean isOtherDescription, QuestionChoice choice, int moduleRepeatNum) {
        return ExportFormatUtils.camelToWordCase(itemFormatter.getPropertyName());
    }

    public abstract T getBean(EnrolleeExportData enrolleeExportData);

    @Override
    public Map<String, String> toStringMap(EnrolleeExportData enrolleeExportData) {
        T bean = getBean(enrolleeExportData);
        Map<String, String> valueMap = new HashMap<>();
        for (PropertyItemFormatter<T> itemInfo : getItemFormatters()) {
            String value = itemInfo.getExportString(bean);
            String columnName = getColumnKey(itemInfo, false, null, 1);
            valueMap.put(columnName, value);
        }
        return valueMap;
    }

    @Override
    public T fromStringMap(UUID studyEnvironmentId, Map<String, String> enrolleeMap) {
        T bean = newBean();
        for (PropertyItemFormatter<T> itemInfo : getItemFormatters()) {
            String columnName = getColumnKey(itemInfo, false, null, 1);
            String stringVal = enrolleeMap.get(columnName);
            Object value = itemInfo.getValueFromString(stringVal);
            try {
                PropertyUtils.setNestedProperty(bean, itemInfo.getPropertyName(), value);
            } catch (Exception e) {
                log.error("error importing property " + itemInfo.getPropertyName(), e);
            }
        }
        return bean;
    }

    protected abstract T newBean();

}
