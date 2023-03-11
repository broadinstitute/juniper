package bio.terra.pearl.core.factory.survey;

import bio.terra.pearl.core.model.survey.ParsedSnapshot;
import bio.terra.pearl.core.model.survey.ResponseData;
import java.util.Map;

public class ParsedSnapshotFactory {
    public static ParsedSnapshot fromMap(Map<String, Object> valueMap) {
        ResponseData responseData = ResponseDataFactory.fromMap(valueMap);
        ParsedSnapshot snapshot = new ParsedSnapshot();
        snapshot.setParsedData(responseData);
        return snapshot;
    }
}
