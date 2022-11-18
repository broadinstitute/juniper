package bio.terra.pearl.core.dao;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.CaseUtils;
import org.jdbi.v3.core.Jdbi;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class BaseJdbiDao<T> {
    protected Jdbi jdbi;
    protected List<String> insertFields;
    protected List<String> insertColumns;
    protected String tableName;
    protected Class<T> clazz;

    public abstract List<String> getInsertColumns();
    public abstract Class<T> getClazz();

    public abstract String getTableName();

    public BaseJdbiDao(Jdbi jdbi) {
        this.insertColumns = getInsertColumns();
        this.insertFields = insertColumns.stream().map(column ->
                ":" + CaseUtils.toCamelCase(column, false, new char[]{'_'}))
                .collect(Collectors.toList());
        this.tableName = getTableName();
        this.clazz = getClazz();
        this.jdbi = jdbi;
    }

    public T create(T modelObj) {
        return jdbi.withHandle(handle ->
                handle.createUpdate("insert into " + tableName + " (" + StringUtils.join(insertColumns, ",") +") " +
                                "values (" + StringUtils.join(insertFields, ",") + ")")
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
}
