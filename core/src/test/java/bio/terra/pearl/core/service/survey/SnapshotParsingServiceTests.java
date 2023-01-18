package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.model.survey.ParsedSnapshot;
import bio.terra.pearl.core.model.survey.ResponseData;
import bio.terra.pearl.core.model.survey.ResponseDataItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class SnapshotParsingServiceTests extends BaseSpringBootTest {
    @Autowired
    private SnapshotParsingService snapshotParsingService;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testParsing() {
        ResponseData responseData = new ResponseData();
        responseData.getItems().add(ResponseDataItem.builder()
                .stableId("testSurvey_q1").value(new TextNode("value1")).build());
        responseData.getItems().add(ResponseDataItem.builder()
                .stableId("testSurvey_q2").value(new IntNode(2)).build());
        ParsedSnapshot snapshot = new ParsedSnapshot();
        snapshot.setParsedData(responseData);

        TestTargetObj targetObj = snapshotParsingService.extractValues(snapshot, TARGET_MAP, TestTargetObj.class);

        Assertions.assertEquals("value1", targetObj.prop1);
        Assertions.assertEquals(2, targetObj.prop2);
    }

    private static final Map<String, String> TARGET_MAP = Map.of(
            "testSurvey_q1", "prop1",
            "testSurvey_q2", "prop2"
    );

    private record TestTargetObj(String prop1, int prop2) {}
}
