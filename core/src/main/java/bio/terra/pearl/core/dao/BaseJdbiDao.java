package bio.terra.pearl.core.dao;

import bio.terra.pearl.core.model.BaseEntity;
import org.apache.commons.lang3.StringUtils;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.springframework.beans.BeanUtils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class BaseJdbiDao<T extends BaseEntity> {
    protected Jdbi jdbi;
    protected List<String> insertFields;
    protected List<String> insertFieldSymbols;
    protected List<String> insertColumns;
    protected String updateFieldString;
    protected String tableName;
    protected Class<T> clazz;

    protected abstract Class<T> getClazz();

    protected RowMapper<T> getRowMapper() {
        return BeanMapper.of(getClazz());
    }

    protected List<String> getExcludedFields() {
        return Arrays.asList("id", "class");
    }

    protected boolean isInsertableFieldType(Class fieldType) {
        return Enum.class.isAssignableFrom(fieldType) ||
                Arrays.asList(String.class, Boolean.class, Instant.class).contains(fieldType);
    }

    protected List<String> generateInsertFields() {
        try {
            BeanInfo info = Introspector.getBeanInfo(getClazz());
            List<String> allSimpleProperties = Arrays.asList(info.getPropertyDescriptors()).stream()
                    .filter(descriptor -> isInsertableFieldType(descriptor.getPropertyType()))
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

    protected String generateUpdateFieldString(List<String> insertFieldSymbols, List<String> insertColumns) {
        return IntStream
                .range(0, insertFieldSymbols.size())
                .mapToObj(i -> insertColumns.get(i) + " = " + insertFieldSymbols.get(i))
                .collect(Collectors.joining(", "));
    }

    protected String getTableName() {
        return toSnakeCase(getClazz().getSimpleName());
    };

    public BaseJdbiDao(Jdbi jdbi) {
        clazz = getClazz();
        insertFields = generateInsertFields();
        insertFieldSymbols = insertFields.stream().map(field -> ":" + field).collect(Collectors.toList());
        insertColumns = generateInsertColumns(insertFields);
        updateFieldString = generateUpdateFieldString(insertFieldSymbols, insertColumns);
        tableName = getTableName();
        jdbi.registerRowMapper(clazz, getRowMapper());
        this.jdbi = jdbi;
    }

    public T create(T modelObj) {
        return jdbi.withHandle(handle ->
                handle.createUpdate("insert into " + tableName + " (" + StringUtils.join(insertColumns, " ,") +") " +
                                "values (" + StringUtils.join(insertFieldSymbols, ", ") + ");")
                        .bindBean(modelObj)
                        .executeAndReturnGeneratedKeys()
                        .mapTo(clazz)
                        .one()
        );
    }

    /** basic get-by-id */
    public T findOne(UUID id) {
        return jdbi.withHandle(handle ->
                handle.createQuery("select * from " + tableName + " where id = :id;")
                        .bind("id", id)
                        .mapTo(clazz)
                        .one()
        );
    }

    /** defaults to matching on id if provided.  If the implementing DAO supplies a getNaturalKeyMatchQuery()
     * then that will be used to find a match where no id is present */
    public T findOneMatch(T matchObj) {
        if (matchObj.getId() != null) {
            return findOne(matchObj.getId());
        }
        if (getNaturalKeyMatchQuery() != null) {
            return findByNaturalKey(matchObj);
        }
        return null;
    }

    public T createOrUpdate(T matchObj) {
        T existingObj = findOneMatch(matchObj);
        if (existingObj == null) {
            return create(matchObj);
        }
        BeanUtils.copyProperties(matchObj, existingObj, new String[] {"id"});
        return update(existingObj);
    }

    public T update(T matchObj) {
        if (matchObj.getId() == null) {
            throw new RuntimeException("attempted update on " + clazz + " with no id");
        }
        return jdbi.withHandle(handle ->
                handle.createUpdate("update " + tableName + " set " + updateFieldString +
                                " where id = :id;")
                        .bindBean(matchObj)
                        .executeAndReturnGeneratedKeys()
                        .mapTo(clazz)
                        .one()
        );
    }

    public T findByNaturalKey(T matchObj) {
        return jdbi.withHandle(handle ->
                handle.createQuery(getNaturalKeyMatchQuery())
                        .bindBean(matchObj)
                        .mapTo(clazz)
                        .one()
        );
    }

    protected String getNaturalKeyMatchQuery() {
        return null;
    }

    // from https://stackoverflow.com/questions/10310321/regex-for-converting-camelcase-to-camel-case-in-java
    protected static String toSnakeCase(String camelCased) {
        return camelCased.replaceAll("(.)(\\p{Upper})", "$1_$2").toLowerCase();
    }
}
