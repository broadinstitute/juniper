package bio.terra.pearl.core.service.search.sql;

import bio.terra.pearl.core.dao.BaseJdbiDao;
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

public class EnrolleeSearchQueryBuilder {
    private final List<SelectClause> selectClauseList = new ArrayList<>();
    private final List<JoinClause> joinClauseList = new ArrayList<>();
    private final UUID studyEnvId;

    private Condition whereConditions;

    public EnrolleeSearchQueryBuilder(UUID studyEnvId) {
        this.studyEnvId = studyEnvId;
    }

    public SelectQuery<Record> toQuery(DSLContext context) {
        SelectJoinStep<Record> selectQuery = context
                .select(field("enrollee.id as enrollee_id"))
                .select(field("enrollee.profile_id as enrollee_profile_id"))
                .select(field("enrollee.created_at as enrollee_created_at"))
                .select(field("enrollee.last_updated_at as enrollee_last_updated_at"))
                .select(field("enrollee.participant_user_id as enrollee_participant_user_id"))
                .select(field("enrollee.study_environment_id as enrollee_study_environment_id"))
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

    public void addSelectClause(SelectClause selectClause) {
        if (selectClauseList
                .stream()
                .anyMatch(
                        existing -> existing.getAlias().equals(selectClause.getAlias())))
            return;
        selectClauseList.add(selectClause);
    }

    public void addJoinClause(JoinClause joinClause) {
        if (joinClauseList
                .stream()
                .anyMatch(
                        existing -> existing.getAlias().equals(joinClause.getAlias())
                                && existing.getTable().equals(joinClause.getTable())))
            return;
        joinClauseList.add(joinClause);
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

        for (SelectClause selectClause : other.selectClauseList) {
            addSelectClause(selectClause);
        }

        for (JoinClause joinClause : other.joinClauseList) {
            addJoinClause(joinClause);
        }

        whereConditions = condition(operator, other.whereConditions, this.whereConditions);
        return this;
    }

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
