package bio.terra.pearl.core.service.search.terms;

import bio.terra.pearl.core.dao.participant.ProfileDao;
import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.sql.EnrolleeSearchQueryBuilder;
import org.jooq.Condition;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * This term can be used to search for the age of an enrollee. It uses the `birthDate` field from the enrollee's profile
 * and converts it to an integer representing the age in years.
 */
public class AgeTerm implements EnrolleeTerm {

    private final ProfileDao profileDao;

    public AgeTerm(ProfileDao profileDao) {
        this.profileDao = profileDao;
    }


    @Override
    public SearchValue extract(EnrolleeSearchContext context) {
        if (Objects.isNull(context.getProfile()) || Objects.isNull(context.getProfile().getBirthDate())) {
            return new SearchValue();
        }

        Period period = Period.between(context.getProfile().getBirthDate(), LocalDate.now());
        return new SearchValue(
                Math.abs(period.getYears())
        );
    }

    @Override
    public List<EnrolleeSearchQueryBuilder.JoinClause> requiredJoinClauses() {
        return List.of(new EnrolleeSearchQueryBuilder.JoinClause("profile", "profile", "enrollee.profile_id = profile.id "));
    }

    @Override
    public List<EnrolleeSearchQueryBuilder.SelectClause> requiredSelectClauses() {
        return List.of(
                new EnrolleeSearchQueryBuilder.SelectClause("profile", profileDao)
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
