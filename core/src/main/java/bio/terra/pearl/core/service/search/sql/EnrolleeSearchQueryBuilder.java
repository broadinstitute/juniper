package bio.terra.pearl.core.service.search.sql;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.dao.participant.EnrolleeDao;
import bio.terra.pearl.core.dao.participant.ProfileDao;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
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

/**
 * A builder for constructing a jOOQ SQL query to search for enrollees via an
 * {@link bio.terra.pearl.core.service.search.EnrolleeSearchExpression}. This should not be used directly, but rather
 * the expression should be passed to the {@link bio.terra.pearl.core.dao.search.EnrolleeSearchExpressionDao}.
 * where the query building will occur.
 */
public class EnrolleeSearchQueryBuilder {
    private final List<SelectClause> selectClauseList = new ArrayList<>();
    private final List<JoinClause> joinClauseList = new ArrayList<>();
    private final UUID studyEnvId;

    private Condition whereConditions;

    public EnrolleeSearchQueryBuilder(EnrolleeDao enrolleeDao, ProfileDao profileDao, UUID studyEnvId) {
        this.studyEnvId = studyEnvId;

        this.selectClauseList.add(new SelectClause("enrollee", enrolleeDao));
        this.selectClauseList.add(new SelectClause("profile", profileDao));
        this.joinClauseList.add(new JoinClause("profile", "profile", "enrollee.profile_id = profile.id"));
    }

    /**
     * Converts the builder to a jOOQ query.
     */
    public SelectQuery<Record> toQuery(DSLContext context) {
        SelectJoinStep<Record> selectQuery = context
                .select(selectClauseList
                        .stream()
                        .map(select -> field(select.generateSql()))
                        .collect(Collectors.toList()))
                .from("enrollee enrollee");

        for (JoinClause join : joinClauseList) {
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

    /**
     * Adds a select clause to the query. If a select already exists with the same alias, it will not be added again.
     */
    public void addSelectClause(SelectClause selectClause) {
        if (selectClauseList
                .stream()
                .anyMatch(
                        existing -> existing.getAlias().equals(selectClause.getAlias())))
            return;
        selectClauseList.add(selectClause);
    }

    /**
     * Adds a join clause to the query. If a join already exists with the same alias and table, it will not be added again.
     */
    public void addJoinClause(JoinClause joinClause) {
        if (joinClauseList
                .stream()
                .anyMatch(
                        existing -> existing.getAlias().equals(joinClause.getAlias())
                                && existing.getTable().equals(joinClause.getTable())))
            return;
        joinClauseList.add(joinClause);
    }

    /**
     * Adds a condition to the query. If a condition already exists, it will be combined with the new condition using the
     * provided operator.
     */
    public void addCondition(Condition condition, Operator operator) {
        if (Objects.isNull(whereConditions)) {
            whereConditions = condition;
            return;
        }
        whereConditions = condition(operator, whereConditions, condition);
    }

    /**
     * Adds a condition to the query. If a condition already exists, it will be combined with the new condition with an
     * AND.
     */
    public void addCondition(Condition condition) {
        addCondition(condition, Operator.AND);
    }

    /**
     * Merges another query builder into this one. The other query builder's select and join clauses will be added to
     * this one, and the where conditions will be combined with the provided operator.
     */
    public EnrolleeSearchQueryBuilder merge(EnrolleeSearchQueryBuilder other, Operator operator) {

        for (SelectClause selectClause : other.selectClauseList) {
            addSelectClause(selectClause);
        }

        for (JoinClause joinClause : other.joinClauseList) {
            addJoinClause(joinClause);
        }

        whereConditions = condition(operator, other.whereConditions, this.whereConditions);
        return this;
    }

    /**
     * A clause to join a table to the query.
     */
    @Getter
    public static class JoinClause {
        private final String alias;
        private final String table;
        private final String on;

        public JoinClause(String table, String alias, String on) {
            this.alias = alias;
            this.table = table;
            this.on = on;
        }

    }

    /**
     * A clause to select columns from a table. Uses a {@link BaseJdbiDao} to get all columns from the table.
     */
    @Getter
    public static class SelectClause {
        private String alias;
        private BaseJdbiDao dao;

        public SelectClause(String alias, BaseJdbiDao dao) {
            this.alias = alias;
            this.dao = dao;
        }

        public String generateSql() {
            List<String> columns = dao.getGetQueryColumns();
            List<String> columnsWithPrefix = columns.stream().map(
                    column -> alias + "." + column + " as " + alias + "_" + column
            ).toList();
            return StringUtils.join(columnsWithPrefix, ", ");
        }
    }
}
