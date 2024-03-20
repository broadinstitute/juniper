package bio.terra.pearl.core.service.search.terms;

import bio.terra.pearl.core.dao.survey.AnswerDao;
import bio.terra.pearl.core.model.survey.Answer;
import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.sql.EnrolleeSearchQueryBuilder;
import org.jooq.Condition;

import java.util.List;
import java.util.Optional;

import static org.jooq.impl.DSL.condition;

public class AnswerTerm implements EnrolleeTerm {

    private final String questionStableId;
    private final String surveyStableId;
    private final AnswerDao answerDao;

    public AnswerTerm(AnswerDao answerDao, String surveyStableId, String questionStableId) {
        if (!isAlphaNumeric(questionStableId) || !isAlphaNumeric(surveyStableId)) {
            throw new IllegalArgumentException("Invalid stable ids: must be alphanumeric and underscore only");
        }

        this.questionStableId = questionStableId;
        this.surveyStableId = surveyStableId;
        this.answerDao = answerDao;
    }

    @Override
    public SearchValue extract(EnrolleeSearchContext context) {
        Answer answer = answerDao.findForEnrolleeByQuestion(context.getEnrollee().getId(), surveyStableId, questionStableId);
        return switch (answer.getAnswerType()) {
            case STRING -> new SearchValue(answer.getStringValue());
            case NUMBER -> new SearchValue(answer.getNumberValue());
            case BOOLEAN -> new SearchValue(answer.getBooleanValue());
            case OBJECT -> new SearchValue(answer.getObjectValue());
            default -> throw new IllegalArgumentException("Unsupported answer type: " + answer.getAnswerType());
        };
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
