package bio.terra.pearl.core.service.search.expressions;

import bio.terra.pearl.core.dao.participant.EnrolleeDao;
import bio.terra.pearl.core.dao.participant.ProfileDao;
import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpression;
import bio.terra.pearl.core.service.search.sql.EnrolleeSearchQueryBuilder;
import bio.terra.pearl.core.service.search.terms.SearchTerm;

import java.util.UUID;

/**
 * Allows you to include a term (and related data) in the search result without
 * doing any filtering on the term. For example, `include({family.shortcode})`
 * would load family data without filtering on the family shortcode.
 */
public class IncludeExpression implements EnrolleeSearchExpression {
    private final SearchTerm inner;
    private final EnrolleeDao enrolleeDao;
    private final ProfileDao profileDao;

    public IncludeExpression(ProfileDao profileDao,
                             EnrolleeDao enrolleeDao,
                             SearchTerm inner) {
        this.profileDao = profileDao;
        this.enrolleeDao = enrolleeDao;
        this.inner = inner;
    }

    @Override
    public boolean evaluate(EnrolleeSearchContext enrolleeCtx) {
        return true;
    }

    @Override
    public EnrolleeSearchQueryBuilder generateQueryBuilder(UUID studyEnvId) {
        EnrolleeSearchQueryBuilder enrolleeSearchQueryBuilder = new EnrolleeSearchQueryBuilder(enrolleeDao, profileDao, studyEnvId);

        // Add the join, select, and condition clauses from the inner expression
        // to the query builder.
        inner.requiredJoinClauses().forEach(enrolleeSearchQueryBuilder::addJoinClause);
        inner.requiredSelectClauses().forEach(enrolleeSearchQueryBuilder::addSelectClause);
        inner.requiredConditions().ifPresent(enrolleeSearchQueryBuilder::addCondition);

        return enrolleeSearchQueryBuilder;
    }

}
