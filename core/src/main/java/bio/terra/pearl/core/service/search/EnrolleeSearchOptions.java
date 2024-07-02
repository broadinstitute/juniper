package bio.terra.pearl.core.service.search;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
public class EnrolleeSearchOptions {
    // be careful; potential sql injection, only use trusted values
    private String sortField;
    private boolean sortAscending;

    // limit number of results
    private Integer limit;
}
