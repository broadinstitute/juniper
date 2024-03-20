package bio.terra.pearl.core.service.search.terms;

import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.sql.EnrolleeSearchQueryBuilder;
import org.apache.commons.beanutils.PropertyUtils;
import org.jooq.Condition;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static bio.terra.pearl.core.dao.BaseJdbiDao.toSnakeCase;

public class ProfileTerm implements EnrolleeTerm {

    private final String field;

    public ProfileTerm(String field) {
        if (!ACCEPTABLE_FIELDS.contains(field)) {
            throw new IllegalArgumentException("Invalid field: " + field);
        }
        this.field = field;
    }

    @Override
    public SearchValue extract(EnrolleeSearchContext context) {

        // other than birthDate, all fields are strings
        if (field.equals("birthDate")) {
            return new SearchValue(context.getProfile().getBirthDate());
        }

        String strValue = null;
        try {
            Object objValue = PropertyUtils.getNestedProperty(context.getProfile(), field);
            strValue = objValue.toString();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid field: " + field);
        }

        return new SearchValue(strValue);
    }

    @Override
    public List<EnrolleeSearchQueryBuilder.JoinClause> requiredJoinClauses() {
        List<EnrolleeSearchQueryBuilder.JoinClause> joinClauses = new ArrayList<>();

        joinClauses.add(new EnrolleeSearchQueryBuilder.JoinClause("profile", "profile", "enrollee.profile_id = profile.id"));

        if (field.startsWith("mailingAddress")) {
            joinClauses.add(new EnrolleeSearchQueryBuilder.JoinClause("mailing_address", "mailing_address", "profile.mailing_address_id = mailing_address.id"));
        }

        return joinClauses;
    }

    @Override
    public List<EnrolleeSearchQueryBuilder.SelectClause> requiredSelectClauses() {
        if (field.startsWith("mailingAddress")) {
            return List.of(new EnrolleeSearchQueryBuilder.SelectClause("mailing_address", toSnakeCase(field.substring(field.indexOf(".") + 1))));
        }

        return List.of(new EnrolleeSearchQueryBuilder.SelectClause("profile", toSnakeCase(field)));
    }

    @Override
    public Optional<Condition> requiredConditions() {
        return Optional.empty();
    }

    @Override
    public String termClause() {
        if (field.startsWith("mailingAddress"))
            return "mailing_address." + toSnakeCase(field.substring(field.indexOf(".") + 1));
        return "profile." + toSnakeCase(field);
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
            "birthDate",
            "mailingAddress.state"
    );

}
