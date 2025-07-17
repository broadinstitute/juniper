package bio.terra.pearl.core.service.search.expressions;

import bio.terra.pearl.core.dao.participant.EnrolleeDao;
import bio.terra.pearl.core.dao.participant.ProfileDao;
import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpression;
import bio.terra.pearl.core.service.search.sql.EnrolleeSearchQueryBuilder;
import bio.terra.pearl.core.service.search.terms.SearchTerm;
import bio.terra.pearl.core.service.search.terms.SearchValue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.jooq.impl.DSL.condition;

public class UnaryExpression implements EnrolleeSearchExpression {
    private final SearchTerm termExtractor;
    private final EnrolleeDao enrolleeDao;
    private final ProfileDao profileDao;
    private final UnarySearchOperator operator;

    public UnaryExpression(EnrolleeDao enrolleeDao, ProfileDao profileDao, SearchTerm termExtractor, UnarySearchOperator operator) {
        this.operator = operator;
        this.enrolleeDao = enrolleeDao;
        this.profileDao = profileDao;
        this.termExtractor = termExtractor;
    }

    @Override
    public boolean evaluate(EnrolleeSearchContext enrollee) {
        SearchValue val = termExtractor.extract(enrollee);

        return switch (this.operator) {
            case IS_NULL -> val.getSearchValueType().equals(SearchValue.SearchValueType.NULL);
            case IS_NOT_NULL -> !val.getSearchValueType().equals(SearchValue.SearchValueType.NULL);
        };
    }

    @Override
    public EnrolleeSearchQueryBuilder generateQueryBuilder(UUID studyEnvId) {
        EnrolleeSearchQueryBuilder enrolleeSearchQueryBuilder = new EnrolleeSearchQueryBuilder(enrolleeDao, profileDao, studyEnvId);

        termExtractor.requiredJoinClauses().forEach(enrolleeSearchQueryBuilder::addJoinClause);
        termExtractor.requiredSelectClauses().forEach(enrolleeSearchQueryBuilder::addSelectClause);

        List<Object> boundObjects = new ArrayList<>(termExtractor.boundObjects());

        enrolleeSearchQueryBuilder.addCondition(condition(
                "%s %s".formatted(termExtractor.termClause(), this.operator.getOperator()), boundObjects.toArray()
        ));

        return enrolleeSearchQueryBuilder;
    }
}
