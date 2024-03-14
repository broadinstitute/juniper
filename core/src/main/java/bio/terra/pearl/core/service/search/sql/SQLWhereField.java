package bio.terra.pearl.core.service.search.sql;

import org.jdbi.v3.core.statement.Query;

public class SQLWhereField implements SQLWhereClause {
    private String alias;
    private String field;

    public SQLWhereField(String alias, String value) {
        this.alias = alias;
        this.field = value;
    }

    public String generateSql(SQLContext context) {
        return String.format("%s.%s", alias, field);
    }

    @Override
    public void bindSqlParams(Query query) {
    }
}
