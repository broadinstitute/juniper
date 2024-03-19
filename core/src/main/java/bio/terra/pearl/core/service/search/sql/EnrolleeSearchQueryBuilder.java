package bio.terra.pearl.core.service.search.sql;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Operator;
import org.jooq.Record;
import org.jooq.SelectJoinStep;
import org.jooq.SelectQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.condition;
import static org.jooq.impl.DSL.field;

public class EnrolleeSearchQueryBuilder {
    private final List<SQLSelectClause> sqlSelectClauseList = new ArrayList<>();
    private final List<SQLJoinClause> sqlJoinClauseList = new ArrayList<>();
    private final UUID studyEnvId;

    private Condition whereConditions;

    public EnrolleeSearchQueryBuilder(UUID studyEnvId) {
        this.studyEnvId = studyEnvId;
    }

    public SelectQuery<Record> toQuery(DSLContext context) {

        SelectJoinStep<Record> selectQuery = context
                .select(field("enrollee.*"))
                .select(sqlSelectClauseList
                        .stream()
                        .map(select -> field(select.generateSql()))
                        .collect(Collectors.toList()))
                .from("enrollee enrollee");

        for (SQLJoinClause join : sqlJoinClauseList) {
            String tableName = Objects.nonNull(join.getAlias())
                    ? join.getTable() + " " + join.getAlias()
                    : join.getTable();

            // default to left join to get as much enrollee data as possible
            selectQuery = selectQuery.leftJoin(tableName).on(join.getOn());
        }

        return selectQuery
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

    public void addCondition(Condition condition, Operator operator) {
        if (Objects.isNull(whereConditions)) {
            whereConditions = condition;
            return;
        }
        whereConditions = condition(operator, whereConditions, condition);
    }

    public void addCondition(Condition condition) {
        addCondition(condition, Operator.AND);
    }

    public EnrolleeSearchQueryBuilder merge(EnrolleeSearchQueryBuilder other, Operator operator) {

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
