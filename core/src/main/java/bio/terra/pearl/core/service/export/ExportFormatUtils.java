package bio.terra.pearl.core.service.export;

import bio.terra.pearl.core.service.export.instance.ItemExportInfo;
import bio.terra.pearl.core.service.export.instance.ModuleExportInfo;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;

public class ExportFormatUtils {
    public static final String ANALYSIS_DATE_FORMAT = "yyyy-MM-dd";
    public static final String ANALYSIS_DATE_TIME_FORMAT = "yyyy-MM-dd hh:mma";
    public static final String NULL_STRING = "";
    public static final String COLUMN_NAME_DELIMITER = ".";
    public static String formatBoolean(Boolean bool) {
        return bool.toString();
    }

    public static String formatLocalDate(LocalDate localDate) {
        return localDate.format(DateTimeFormatter.ofPattern(ANALYSIS_DATE_FORMAT));
    }

    public static String formatInstant(Instant instant) {
        return DateTimeFormatter.ofPattern(ANALYSIS_DATE_TIME_FORMAT)
                .withZone(ZoneOffset.UTC).format(instant);
    }

    public static String formatForExport(Object value) {
        if (value == null) {
            return NULL_STRING;
        }
        if (Boolean.class.isInstance(value)) {
            return formatBoolean((Boolean) value);
        } else if (LocalDate.class.isInstance(value)) {
            return formatLocalDate((LocalDate) value);
        } else if (Instant.class.isInstance(value)) {
            return formatInstant((Instant) value);
        }
        return value.toString();
    }

    public static List<String> getIncludedProperties(Class clazz, List<String> excludedProps) throws Exception {
        BeanInfo info = Introspector.getBeanInfo(clazz);
        return Arrays.asList(info.getPropertyDescriptors()).stream()
                .map(descriptor -> descriptor.getName())
                .filter(name -> !excludedProps.contains(name))
                .collect(Collectors.toList());
    }

    public static Map<String, String> mapBeanForExport(Object bean, ModuleExportInfo moduleInfo) throws Exception {
        Map<String, String> valueMap = new HashMap<>();
        for (ItemExportInfo itemInfo : moduleInfo.getItems()) {
            addPropertyForExport(bean, itemInfo, valueMap);
        }
        return valueMap;
    }

    public static void addPropertyForExport(Object bean, ItemExportInfo itemExportInfo, Map<String, String> valueMap) throws Exception {
        Object value = null;
        try {
            value = PropertyUtils.getNestedProperty(bean, itemExportInfo.getPropertyAccessor());
        } catch (NestedNullException e) {
            // do nothing
        }
        String columnValue = ExportFormatUtils.formatForExport(value);
        valueMap.put(itemExportInfo.getBaseColumnKey(), columnValue);
    }

    public static ItemExportInfo getItemInfoForBeanProp(String moduleName, String propertyName) {
        return ItemExportInfo.builder()
                .propertyAccessor(propertyName)
                .baseColumnKey(moduleName + COLUMN_NAME_DELIMITER + propertyName)
                .build();
    }

    /** converts, e.g. "mailingAddress" to "Mailing Address" */
    public static String camelToWordCase(String camelCased) {
        String spacedString = camelCased.replace(".", " - ");
        return StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(
                        StringUtils.capitalize(spacedString)), " ");
    }

    public static String getColumnKey(String moduleName, String fieldName) {
        return moduleName + COLUMN_NAME_DELIMITER + fieldName;
    }

}
