package bio.terra.pearl.core.service.search.terms;

import bio.terra.pearl.core.model.survey.Answer;
import bio.terra.pearl.core.service.search.BooleanOperator;
import bio.terra.pearl.core.service.search.ComparisonOperator;
import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.sql.SQLJoinClause;
import bio.terra.pearl.core.service.search.sql.SQLSelectClause;
import bio.terra.pearl.core.service.search.sql.SQLWhereBooleanExpression;
import bio.terra.pearl.core.service.search.sql.SQLWhereClause;
import bio.terra.pearl.core.service.search.sql.SQLWhereComparisonExpression;
import bio.terra.pearl.core.service.search.sql.SQLWhereFieldTerm;
import bio.terra.pearl.core.service.search.sql.SQLWhereValueTerm;
import bio.terra.pearl.core.service.survey.AnswerService;

import java.util.List;

public class AnswerTermExtractor implements EnrolleeTermExtractor {

    private final String questionStableId;
    private final String surveyStableId;
    private final AnswerService answerService;

    public AnswerTermExtractor(AnswerService answerService, String surveyStableId, String questionStableId) {
        this.questionStableId = questionStableId;
        this.surveyStableId = surveyStableId;
        this.answerService = answerService;
    }

    @Override
    public Term extract(EnrolleeSearchContext context) {
        Answer answer = answerService.findForEnrolleeByQuestion(context.getEnrollee().getId(), surveyStableId, questionStableId);
        return switch (answer.getAnswerType()) {
            case STRING -> new Term(answer.getStringValue());
            case NUMBER -> new Term(answer.getNumberValue());
            case BOOLEAN -> new Term(answer.getBooleanValue());
            case OBJECT -> new Term(answer.getObjectValue());
            default -> throw new IllegalArgumentException("Unsupported answer type: " + answer.getAnswerType());
        };
    }

    @Override
    public List<SQLJoinClause> requiredJoinClauses() {
        return List.of(new SQLJoinClause("answer", questionStableId, "enrollee.id = %s.enrollee_id".formatted(questionStableId)));
    }

    @Override
    public List<SQLSelectClause> requiredSelectClauses() {
        return List.of(
                new SQLSelectClause(questionStableId, "string_value"),
                new SQLSelectClause(questionStableId, "question_stable_id"),
                new SQLSelectClause(questionStableId, "survey_stable_id")
        );
    }

    @Override
    public SQLWhereClause requiredWhereClause() {
        return new SQLWhereBooleanExpression(
                new SQLWhereComparisonExpression(
                        new SQLWhereFieldTerm(questionStableId, "survey_stable_id"),
                        new SQLWhereValueTerm(this.surveyStableId),
                        ComparisonOperator.EQUALS),
                new SQLWhereComparisonExpression(
                        new SQLWhereFieldTerm(questionStableId, "question_stable_id"),
                        new SQLWhereValueTerm(this.questionStableId),
                        ComparisonOperator.EQUALS),
                BooleanOperator.AND
        );
    }

    @Override
    public SQLWhereClause termClause() {
        return new SQLWhereFieldTerm(questionStableId, "string_value");
    }

}
