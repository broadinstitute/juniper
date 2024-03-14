package bio.terra.pearl.core.service.search.sql;

import lombok.Getter;

@Getter
public class SQLJoinClause {
    private JoinType joinType = JoinType.INNER;
    private final String alias;
    private final String table;
    private final String on;

    public SQLJoinClause(String alias, String table, String on) {
        this.alias = alias;
        this.table = table;
        this.on = on;
    }

    public String generateSql() {
        return String.format("%s JOIN %s %s ON %s", joinType, table, alias, on);
    }

    public enum JoinType {
        INNER,
        LEFT,
        RIGHT
    }
}
