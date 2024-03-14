package bio.terra.pearl.core.service.search.sql;

import org.jdbi.v3.core.statement.Query;

public interface SQLWhereClause {
    String generateSql(SQLContext context);
    void bindSqlParams(Query query);
}
