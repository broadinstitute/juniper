package bio.terra.pearl.core.service.search.terms;

import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.sql.EnrolleeSearchQueryBuilder;
import org.jooq.Condition;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Allows searching on basic properties of the enrollee, e.g. "consented"
 */
public class EnrolleeTerm implements SearchTerm {

    private final String field;

    public EnrolleeTerm(String field) {
        if (!FIELDS.containsKey(field)) {
            throw new IllegalArgumentException("Invalid field: " + field);
        }

        this.field = field;
    }


    @Override
    public SearchValue extract(EnrolleeSearchContext context) {
        return SearchValue.ofNestedProperty(context.getEnrollee(), field, FIELDS.get(field));
    }

    @Override
    public List<EnrolleeSearchQueryBuilder.JoinClause> requiredJoinClauses() {
        return List.of();
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
    public String termClause() {
        return "enrollee." + field;
    }

    @Override
    public List<Object> boundObjects() {
        return List.of();
    }

    public static final Map<String, SearchValue.SearchValueType> FIELDS = Map.ofEntries(
            Map.entry("shortcode", SearchValue.SearchValueType.STRING),
            Map.entry("subject", SearchValue.SearchValueType.BOOLEAN),
            Map.entry("consented", SearchValue.SearchValueType.BOOLEAN));

}
