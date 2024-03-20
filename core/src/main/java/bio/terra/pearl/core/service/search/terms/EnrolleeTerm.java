package bio.terra.pearl.core.service.search.terms;

import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.sql.EnrolleeSearchQueryBuilder;
import org.jooq.Condition;

import java.util.List;
import java.util.Optional;

public interface EnrolleeTerm {
    /**
     * Extract the term's value from the enrollee.
     */
    SearchValue extract(EnrolleeSearchContext enrollee);

    /**
     * Joins required to extract this term in a SQL search.
     */
    List<EnrolleeSearchQueryBuilder.JoinClause> requiredJoinClauses();

    /**
     * Select clauses required to extract this term in a SQL search.
     */
    List<EnrolleeSearchQueryBuilder.SelectClause> requiredSelectClauses();

    /**
     * Required where conditions - for example, if the term is an answer field, the survey and question stable ids
     * need to be checked before the term can be extracted.
     */
    Optional<Condition> requiredConditions();

    /**
     * The actual term clause to be used in the SQL query. For example, `profile.given_name` or `?` if it needs to be
     * bound and sanitized.
     */
    String termClause();

    /**
     * Bound objects to be used in the SQL query. For example, the value of the term if user inputted.
     */
    List<Object> boundObjects();
}
