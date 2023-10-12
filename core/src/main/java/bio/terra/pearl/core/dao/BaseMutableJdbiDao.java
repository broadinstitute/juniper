package bio.terra.pearl.core.dao;

import bio.terra.pearl.core.model.BaseEntity;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.jdbi.v3.core.statement.Slf4JSqlLogger;
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

    protected String getUpsertQuerySql(String onConflictField) {
        // This query does both inserts and updates. For the inserts, we need to set the createdAt field.
        String insertColumnsString = String.join(", ", insertColumns);
        String insertFieldSymbolsString = String.join(", ", insertFieldSymbols);

        // For the updates, we need to exclude the createdAt field to preserve the original value
        List<String> updateColumns = insertColumns.stream().filter(col -> !"created_at".equals(col)).toList();
        List<String> excludedColumns = updateColumns.stream().map(column -> column + " = excluded." + column).toList();
        String excludedColumnsString = String.join(", ", excludedColumns);

        return "insert into " + tableName + " (" + insertColumnsString + ") "
                + "values (" + insertFieldSymbolsString + ") "
                + "on conflict (" + onConflictField + ") do update set "
                + excludedColumnsString;
    }

    public void bulkUpsert(List<T> modelObjs, String onConflictField) {
        if (modelObjs.isEmpty()) {
            return;
        }
        int[] result = jdbi.withHandle(handle -> {
            PreparedBatch batch = handle.prepareBatch(getUpsertQuerySql(onConflictField));
            for (T obj : modelObjs) {
                if (obj.getId() != null) {
                    throw new IllegalArgumentException("object passed to bulk upsert already has id: " + obj.getId());
                }
                batch.bindBean(obj).add();
            }
            return batch.execute();
        });
        if (result.length != modelObjs.size() || Arrays.stream(result).anyMatch(rowsUpdated -> rowsUpdated != 1)) {
            throw new IllegalStateException("bulk upsert failed for at least one row");
        }
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

    /** updates a single property.  This will also update the lastUpdatedAt property too */
    protected void updateProperty(UUID id, String propertyColumn, Object propertyValue) {
        updateProperty(id, propertyColumn, propertyValue, tableName, jdbi);
    }

    /**
     * static method for use in DAOs that do not extend this class.  for example, if only one property
     * of an object is intended to be mutable, that DAO can implement an explicit method for setting that property
     * using this method.
     *
     * This method sets the given property and also updates lastUpdatedAt to now()
     */
    public static void updateProperty(UUID id, String propertyColumn, Object propertyValue, String tableName, Jdbi jdbi) {
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
