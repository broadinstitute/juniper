package bio.terra.pearl.core.dao.search;

import bio.terra.pearl.core.model.search.EnrolleeSearchResult;
import org.jdbi.v3.core.result.LinkedHashMapRowReducer;
import org.jdbi.v3.core.result.RowView;

import java.util.Map;
import java.util.UUID;

public class EnrolleeSearchResultReducer implements LinkedHashMapRowReducer<UUID, EnrolleeSearchResult> {
    @Override
    public void accumulate(Map<UUID, EnrolleeSearchResult> map, RowView rowView) {
        final EnrolleeSearchResult searchResult = map.computeIfAbsent(rowView.getColumn("enrollee_id", UUID.class),
                id -> rowView.getRow(EnrolleeSearchResult.class));

        // currently, does nothing, but in the future could reduce down rows from tables which
        // could return multiple values; e.g., kits
    }
}
