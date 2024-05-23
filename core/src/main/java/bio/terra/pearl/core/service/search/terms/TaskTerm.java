package bio.terra.pearl.core.service.search.terms;

import bio.terra.pearl.core.dao.workflow.ParticipantTaskDao;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.sql.EnrolleeSearchQueryBuilder;
import org.jooq.Condition;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static bio.terra.pearl.core.dao.BaseJdbiDao.toSnakeCase;
import static org.jooq.impl.DSL.condition;

/**
 * Allows searching on an enrollee's task status.
 */
public class TaskTerm implements SearchTerm {
    private final String targetStableId;
    private final String field;
    private final ParticipantTaskDao participantTaskDao;

    public TaskTerm(ParticipantTaskDao participantTaskDao, String targetStableId, String field) {
        if (!isAlphaNumeric(targetStableId)) {
            throw new IllegalArgumentException("Invalid stable ids: must be alphanumeric and underscore only");
        }

        if (!FIELDS.containsKey(field)) {
            throw new IllegalArgumentException("Invalid field: " + field);
        }

        this.targetStableId = targetStableId;
        this.field = field;
        this.participantTaskDao = participantTaskDao;
    }

    @Override
    public SearchValue extract(EnrolleeSearchContext context) {
        Optional<ParticipantTask> taskOpt = participantTaskDao.findTaskForActivity(
                context.getEnrollee(),
                context.getEnrollee().getStudyEnvironmentId(),
                this.targetStableId);

        if (field.equals("assigned")) {
            return new SearchValue(taskOpt.isPresent());
        }

        if (taskOpt.isEmpty()) {
            return new SearchValue();
        }
        ParticipantTask task = taskOpt.get();
        return SearchValue.ofNestedProperty(task, field, FIELDS.get(field));
    }

    @Override
    public List<EnrolleeSearchQueryBuilder.JoinClause> requiredJoinClauses() {
        return List.of(new EnrolleeSearchQueryBuilder.JoinClause("participant_task", alias(), "enrollee.id = %s.enrollee_id".formatted(alias())));
    }

    @Override
    public List<EnrolleeSearchQueryBuilder.SelectClause> requiredSelectClauses() {
        return List.of(
                new EnrolleeSearchQueryBuilder.SelectClause(alias(), participantTaskDao)
        );
    }

    @Override
    public Optional<Condition> requiredConditions() {
        return Optional.of(
                condition(
                        alias() + ".target_stable_id = ?",
                        targetStableId));
    }

    @Override
    public String termClause() {
        if (field.equals("assigned"))
            return alias() + ".id IS NOT NULL";
        return alias() + "." + toSnakeCase(field);
    }

    @Override
    public List<Object> boundObjects() {
        return List.of();
    }

    private static boolean isAlphaNumeric(String s) {
        return s.matches("^[a-zA-Z0-9_]*$");
    }

    private String alias() {
        return "task_" + targetStableId;
    }

    public static final Map<String, SearchValue.SearchValueType> FIELDS = Map.ofEntries(
            Map.entry("status", SearchValue.SearchValueType.STRING),
            Map.entry("assigned", SearchValue.SearchValueType.BOOLEAN));
}
