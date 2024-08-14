package bio.terra.pearl.core.service.export.formatters;

import bio.terra.pearl.core.service.export.DataValueExportType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class ExportFormatUtils {
    public static final String ANALYSIS_DATE_FORMAT = "yyyy-MM-dd";
    public static final String ANALYSIS_DATE_TIME_FORMAT = "yyyy-MM-dd hh:mma";
    public static final String NULL_STRING = "";
    public static final String COLUMN_NAME_DELIMITER = ".";

    public static final Map<Class, DataValueExportType> DATA_TYPE_MAP = Map.of(
            String.class, DataValueExportType.STRING,
            Double.class, DataValueExportType.NUMBER,
            Integer.class, DataValueExportType.NUMBER,
            LocalDate.class, DataValueExportType.DATE,
            Instant.class, DataValueExportType.DATE_TIME,
            Boolean.class, DataValueExportType.BOOLEAN,
            boolean.class, DataValueExportType.BOOLEAN
    );
    public static String formatBoolean(Boolean bool) {
        return bool.toString();
    }

    public static String formatLocalDate(LocalDate localDate) {
        return localDate.format(DateTimeFormatter.ofPattern(ANALYSIS_DATE_FORMAT));
    }

    public static LocalDate importLocalDate(String localDateString) {
        if (StringUtils.isBlank(localDateString)) {
            return null;
        }
        return LocalDate.parse(localDateString, DateTimeFormatter.ofPattern(ANALYSIS_DATE_FORMAT));
    }

    public static String formatInstant(Instant instant) {
        return DateTimeFormatter.ofPattern(ANALYSIS_DATE_TIME_FORMAT)
                .withZone(ZoneOffset.UTC).format(instant);
    }

    public static Instant importInstant(String instantString) {
        if (StringUtils.isBlank(instantString)) {
            return null;
        }
        return Instant.from(
                DateTimeFormatter.ofPattern(ANALYSIS_DATE_TIME_FORMAT)
                        .withZone(ZoneId.of("Z")) // for now do everything in UTC
                        .parse(instantString)
        );
    }

    /** simple property formatter -- just branches on the class of the thing to format */
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

    public static List<String> getIncludedProperties(Class clazz, List<String> excludedProps) {
        try {
            BeanInfo info = Introspector.getBeanInfo(clazz);
            return Arrays.asList(info.getPropertyDescriptors()).stream()
                    .map(descriptor -> descriptor.getName())
                    .filter(name -> !excludedProps.contains(name))
                    .collect(Collectors.toList());
        } catch (IntrospectionException e) {
            throw new IllegalStateException("Error introspecting class " + clazz.getName(), e);
        }

    }

    /** converts, e.g. "mailingAddress" to "Mailing Address" */
    public static String camelToWordCase(String camelCased) {
        String spacedString = camelCased.replace(".", " - ");
        return StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(
                        StringUtils.capitalize(spacedString)), " ");
    }

    public static Object getValueFromString(String exportString, DataValueExportType dataType) {
        if (dataType.equals(DataValueExportType.DATE_TIME)) {
            return ExportFormatUtils.importInstant(exportString);
        } else if (dataType.equals(DataValueExportType.DATE)) {
            return ExportFormatUtils.importLocalDate(exportString);
        } else if (dataType.equals(DataValueExportType.BOOLEAN)) {
            return Boolean.valueOf(exportString);
        }
        return exportString;
    }

    public static String formatIndex(int index) {
        return String.format("[%d]", index);
    }

}
