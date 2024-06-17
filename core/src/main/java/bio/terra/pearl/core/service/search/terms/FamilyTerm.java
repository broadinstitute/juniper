package bio.terra.pearl.core.service.search.terms;

import bio.terra.pearl.core.dao.participant.FamilyDao;
import bio.terra.pearl.core.model.participant.Family;
import bio.terra.pearl.core.model.search.SearchValueTypeDefinition;
import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.sql.EnrolleeSearchQueryBuilder;
import org.jooq.Condition;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static bio.terra.pearl.core.service.search.terms.SearchValue.SearchValueType.STRING;

/**
 * This term can be used to search for the age of an enrollee. It uses the `birthDate` field from the enrollee's profile
 * and converts it to an integer representing the age in years.
 */
public class FamilyTerm implements SearchTerm {

    private final FamilyDao familyDao;
    private final String field;

    public FamilyTerm(FamilyDao familyDao, String field) {
        this.familyDao = familyDao;

        if (!FIELDS.containsKey(field)) {
            throw new IllegalArgumentException("Invalid field: " + field);
        }

        this.field = field;
    }


    @Override
    public SearchValue extract(EnrolleeSearchContext context) {
        if (Objects.isNull(context.getProfile()) || Objects.isNull(context.getProfile().getBirthDate())) {
            return new SearchValue();
        }

        List<Family> families = this.familyDao.findByEnrolleeId(context.getEnrollee().getId());

        if (families.isEmpty()) {
            return new SearchValue();
        }

        List<SearchValue> values = families
                .stream()
                .map(val -> SearchValue.ofNestedProperty(val, field, FIELDS.get(field).getType()))
                .toList();
        return new SearchValue(values);
    }

    @Override
    public List<EnrolleeSearchQueryBuilder.JoinClause> requiredJoinClauses() {
        return List.of(
                new EnrolleeSearchQueryBuilder.JoinClause("family_enrollee", "family_enrollee", "enrollee.id = family_enrollee.enrollee_id"),
                new EnrolleeSearchQueryBuilder.JoinClause("family", "family", "family.id = family_enrollee.family_id"));
    }

    @Override
    public List<EnrolleeSearchQueryBuilder.SelectClause> requiredSelectClauses() {
        return List.of(
                new EnrolleeSearchQueryBuilder.SelectClause("family", familyDao)
        );
    }

    @Override
    public Optional<Condition> requiredConditions() {
        return Optional.empty();
    }

    @Override
    public String termClause() {
        return "family." + field;
    }

    @Override
    public List<Object> boundObjects() {
        return List.of();
    }

    @Override
    public SearchValueTypeDefinition type() {
        return FIELDS.get(field);
    }

    public static final Map<String, SearchValueTypeDefinition> FIELDS = Map.ofEntries(
            Map.entry("shortcode", SearchValueTypeDefinition.builder().type(STRING).build()));

}
