package bio.terra.pearl.core.service.search.terms;

import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.sql.SQLJoinClause;
import bio.terra.pearl.core.service.search.sql.SQLSelectClause;
import org.apache.commons.beanutils.PropertyUtils;
import org.jooq.Condition;

import java.util.List;
import java.util.Optional;

public class ProfileTermExtractor implements EnrolleeTermExtractor {

    private final String field;

    public ProfileTermExtractor(String field) {
        if (!ACCEPTABLE_FIELDS.contains(field)) {
            throw new IllegalArgumentException("Invalid field: " + field);
        }
        this.field = field;
    }

    @Override
    public Term extract(EnrolleeSearchContext context) {

        // other than birthDate, all fields are strings
        if (field.equals("birthDate")) {
            return new Term(context.getProfile().getBirthDate());
        }

        String strValue = null;
        try {
            Object objValue = PropertyUtils.getNestedProperty(context.getProfile(), field);
            strValue = objValue.toString();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid field: " + field);
        }

        return new Term(strValue);
    }

    @Override
    public List<SQLJoinClause> requiredJoinClauses() {
        return List.of(new SQLJoinClause("profile", "profile", "enrollee.profile_id = profile.id"));
    }

    @Override
    public List<SQLSelectClause> requiredSelectClauses() {
        return List.of(new SQLSelectClause("profile", field));
    }

    @Override
    public Optional<Condition> requiredConditions() {
        return Optional.empty();
    }

    @Override
    public String termClause() {
        return "profile." + field;
    }

    @Override
    public List<Object> boundObjects() {
        return List.of();
    }

    public static final List<String> ACCEPTABLE_FIELDS = List.of(
            "givenName",
            "familyName",
            "contactEmail",
            "phoneNumber",
            "birthDate"
    );

}
