package bio.terra.pearl.core.service.search.sql;

import org.jdbi.v3.core.statement.Query;

import java.util.Objects;

public class SQLWhereFieldTerm implements SQLWhereClause {
    private String alias;
    private String field;

    public SQLWhereFieldTerm(String alias, String value) {
        this.alias = alias;
        this.field = value;
    }

    public SQLWhereFieldTerm(String customField) {
        this.field = customField;
    }

    public String generateSql(SQLContext context) {
        if (Objects.isNull(alias)) {
            return field;
        }
        return String.format("%s.%s", alias, field);
    }

    @Override
    public void bindSqlParams(Query query) {
    }
}
