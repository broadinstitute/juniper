package bio.terra.pearl.core.service.search.sql;

import lombok.Getter;

@Getter
public class SQLSelectClause {
    private String alias;
    private String field;

    public SQLSelectClause(String alias, String value) {
        this.alias = alias;
        this.field = value;
    }

    public String generateSql() {
        return String.format("%s.%s", alias, field);
    }
}
