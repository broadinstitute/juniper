package bio.terra.pearl.core.service.search.sql;

import lombok.Getter;

@Getter
public class SQLJoinClause {
    private JoinType joinType = JoinType.INNER;
    private final String alias;
    private final String table;
    private final String on;

    public SQLJoinClause(String table, String alias, String on) {
        this.alias = alias;
        this.table = table;
        this.on = on;
    }

    public enum JoinType {
        INNER,
        LEFT,
        RIGHT
    }
}
