package bio.terra.pearl.core.model.publishing;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.beanutils.PropertyUtils;

public record ConfigChangeRecord(String propertyName, Object oldValue, Object newValue) {
    public ConfigChangeRecord(Object source, Object dest, String propertyName) throws Exception {
        this(propertyName,
                source != null ? PropertyUtils.getProperty(source, propertyName) : null,
                source != null ? PropertyUtils.getProperty(dest, propertyName) : null);
    }

    public static <T> List<ConfigChangeRecord> allChanges(T source, T dest, List<String> ignoreProperties) throws Exception {
        BeanInfo info = Introspector.getBeanInfo(source.getClass());

        List<String> propertyNames = Arrays.asList(info.getPropertyDescriptors()).stream()
                .map(descriptor -> descriptor.getName())
                .filter(name -> !ignoreProperties.contains(name)).toList();
        List<ConfigChangeRecord> records = new ArrayList<>();
        for (String propertyName : propertyNames) {
            var record = new ConfigChangeRecord(source, dest, propertyName);
            if (!record.newValue.equals(record.oldValue)) {
                records.add(record);
            }
        }
        return records;
    }


}

