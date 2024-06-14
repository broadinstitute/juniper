package bio.terra.pearl.core.service.search.terms;

import bio.terra.pearl.core.dao.survey.AnswerDao;
import bio.terra.pearl.core.model.search.SearchValueTypeDefinition;
import bio.terra.pearl.core.model.survey.Answer;
import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.sql.EnrolleeSearchQueryBuilder;
import org.jooq.Condition;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.jooq.impl.DSL.condition;

/**
 * This term can be used to search for an answer to a question in a survey. Note that using the evaluate method on
 * this term requires a SQL call to the database per enrollee and as such could be slow for a large list of enrollees.
 */
public class AnswerTerm implements SearchTerm {
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
        if (Objects.isNull(answer)) {
            return new SearchValue();
        }
        // answerType *shouldn't* be null, but we'll handle it just in case by assuming it's a string
        if (Objects.isNull(answer.getAnswerType())) {
            return new SearchValue(answer.getStringValue());
        }
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

    @Override
    public SearchValueTypeDefinition type() {
        // TODO: this should be determined by the survey question definition, but for now
        //       we are assuming all answers are strings
        return SearchValueTypeDefinition.builder().type(SearchValue.SearchValueType.STRING).build();
    }

    private static boolean isAlphaNumeric(String s) {
        return s.matches("^[a-zA-Z0-9_]*$");
    }

    private String alias() {
        return "answer_" + questionStableId;
    }
}
