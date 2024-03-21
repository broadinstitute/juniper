package bio.terra.pearl.core.service.search.terms;

import bio.terra.pearl.core.dao.participant.MailingAddressDao;
import bio.terra.pearl.core.dao.participant.ProfileDao;
import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.sql.EnrolleeSearchQueryBuilder;
import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtils;
import org.jooq.Condition;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static bio.terra.pearl.core.dao.BaseJdbiDao.toSnakeCase;

public class ProfileTerm implements EnrolleeTerm {

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
        SearchValue value;
        try {
            Object objValue = PropertyUtils.getNestedProperty(context.getProfile(), field);

            SearchValue.SearchValueType type = FIELDS.get(field);

            switch (type) {
                case STRING -> value = new SearchValue(objValue.toString());
                case DATE -> value = new SearchValue((LocalDate) objValue);
                case INTEGER -> value = new SearchValue((Integer) objValue);
                case DOUBLE -> value = new SearchValue((Double) objValue);
                case BOOLEAN -> value = new SearchValue((Boolean) objValue);
                default -> throw new IllegalArgumentException("Unsupported field type: " + type);
            }
        } catch (NullPointerException | NestedNullException e) {
            // if the field is null / not provided, we want to return null/empty search value
            return new SearchValue();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid field: " + field);
        }

        return value;
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
        return "profile." + toSnakeCase(field);
    }

    @Override
    public List<Object> boundObjects() {
        return List.of();
    }

    public static final Map<String, SearchValue.SearchValueType> FIELDS = Map.of(
            "givenName", SearchValue.SearchValueType.STRING,
            "familyName", SearchValue.SearchValueType.STRING,
            "contactEmail", SearchValue.SearchValueType.STRING,
            "phoneNumber", SearchValue.SearchValueType.STRING,
            "birthDate", SearchValue.SearchValueType.DATE,
            "mailingAddress.state", SearchValue.SearchValueType.STRING
    );
}
