package bio.terra.pearl.core.service.search.terms;

import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.sql.SQLJoinClause;
import bio.terra.pearl.core.service.search.sql.SQLSelectClause;
import org.jooq.Condition;

import java.util.List;
import java.util.Optional;

public interface EnrolleeTermExtractor {
    Term extract(EnrolleeSearchContext enrollee);

    List<SQLJoinClause> requiredJoinClauses();
    List<SQLSelectClause> requiredSelectClauses();

    Optional<Condition> requiredConditions();

    // Clause which is either a SQL field (e.g., profile.givenName)
    // or a bound constant from user input (e.g., "John")
    String termClause();

    List<Object> boundObjects();
}
