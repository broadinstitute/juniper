package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.survey.ParsedSnapshotFactory;
import bio.terra.pearl.core.model.survey.ParsedSnapshot;
import com.fasterxml.jackson.databind.ObjectMapper;
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
        ParsedSnapshot snapshot = ParsedSnapshotFactory.fromMap(Map.of(
                "testSurvey_q1", "value1",
                "testSurvey_q2", 2
        ));
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
