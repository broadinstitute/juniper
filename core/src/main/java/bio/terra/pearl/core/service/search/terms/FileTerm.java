package bio.terra.pearl.core.service.search.terms;

import bio.terra.pearl.core.model.search.SearchValueTypeDefinition;
import bio.terra.pearl.core.service.search.sql.EnrolleeSearchQueryBuilder;
import org.jooq.Condition;

import java.util.List;
import java.util.Optional;

import static bio.terra.pearl.core.service.search.terms.SearchValue.SearchValueType.BOOLEAN;

/**
 * Base class for file-related search terms. Provides shared alias generation, the common
 * answer + participant_file join clauses, and the trivial SearchTerm overrides.
 * Subclasses supply the alias prefix, any additional joins, the termClause, and extract().
 *
 * <p>The questionStableId is validated as alphanumeric+underscore so it can be safely
 * embedded in SQL JOIN ON clauses (same approach as AnswerTerm).
 */
public abstract class FileTerm extends SearchTerm {
    protected final String questionStableId;
    private final String aliasPrefix;

    protected FileTerm(String questionStableId, String aliasPrefix) {
        if (!isAlphaNumeric(questionStableId)) {
            throw new IllegalArgumentException("Invalid question stable id: must be alphanumeric and underscore only");
        }
        this.questionStableId = questionStableId;
        this.aliasPrefix = aliasPrefix;
    }

    protected EnrolleeSearchQueryBuilder.JoinClause answerJoinClause() {
        // SAFE: questionStableId validated as alphanumeric+underscore in constructor
        return new EnrolleeSearchQueryBuilder.JoinClause("answer", answerAlias(),
                "enrollee.id = %s.enrollee_id AND %s.question_stable_id = '%s' AND %s.format = 'FILE_UPLOAD'"
                        .formatted(answerAlias(), answerAlias(), questionStableId, answerAlias()));
    }

    protected EnrolleeSearchQueryBuilder.JoinClause participantFileJoinClause() {
        return new EnrolleeSearchQueryBuilder.JoinClause("participant_file", pfAlias(),
                "enrollee.id = %s.enrollee_id AND %s.object_value::jsonb @> json_build_array(json_build_object('fileName', %s.file_name))::jsonb"
                        .formatted(pfAlias(), answerAlias(), pfAlias()));
    }

    @Override
    public List<EnrolleeSearchQueryBuilder.SelectClause> requiredSelectClauses() {
        return List.of();
    }

    @Override
    public Optional<Condition> requiredConditions() {
        return Optional.empty();
    }

    @Override
    public List<Object> boundObjects() {
        return List.of();
    }

    @Override
    public SearchValueTypeDefinition type() {
        return SearchValueTypeDefinition.builder().type(BOOLEAN).build();
    }

    protected String answerAlias() {
        return "a_" + aliasPrefix + "_" + questionStableId;
    }

    protected String pfAlias() {
        return "pf_" + aliasPrefix + "_" + questionStableId;
    }

    protected static boolean isAlphaNumeric(String s) {
        return s != null && s.matches("^[a-zA-Z0-9_]+$");
    }
}
