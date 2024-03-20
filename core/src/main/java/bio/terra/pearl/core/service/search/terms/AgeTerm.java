package bio.terra.pearl.core.service.search.terms;

import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.sql.SQLJoinClause;
import bio.terra.pearl.core.service.search.sql.SQLSelectClause;
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
                (period.toTotalMonths() / 12.0) + (period.getDays() / 365.0)
        );
    }

    @Override
    public List<SQLJoinClause> requiredJoinClauses() {
        return List.of(new SQLJoinClause("profile", "profile", "enrollee.profile_id = profile.id "));
    }

    @Override
    public List<SQLSelectClause> requiredSelectClauses() {
        return List.of(
                new SQLSelectClause("profile", "birth_date")
        );
    }

    @Override
    public Optional<Condition> requiredConditions() {
        return Optional.empty();
    }

    @Override
    public String termClause() {
        return "AGE(profile.birth_date)";
    }

    @Override
    public List<Object> boundObjects() {
        return List.of();
    }

}
