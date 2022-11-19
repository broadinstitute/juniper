package bio.terra.pearl.core.dao;

import org.apache.commons.lang3.StringUtils;
import org.jdbi.v3.core.Jdbi;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class BaseJdbiDao<T> {
    protected Jdbi jdbi;
    protected List<String> insertFields;
    protected List<String> insertColumns;
    protected String tableName;
    protected Class<T> clazz;

    protected List<String> getExcludedFields() {
        return Arrays.asList("id", "class");
    }

    protected List<String> generateInsertFields() {
        try {
            BeanInfo info = Introspector.getBeanInfo(getClazz());
            List<String> allSimpleProperties = Arrays.asList(info.getPropertyDescriptors()).stream()
                    .filter(descriptor -> !Collection.class.isAssignableFrom(descriptor.getPropertyType()))
                    .filter(descriptor -> !getExcludedFields().contains(descriptor.getName()))
                    .map(descriptor -> descriptor.getName())
                    .collect(Collectors.toList());
            return allSimpleProperties;
        } catch (IntrospectionException e) {
            throw new RuntimeException("Unable to introspect " + getClazz().getName());
        }
    }

    protected List<String> generateInsertColumns(List<String> insertFields) {
        return insertFields.stream().map(field -> toSnakeCase(field))
                .collect(Collectors.toList());
    }

    protected abstract Class<T> getClazz();

    protected String getTableName() {
        return toSnakeCase(getClazz().getSimpleName());
    };

    public BaseJdbiDao(Jdbi jdbi) {
        clazz = getClazz();
        insertFields = generateInsertFields();
        insertColumns = generateInsertColumns(insertFields);
        tableName = getTableName();
        this.jdbi = jdbi;
    }

    public T create(T modelObj) {
        List<String> paramNames = insertFields.stream().map(field -> ":" + field).collect(Collectors.toList());
        return jdbi.withHandle(handle ->
                handle.createUpdate("insert into " + tableName + " (" + StringUtils.join(insertColumns, ",") +") " +
                                "values (" + StringUtils.join(paramNames, ",") + ")")
                        .bindBean(modelObj)
                        .executeAndReturnGeneratedKeys()
                        .mapTo(clazz)
                        .one()
        );
    }

    public T findOne(UUID id) {
        return jdbi.withHandle(handle ->
                handle.createQuery("select * from " + tableName + " where id = :id")
                        .bind("id", id)
                        .mapTo(clazz)
                        .one()
        );
    }

    // from https://stackoverflow.com/questions/10310321/regex-for-converting-camelcase-to-camel-case-in-java
    protected static String toSnakeCase(String camelCased) {
        return camelCased.replaceAll("(.)(\\p{Upper})", "$1_$2").toLowerCase();
    }
}
