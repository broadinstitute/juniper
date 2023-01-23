package bio.terra.pearl.core.factory.survey;

import bio.terra.pearl.core.model.survey.ParsedSnapshot;
import bio.terra.pearl.core.model.survey.ResponseData;
import bio.terra.pearl.core.model.survey.ResponseDataItem;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.Map;

public class ParsedSnapshotFactory {
    public static ParsedSnapshot fromMap(Map<String, Object> valueMap) {
        ResponseData responseData = new ResponseData();
        for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
            ResponseDataItem.ResponseDataItemBuilder itemBuilder = ResponseDataItem.builder()
                    .stableId(entry.getKey());
            if (entry.getValue() instanceof String) {
                itemBuilder.value(new TextNode((String) entry.getValue()));
            } else if (entry.getValue() instanceof Integer) {
                itemBuilder.value(new IntNode((Integer) entry.getValue()));
            }
            responseData.getItems().add(itemBuilder.build());
        }
        ParsedSnapshot snapshot = new ParsedSnapshot();
        snapshot.setParsedData(responseData);
        return snapshot;
    }
}
