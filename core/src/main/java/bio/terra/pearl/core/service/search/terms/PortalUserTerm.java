package bio.terra.pearl.core.service.search.terms;

import bio.terra.pearl.core.dao.participant.ParticipantUserDao;
import bio.terra.pearl.core.dao.participant.PortalParticipantUserDao;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.search.SearchValueTypeDefinition;
import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.sql.EnrolleeSearchQueryBuilder;
import com.google.api.gax.rpc.UnimplementedException;
import org.jooq.Condition;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static bio.terra.pearl.core.dao.BaseJdbiDao.toSnakeCase;
import static bio.terra.pearl.core.service.search.terms.SearchValue.SearchValueType.DATE;
import static bio.terra.pearl.core.service.search.terms.SearchValue.SearchValueType.INSTANT;

/** a term for the PortalParticipantUser (named PortalUser because the expression term is "portalUser") */
public class PortalUserTerm implements SearchTerm {

    private final String field;
    private final PortalParticipantUserDao portalParticipantUserDao;

    public PortalUserTerm(PortalParticipantUserDao portalParticipantUserDao, String field) {
        if (!FIELDS.containsKey(field)) {
            throw new IllegalArgumentException("Invalid field: " + field);
        }
        this.portalParticipantUserDao = portalParticipantUserDao;
        this.field = field;
    }

    @Override
    public SearchValue extract(EnrolleeSearchContext context) {
        Optional<PortalParticipantUser> ppUser = portalParticipantUserDao.findByProfileId(context.getEnrollee().getProfileId());
        if (ppUser.isEmpty()) {
            return new SearchValue();
        }
        return SearchValue.ofNestedProperty(ppUser, field, FIELDS.get(field).getType());
    }

    @Override
    public List<EnrolleeSearchQueryBuilder.JoinClause> requiredJoinClauses() {
        return List.of(
                new EnrolleeSearchQueryBuilder.JoinClause("portal_participant_user", "portalUser", "portalUser.profile_id = profile.id")
        );
    }

    @Override
    public List<EnrolleeSearchQueryBuilder.SelectClause> requiredSelectClauses() {
        return List.of(
                new EnrolleeSearchQueryBuilder.SelectClause("portalUser", portalParticipantUserDao)
        );
    }

    @Override
    public Optional<Condition> requiredConditions() {
        return Optional.empty();
    }

    @Override
    public String termClause() {
        return "portalUser." + toSnakeCase(field);
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
            Map.entry("createdAt", SearchValueTypeDefinition.builder().type(INSTANT).build()),
            Map.entry("lastLogin", SearchValueTypeDefinition.builder().type(INSTANT).build()));

}
