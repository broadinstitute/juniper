package bio.terra.pearl.core.service.search.terms;

import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.sql.EnrolleeSearchQueryBuilder;
import org.jooq.Condition;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;

public class AgeTerm implements EnrolleeTerm {

    @Override
    public SearchValue extract(EnrolleeSearchContext context) {
        Period period = Period.between(LocalDate.now(), context.getProfile().getBirthDate());
        return new SearchValue(
                period.getYears()
        );
    }

    @Override
    public List<EnrolleeSearchQueryBuilder.JoinClause> requiredJoinClauses() {
        return List.of(new EnrolleeSearchQueryBuilder.JoinClause("profile", "profile", "enrollee.profile_id = profile.id "));
    }

    @Override
    public List<EnrolleeSearchQueryBuilder.SelectClause> requiredSelectClauses() {
        return List.of(
                new EnrolleeSearchQueryBuilder.SelectClause("profile", "birth_date")
        );
    }

    @Override
    public Optional<Condition> requiredConditions() {
        return Optional.empty();
    }

    @Override
    public String termClause() {
        return "EXTRACT('YEAR' FROM AGE(profile.birth_date))";
    }

    @Override
    public List<Object> boundObjects() {
        return List.of();
    }

}
