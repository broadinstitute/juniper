package bio.terra.pearl.core.model.publishing;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.apache.commons.beanutils.PropertyUtils;

public record ConfigChangeRecord(String propertyName, Object oldValue, Object newValue) {
    public ConfigChangeRecord(Object source, Object dest, String propertyName) throws Exception {
        this(propertyName,
                dest != null ? PropertyUtils.getProperty(dest, propertyName) : null,
                source != null ? PropertyUtils.getProperty(source, propertyName) : null);
    }

    /**
     * gets a list of change records for each property that has changed between source and dest.  If one of source
     * or dest is null, all properties will be returned.  If both are null, an empty list will be returned
     */
    public static <T> List<ConfigChangeRecord> allChanges(T source, T dest, List<String> ignoreProperties) throws Exception {
        if (source == null && dest == null) {
            return List.of();
        }
        BeanInfo info = Introspector.getBeanInfo(source != null ? source.getClass() : dest.getClass());

        List<String> propertyNames = Arrays.asList(info.getPropertyDescriptors()).stream()
                .map(descriptor -> descriptor.getName())
                .filter(name -> !ignoreProperties.contains(name)).toList();
        List<ConfigChangeRecord> records = new ArrayList<>();
        for (String propertyName : propertyNames) {
            var record = new ConfigChangeRecord(source, dest, propertyName);
            // if the new value is different than the old, add the record
            if (!Objects.equals(record.newValue, record.oldValue)) {
                records.add(record);
            }
        }
        return records;
    }


}

