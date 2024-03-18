package bio.terra.pearl.core.service.search.terms;

import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.sql.SQLJoinClause;
import bio.terra.pearl.core.service.search.sql.SQLSelectClause;
import bio.terra.pearl.core.service.search.sql.SQLWhereClause;
import bio.terra.pearl.core.service.search.sql.SQLWhereFieldTerm;
import bio.terra.pearl.core.service.survey.AnswerService;
import lombok.Getter;

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
        return answerService.getAnswer(context.getEnrollee().getId(), surveyStableId)
    }

    @Override
    public List<SQLJoinClause> requiredJoinClauses() {
        return List.of(new SQLJoinClause("profile", "profile", "enrollee.profile_id = profile.id"));
    }

    @Override
    public List<SQLSelectClause> requiredSelectClauses() {
        return List.of(new SQLSelectClause("answer", field.getValue()));
    }

    @Override
    public SQLWhereClause termClause() {
        return new SQLWhereFieldTerm("answer", "boolean_value");
    }

}
