package bio.terra.pearl.core.service.search.terms;

import bio.terra.pearl.core.dao.participant.ParticipantUserDao;
import bio.terra.pearl.core.model.search.SearchValueTypeDefinition;
import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.sql.EnrolleeSearchQueryBuilder;
import org.jooq.Condition;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static bio.terra.pearl.core.dao.BaseJdbiDao.toSnakeCase;
import static bio.terra.pearl.core.service.search.terms.SearchValue.SearchValueType.DATE;

/**
 * Allows searching on basic user properties, e.g. "lastLogin"
 */
public class UserTerm implements SearchTerm {

    private final String field;
    private final ParticipantUserDao participantUserDao;

    public UserTerm(ParticipantUserDao participantUserDao, String field) {
        if (!FIELDS.containsKey(field)) {
            throw new IllegalArgumentException("Invalid field: " + field);
        }

        this.participantUserDao = participantUserDao;
        this.field = field;
    }

    @Override
    public SearchValue extract(EnrolleeSearchContext context) {
        return SearchValue.ofNestedProperty(context.getEnrollee(), field, FIELDS.get(field).getType());
    }

    @Override
    public List<EnrolleeSearchQueryBuilder.JoinClause> requiredJoinClauses() {
        return List.of(
                new EnrolleeSearchQueryBuilder.JoinClause("participant_user", "participant_user", "participant_user.id = enrollee.participant_user_id")
        );
    }

    @Override
    public List<EnrolleeSearchQueryBuilder.SelectClause> requiredSelectClauses() {
        return List.of(
                new EnrolleeSearchQueryBuilder.SelectClause("participant_user", participantUserDao)
        );
    }

    @Override
    public Optional<Condition> requiredConditions() {
        return Optional.empty();
    }

    @Override
    public String termClause() {
        return "participant_user." + toSnakeCase(field);
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
            Map.entry("lastLogin", SearchValueTypeDefinition.builder().type(DATE).build()));

}
