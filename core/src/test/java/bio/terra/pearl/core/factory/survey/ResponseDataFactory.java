package bio.terra.pearl.core.factory.survey;

import bio.terra.pearl.core.model.survey.ResponseData;
import bio.terra.pearl.core.model.survey.ResponseDataItem;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.Map;

public class ResponseDataFactory {
    public static ResponseData fromMap(Map<String, Object> valueMap) {
        ResponseData responseData = new ResponseData();
        for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
            ResponseDataItem.ResponseDataItemBuilder itemBuilder = ResponseDataItem.builder()
                    .stableId(entry.getKey());
            if (entry.getValue() instanceof String) {
                itemBuilder.value(new TextNode((String) entry.getValue()));
            } else if (entry.getValue() instanceof Integer) {
                itemBuilder.value(new IntNode((Integer) entry.getValue()));
            } else if (entry.getValue() instanceof Boolean) {
                itemBuilder.value(BooleanNode.valueOf((boolean) entry.getValue()));
            }
            responseData.getItems().add(itemBuilder.build());
        }
        return responseData;
    }
}
