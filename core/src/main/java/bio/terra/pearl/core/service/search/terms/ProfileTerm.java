package bio.terra.pearl.core.service.search.terms;

import bio.terra.pearl.core.dao.participant.MailingAddressDao;
import bio.terra.pearl.core.dao.participant.ProfileDao;
import bio.terra.pearl.core.model.search.SearchValueTypeDefinition;
import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.sql.EnrolleeSearchQueryBuilder;
import org.jooq.Condition;

import java.util.*;

import static bio.terra.pearl.core.dao.BaseJdbiDao.toSnakeCase;
import static bio.terra.pearl.core.service.search.terms.SearchValue.SearchValueType.DATE;
import static bio.terra.pearl.core.service.search.terms.SearchValue.SearchValueType.STRING;

/**
 * This term can be used to search for any of the profile or mailing address fields within a search expression.
 */
public class ProfileTerm implements SearchTerm {
    private final String field;
    private final ProfileDao profileDao;
    private final MailingAddressDao mailingAddressDao;

    public ProfileTerm(ProfileDao profileDao, MailingAddressDao mailingAddressDao, String field) {
        if (!FIELDS.containsKey(field)) {
            throw new IllegalArgumentException("Invalid field: " + field);
        }

        this.profileDao = profileDao;
        this.mailingAddressDao = mailingAddressDao;

        this.field = field;
    }

    @Override
    public SearchValue extract(EnrolleeSearchContext context) {
        if (Objects.isNull(context.getProfile())) {
            return new SearchValue();
        }

        if (field.equals("name")) {
            String givenName = Objects.requireNonNullElse(context.getProfile().getGivenName(), "");
            String familyName = Objects.requireNonNullElse(context.getProfile().getFamilyName(), "");

            return new SearchValue((givenName + " " + familyName).trim());
        }

        return SearchValue.ofNestedProperty(context.getProfile(), field, FIELDS.get(field).getType());
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
            return List.of(new EnrolleeSearchQueryBuilder.SelectClause("mailing_address", mailingAddressDao));
        }

        return List.of(new EnrolleeSearchQueryBuilder.SelectClause("profile", profileDao));
    }

    @Override
    public Optional<Condition> requiredConditions() {
        return Optional.empty();
    }

    @Override
    public String termClause() {
        if (field.startsWith("mailingAddress"))
            return "mailing_address." + toSnakeCase(field.substring(field.indexOf(".") + 1));
        if (field.equals("name"))
            return "concat(profile.given_name, ' ', profile.family_name)";
        return "profile." + toSnakeCase(field);
    }

    @Override
    public List<Object> boundObjects() {
        return List.of();
    }

    public static final Map<String, SearchValueTypeDefinition> FIELDS = Map.ofEntries(
            Map.entry("givenName", SearchValueTypeDefinition.ofType(STRING)),
            Map.entry("familyName", SearchValueTypeDefinition.ofType(STRING)),
            Map.entry("name", SearchValueTypeDefinition.ofType(STRING)),
            Map.entry("contactEmail", SearchValueTypeDefinition.ofType(STRING)),
            Map.entry("phoneNumber", SearchValueTypeDefinition.ofType(STRING)),
            Map.entry("birthDate", SearchValueTypeDefinition.ofType(DATE)),
            Map.entry("sexAtBirth", SearchValueTypeDefinition.ofType(STRING)),
            Map.entry("mailingAddress.state", SearchValueTypeDefinition.ofType(STRING)),
            Map.entry("mailingAddress.city", SearchValueTypeDefinition.ofType(STRING)),
            Map.entry("mailingAddress.postalCode", SearchValueTypeDefinition.ofType(STRING)),
            Map.entry("mailingAddress.street1", SearchValueTypeDefinition.ofType(STRING)),
            Map.entry("mailingAddress.street2", SearchValueTypeDefinition.ofType(STRING)),
            Map.entry("mailingAddress.country", SearchValueTypeDefinition.ofType(STRING))
    );
}
