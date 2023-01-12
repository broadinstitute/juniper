package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.model.survey.ParsedSnapshot;
import bio.terra.pearl.core.model.survey.ResponseDataItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Handles mapping ParsedSnapshots (typically received from the frontend) into objects.  This is done with stableIdMaps
 * which map question stableIds to the object properties they should be assigned to.
 */
@Service
public class SnapshotParsingService {
    private ObjectMapper objectMapper;
    public SnapshotParsingService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    public <T> T extractValues(ParsedSnapshot snapshot, Map<String, String> stableIdMap, Class<T> clazz) {
        Map<String, Object> fieldValues = new HashMap<>();
        for (ResponseDataItem item : snapshot.getParsedData().getItems()) {
            String stableId = item.getStableId();
            if (stableIdMap.containsKey(stableId)) {
                fieldValues.put(stableIdMap.get(stableId), item.getValue());
            }

        }
        return objectMapper.convertValue(fieldValues, clazz);
    }

}
