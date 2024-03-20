package bio.terra.pearl.core.service.search.expressions;

import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpression;
import bio.terra.pearl.core.service.search.sql.EnrolleeSearchQueryBuilder;

import java.util.UUID;

/** represents an empty search/filter rule, which means return everything */
public class DefaultSearchExpression implements EnrolleeSearchExpression {
    public DefaultSearchExpression() {}

    @Override
    public boolean evaluate (EnrolleeSearchContext enrolleeCtx){
        return true;
    }

    @Override
    public EnrolleeSearchQueryBuilder generateQueryBuilder (UUID studyEnvId) {
        return new EnrolleeSearchQueryBuilder(studyEnvId);
    }

}
