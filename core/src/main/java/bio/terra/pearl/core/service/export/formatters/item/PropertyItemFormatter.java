package bio.terra.pearl.core.service.export.formatters.item;

import bio.terra.pearl.core.service.export.DataValueExportType;
import bio.terra.pearl.core.service.export.formatters.ExportFormatUtils;
import bio.terra.pearl.core.service.export.formatters.item.ItemFormatter;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
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

    public PropertyItemFormatter(String propertyName, Class<T> beanClass) {
        this.propertyName = propertyName;
        this.dataType = getDataValueExportType(beanClass, propertyName);
        this.baseColumnKey = propertyName;
    }

    public <T> DataValueExportType getDataValueExportType(Class<T> beanClass, String propertyName) {
        DataValueExportType dataType = DataValueExportType.STRING;
        try {
            BeanInfo info = Introspector.getBeanInfo(beanClass);
            PropertyDescriptor descriptor = Arrays.stream(info.getPropertyDescriptors()).filter(pd -> pd.getName().equals(propertyName))
                    .findFirst().get();
            dataType = DATA_TYPE_MAP.getOrDefault(descriptor.getPropertyType(), DataValueExportType.STRING);
        } catch (Exception e) {
            // default is string
        }
        return dataType;
    }

    public  Object getRawExportValue(T bean) {
        Object value = null;
        try {
            value = PropertyUtils.getNestedProperty(bean, propertyName);
        } catch (Exception e) {
            log.warn("Error getting property {} from bean {}", propertyName, bean, e);
        }
        return value;
    }

    @Override
    public String getExportString(T bean) {
        return ExportFormatUtils.formatForExport(getRawExportValue(bean));
    }


}
