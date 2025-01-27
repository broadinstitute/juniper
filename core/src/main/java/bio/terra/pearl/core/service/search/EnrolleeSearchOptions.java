package bio.terra.pearl.core.service.search;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@SuperBuilder
@Getter
@Setter
public class EnrolleeSearchOptions {
    // be careful; potential sql injection, only use trusted values
    private String sortField;
    private boolean sortAscending;
    // additional data collections to include in the result -- these will be attached post-filtering.
    @Builder.Default
    private List<Include> includes = new ArrayList<>();

    // limit number of results
    private Integer limit;

    public enum Include {
        kitRequests,
        tasks
    }
}
