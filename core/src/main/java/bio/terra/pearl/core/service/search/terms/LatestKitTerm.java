package bio.terra.pearl.core.service.search.terms;

import bio.terra.pearl.core.dao.kit.KitRequestDao;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.kit.KitRequestStatus;
import bio.terra.pearl.core.model.search.SearchValueTypeDefinition;
import bio.terra.pearl.core.model.survey.QuestionChoice;
import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.sql.EnrolleeSearchQueryBuilder;
import org.jooq.Condition;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static bio.terra.pearl.core.service.search.terms.SearchValue.SearchValueType.STRING;
import static org.jooq.impl.DSL.condition;

/**
 * This term fetches information about the latest kit request for an enrollee.
 */
public class LatestKitTerm implements SearchTerm {
    private final String field;
    private final KitRequestDao kitRequestDao;

    public LatestKitTerm(KitRequestDao kitRequestDao, String field) {

        if (!FIELDS.containsKey(field)) {
            throw new IllegalArgumentException("Invalid field: " + field);
        }

        this.kitRequestDao = kitRequestDao;
        this.field = field;
    }

    @Override
    public SearchValue extract(EnrolleeSearchContext context) {
        List<KitRequest> requests = kitRequestDao.findByEnrollee(context.getEnrollee().getId());
        if (requests.isEmpty()) {
            return new SearchValue();
        }
        requests.sort((r1, r2) -> r2.getLastUpdatedAt().compareTo(r1.getLastUpdatedAt()));
        KitRequest latestKit = requests.get(0);

        return SearchValue.ofNestedProperty(latestKit, field, FIELDS.get(field).getType());
    }

    @Override
    public List<EnrolleeSearchQueryBuilder.JoinClause> requiredJoinClauses() {
        return List.of(new EnrolleeSearchQueryBuilder.JoinClause(
                "kit_request",
                "latest_kit",
                "enrollee.id = latest_kit.enrollee_id"));
    }

    @Override
    public List<EnrolleeSearchQueryBuilder.SelectClause> requiredSelectClauses() {
        return List.of(
                new EnrolleeSearchQueryBuilder.SelectClause("latest_kit", kitRequestDao)
        );
    }

    @Override
    public Optional<Condition> requiredConditions() {
        // creates a subquery to ensure that the latest kit request is the only one returned
        // could also be done with and order by/limit, but this fits into the existing search
        // query infra better
        return Optional.of(condition("NOT EXISTS (SELECT other_kit_request.id FROM kit_request other_kit_request WHERE other_kit_request.enrollee_id = enrollee.id AND other_kit_request.last_updated_at > latest_kit.last_updated_at)"));
    }

    @Override
    public String termClause() {
        return "latest_kit."+field;
    }

    @Override
    public List<Object> boundObjects() {
        return List.of();
    }

    public static final Map<String, SearchValueTypeDefinition> FIELDS = Map.ofEntries(
            Map.entry(
                    "status",
                    SearchValueTypeDefinition
                            .builder()
                            .type(STRING)
                            .choices(
                                    Arrays.asList(KitRequestStatus.values())
                                            .stream()
                                            .map(val -> new QuestionChoice(val.name(), val.name()))
                                            .toList()
                            ).build())
    );
}
