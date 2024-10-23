package bio.terra.pearl.core.service.export.formatters.item;

import bio.terra.pearl.core.service.export.DataValueExportType;
import bio.terra.pearl.core.service.export.formatters.ExportFormatUtils;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtils;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Arrays;

import static bio.terra.pearl.core.service.export.formatters.ExportFormatUtils.DATA_TYPE_MAP;

@SuperBuilder
@Slf4j
public class PropertyItemFormatter<T> extends ItemFormatter<T> {
    @Getter
    private String propertyName;
    @Getter
    private Class<?> propertyClass;

    public PropertyItemFormatter(String propertyName, Class<T> beanClass) {
        this.propertyName = propertyName;
        this.propertyClass = getPropertyClass(beanClass, propertyName);
        this.dataType = DATA_TYPE_MAP.getOrDefault(propertyClass, DataValueExportType.STRING);
        this.baseColumnKey = propertyName;
    }

    public Class<?> getPropertyClass(Class<?> beanClass, String propertyName) {
        try {
            BeanInfo info = Introspector.getBeanInfo(beanClass);
            String simplePropertyName;
            if (propertyName.contains(".")) {
                simplePropertyName = propertyName.substring(0, propertyName.indexOf("."));
            } else {
                simplePropertyName = propertyName;
            }
            PropertyDescriptor descriptor = Arrays.stream(info.getPropertyDescriptors()).filter(pd -> pd.getName().equals(simplePropertyName))
                    .findFirst().get();
            Class<?> clazz = descriptor.getPropertyType();
            if (propertyName.contains(".")) {
                return getPropertyClass(clazz, propertyName.substring(propertyName.indexOf(".") + 1));
            }
            return clazz;
        } catch (Exception e) {
            throw new IllegalStateException("Can't determine property class", e);
        }
    }

    public  Object getRawExportValue(T bean) {
        Object value = null;
        try {
            value = PropertyUtils.getNestedProperty(bean, propertyName);
        } catch (NullPointerException | NestedNullException e) {
            // this is expected if the property is null
        } catch (Exception e) {
            log.warn("Error getting property {} from bean {}", propertyName, bean, e);
        }
        return value;
    }

    /**
     * because we need to generate a tsv,
     * we format everything as a string exactly as the characters should appear in the tsv,
     * the metadata will include information on the actual data type
     */
    public String getExportString(T bean) {
        return ExportFormatUtils.formatForExport(getRawExportValue(bean));
    }


    @Override
    public void importValueToBean(T bean, String exportString) {
        /**
         * don't attempt to set null values -- if the value was not in the import map, we want to use
         * the application default
         */
        if (exportString != null) {
            Object value = ExportFormatUtils.getValueFromString(exportString, dataType);
            try {
                PropertyUtils.setNestedProperty(bean, getPropertyName(), value);
            } catch (Exception e) {
                log.error("error setting property " + getPropertyName(), e);
            }
        }
    }
}
