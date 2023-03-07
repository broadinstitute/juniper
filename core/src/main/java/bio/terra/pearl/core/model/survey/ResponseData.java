package bio.terra.pearl.core.model.survey;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ResponseData {
    private List<ResponseDataItem> items = new ArrayList<>();

    /**
     * gets a particular item by stableId.  This iterates through the entire set to find the item,
     * so it is only intended for grabbing one item in non-performance critical paths.
     * returns null if the item doesn't exist
     */
    public Optional<ResponseDataItem> getItem(String questionStableId) {
        return items.stream().filter(item -> item.getStableId().equals(questionStableId))
                .findFirst();
    }
}


