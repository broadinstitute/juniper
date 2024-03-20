package bio.terra.pearl.core.service.search.expressions;

import bio.terra.pearl.core.dao.participant.EnrolleeDao;
import bio.terra.pearl.core.dao.participant.ProfileDao;
import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpression;
import bio.terra.pearl.core.service.search.sql.EnrolleeSearchQueryBuilder;

import java.util.UUID;

/** represents an empty search/filter rule, which means return everything */
public class DefaultSearchExpression implements EnrolleeSearchExpression {

    private EnrolleeDao enrolleeDao;
    private ProfileDao profileDao;

    public DefaultSearchExpression(EnrolleeDao enrolleeDao, ProfileDao profileDao) {
        this.enrolleeDao = enrolleeDao;
        this.profileDao = profileDao;
    }

    @Override
    public boolean evaluate (EnrolleeSearchContext enrolleeCtx){
        return true;
    }

    @Override
    public EnrolleeSearchQueryBuilder generateQueryBuilder (UUID studyEnvId) {
        return new EnrolleeSearchQueryBuilder(enrolleeDao, profileDao, studyEnvId);
    }

}
