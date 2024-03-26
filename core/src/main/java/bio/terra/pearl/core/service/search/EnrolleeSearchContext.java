package bio.terra.pearl.core.service.search;

import bio.terra.pearl.core.service.rule.EnrolleeProfileBundle;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * Context for evaluating a search expression on an enrollee.
 */
@Getter
@SuperBuilder
public class EnrolleeSearchContext extends EnrolleeProfileBundle {
    public EnrolleeSearchContext(EnrolleeProfileBundle bundle) {
        super(bundle.getEnrollee(), bundle.getProfile());
    }
}
