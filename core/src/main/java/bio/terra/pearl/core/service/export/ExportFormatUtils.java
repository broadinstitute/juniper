package bio.terra.pearl.core.service.export;

import bio.terra.pearl.core.model.participant.Profile;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.beanutils.PropertyUtils;

public class ExportFormatUtils {
    public static final String ANALYSIS_DATE_FORMAT = "yyyy-MM-dd";
    public static final String NULL_STRING = "";
    public static final String COLUMN_NAME_DELIMITER = ".";
    public static String formatBoolean(Boolean bool) {
        return bool.toString();
    }

    public static String formatLocalDate(LocalDate localDate) {
        return localDate.format(DateTimeFormatter.ofPattern(ANALYSIS_DATE_FORMAT));
    }

    public static String formatForExport(Object value) {
        if (value == null) {
            return NULL_STRING;
        }
        if (Boolean.class.isInstance(value)) {
            return formatBoolean((Boolean) value);
        } else if (LocalDate.class.isInstance(value)) {
            return formatLocalDate((LocalDate) value);
        }
        return value.toString();
    }

    public static Map<String, String> mapBeanForExport(Object bean, List<String> excludedProps,
                                                       String propertyPrefix) throws Exception {
        Map<String, String> valueMap = new HashMap<>();
        BeanInfo info = Introspector.getBeanInfo(Profile.class);
        List<String> profileIncludedProperties = Arrays.asList(info.getPropertyDescriptors()).stream()
                .map(descriptor -> descriptor.getName())
                .filter(name -> excludedProps.contains(name))
                .collect(Collectors.toList());
        for (String propertyName : profileIncludedProperties) {
            Object value = PropertyUtils.getProperty(bean, propertyName);
            String columnKey = propertyPrefix + COLUMN_NAME_DELIMITER + propertyName;
            String columnValue = ExportFormatUtils.formatForExport(value);
            valueMap.put(columnKey, columnValue);
        }
        return valueMap;
    }

}
