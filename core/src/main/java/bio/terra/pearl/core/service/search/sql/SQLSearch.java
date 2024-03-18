package bio.terra.pearl.core.service.search.sql;

import lombok.Getter;
import lombok.Setter;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Operator;
import org.jooq.Record;
import org.jooq.SelectQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.condition;
import static org.jooq.impl.DSL.field;

public class SQLSearch {
    private final List<SQLSelectClause> sqlSelectClauseList = new ArrayList<>();
    private final List<SQLJoinClause> sqlJoinClauseList = new ArrayList<>();

    private final UUID studyEnvId;

    /**
     * Recursive tree of SQLWhereClause objects
     */
    @Getter
    @Setter
    private Condition whereConditions;

    public SQLSearch(UUID studyEnvId) {
        this.studyEnvId = studyEnvId;
    }

    public SelectQuery<Record> toQuery(DSLContext context) {

        return context.select(sqlSelectClauseList
                        .stream()
                        .map(select -> field(select.generateSql()))
                        .collect(Collectors.toList()))
                .from("enrollee enrollee")
                // add joins here
                .where(
                        whereConditions,
                        condition("enrollee.study_environment_id = ?", studyEnvId)
                )
                .getQuery();
    }

    public void addSelectClause(SQLSelectClause selectClause) {
        if (sqlSelectClauseList
                .stream()
                .anyMatch(
                        existing -> existing.getAlias().equals(selectClause.getAlias())
                                && existing.getField().equals(selectClause.getField())))
            return;
        sqlSelectClauseList.add(selectClause);
    }

    public void addJoinClause(SQLJoinClause joinClause) {
        if (sqlJoinClauseList
                .stream()
                .anyMatch(
                        existing -> existing.getAlias().equals(joinClause.getAlias())
                                && existing.getTable().equals(joinClause.getTable())))
            return;
        sqlJoinClauseList.add(joinClause);
    }

    public SQLSearch merge(SQLSearch other, Operator operator) {

        for (SQLSelectClause selectClause : other.sqlSelectClauseList) {
            addSelectClause(selectClause);
        }

        for (SQLJoinClause joinClause : other.sqlJoinClauseList) {
            addJoinClause(joinClause);
        }

        whereConditions = condition(operator, other.whereConditions, this.whereConditions);
        return this;
    }
}
