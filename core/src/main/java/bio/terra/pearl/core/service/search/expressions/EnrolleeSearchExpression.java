package bio.terra.pearl.core.service.search.expressions;

import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.sql.SQLSearch;

public interface EnrolleeSearchExpression {
    boolean evaluate(EnrolleeSearchContext enrollee);
    SQLSearch generateSql();
}
