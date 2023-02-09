package bio.terra.pearl.core.dao;

import bio.terra.pearl.core.model.BaseEntity;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.BeanUtils;

/**
 * Base DAO for models that are not immutable.  Adds support for update and createOrUpdate operations
 */
public abstract class BaseMutableJdbiDao<T extends BaseEntity> extends BaseJdbiDao<T> {
    protected String updateFieldString;

    protected String generateUpdateFieldString(List<String> insertFieldSymbols, List<String> insertColumns) {
        // we never want to update the "createdAt" fields, so remove those
        List<String> usedSymbols = insertFieldSymbols.stream().filter(sym -> !":createdAt".equals(sym)).toList();
        List<String> usedColumns = insertColumns.stream().filter(col -> !"created_at".equals(col)).toList();
        return IntStream
                .range(0, usedSymbols.size())
                .mapToObj(i -> usedColumns.get(i) + " = " + usedSymbols.get(i))
                .collect(Collectors.joining(", "));
    }

    public BaseMutableJdbiDao(Jdbi jdbi) {
        super(jdbi);
        updateFieldString = generateUpdateFieldString(insertFieldSymbols, insertColumns);
    }

    public T createOrUpdate(T matchObj) {
        T existingObj = findOneMatch(matchObj).get();
        if (existingObj == null) {
            return create(matchObj);
        }
        BeanUtils.copyProperties(matchObj, existingObj, new String[] {"id"});
        return update(existingObj);
    }

    /**
     * updates the database to reflect the matchObj.  Throws error if matchObj does not have an id
     * This handles updating the lastUpdatedAt field to the current time
     */
    public T update(T matchObj) {
        if (matchObj.getId() == null) {
            throw new RuntimeException("attempted update on " + clazz + " with no id");
        }
        matchObj.setLastUpdatedAt(Instant.now());
        return jdbi.withHandle(handle ->
                handle.createUpdate("update " + tableName + " set " + updateFieldString +
                                " where id = :id;")
                        .bindBean(matchObj)
                        .executeAndReturnGeneratedKeys()
                        .mapTo(clazz)
                        .one()
        );
    }

    /** updates a single property.  This will also updated the lastUpdatedAt property too */
    public void updateProperty(UUID id, String propertyColumn, Object propertyValue) {
        jdbi.withHandle(handle ->
                handle.createUpdate("update " + tableName + " set " + propertyColumn +
                                " = :propertyValue, last_updated_at = :lastUpdatedAt where id = :id;")
                        .bind("id", id)
                        .bind("lastUpdatedAt", Instant.now())
                        .bind("propertyValue", propertyValue)
                        .execute()
        );
    }
}
