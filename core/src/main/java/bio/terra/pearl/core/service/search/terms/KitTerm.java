package bio.terra.pearl.core.service.search.terms;

import bio.terra.pearl.core.dao.kit.KitRequestDao;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.sql.EnrolleeSearchQueryBuilder;
import org.jooq.Condition;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * TODO
 */
public class KitTerm implements SearchTerm {
    private final String field;
    private final KitRequestDao kitRequestDao;

    public KitTerm(KitRequestDao kitRequestDao, String field) {
        this.field = field;
        this.kitRequestDao = kitRequestDao;
    }

    @Override
    public SearchValue extract(EnrolleeSearchContext context) {
        List<KitRequest> kits = kitRequestDao.findByEnrollee(context.getEnrollee().getId());
        if (Objects.isNull(kits) || kits.isEmpty()) {
            return new SearchValue();
        }
        return SearchValue.ofNestedProperty(kit, field, FIELDS.get(field));

    }

    @Override
    public List<EnrolleeSearchQueryBuilder.JoinClause> requiredJoinClauses() {
        return List.of(new EnrolleeSearchQueryBuilder.JoinClause("answer", alias(), "enrollee.id = %s.enrollee_id".formatted(alias())));
    }

    @Override
    public List<EnrolleeSearchQueryBuilder.SelectClause> requiredSelectClauses() {
        return List.of(
                new EnrolleeSearchQueryBuilder.SelectClause(alias(), answerDao)
        );
    }

    @Override
    public Optional<Condition> requiredConditions() {
        return Optional.of(
                condition(
                        alias() + ".survey_stable_id = ? AND " + alias() + ".question_stable_id = ?",
                        surveyStableId, questionStableId));
    }

    @Override
    public String termClause() {
        return alias() + ".string_value";
    }

    @Override
    public List<Object> boundObjects() {
        return List.of();
    }

    private static boolean isAlphaNumeric(String s) {
        return s.matches("^[a-zA-Z0-9_]*$");
    }

    private String alias() {
        return "answer_" + questionStableId;
    }
}
