package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.survey.ParsedSnapshotFactory;
import bio.terra.pearl.core.model.participant.MailingAddress;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.survey.AnswerMapping;
import bio.terra.pearl.core.model.survey.AnswerMappingMapType;
import bio.terra.pearl.core.model.survey.AnswerMappingTargetType;
import bio.terra.pearl.core.model.survey.ParsedSnapshot;
import bio.terra.pearl.core.model.workflow.DataChangeRecord;
import bio.terra.pearl.core.model.workflow.ObjectWithChangeLog;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class SnapshotProcessingServiceTests extends BaseSpringBootTest {
    @Autowired
    private SnapshotProcessingService snapshotProcessingService;
    @Autowired
    private ObjectMapper objectMapper;


    @Test
    public void testMapToTypeWithProfile() {
        Profile profile = Profile.builder()
                .mailingAddress(MailingAddress.builder().build()).build();
        ParsedSnapshot snapshot = ParsedSnapshotFactory.fromMap(Map.of(
                "testSurvey_q1", "myFirstName",
                "testSurvey_q2", "addressPart1"
        ));
        List<AnswerMapping> mappings = List.of(
                AnswerMapping.builder()
                        .targetType(AnswerMappingTargetType.PROFILE)
                        .questionStableId("testSurvey_q1")
                        .targetField("givenName")
                        .mapType(AnswerMappingMapType.TEXT_NODE_TO_STRING)
                        .build(),
                AnswerMapping.builder()
                        .targetType(AnswerMappingTargetType.PROFILE)
                        .questionStableId("testSurvey_q2")
                        .targetField("mailingAddress.street1")
                        .mapType(AnswerMappingMapType.TEXT_NODE_TO_STRING)
                        .build()
        );


        ObjectWithChangeLog<Profile> objChange = snapshotProcessingService.mapValuesToType(snapshot, mappings, profile,
                AnswerMappingTargetType.PROFILE);
        assertThat(objChange.obj().getGivenName(), equalTo("myFirstName"));
        assertThat(objChange.obj().getMailingAddress().getStreet1(), equalTo("addressPart1"));
        assertThat(objChange.changeRecords(), hasSize(2));
    }

    @Test
    public void testNoOpWithNoMappings() {
        ParsedSnapshot snapshot = ParsedSnapshotFactory.fromMap(Map.of(
                "testSurvey_q1", "myFirstName",
                "testSurvey_q2", "addressPart1"
        ));
        List<DataChangeRecord> changeRecords = snapshotProcessingService.processAllAnswerMappings(snapshot,
                new ArrayList<>(), null, UUID.randomUUID());
        assertThat(changeRecords, hasSize(0));
    }

    @Test
    public void testParsing() {
        ParsedSnapshot snapshot = ParsedSnapshotFactory.fromMap(Map.of(
                "testSurvey_q1", "value1",
                "testSurvey_q2", 2
        ));
        TestTargetObj targetObj = snapshotProcessingService.extractValues(snapshot, TARGET_MAP, TestTargetObj.class);

        Assertions.assertEquals("value1", targetObj.prop1);
        Assertions.assertEquals(2, targetObj.prop2);
    }

    private static final Map<String, String> TARGET_MAP = Map.of(
            "testSurvey_q1", "prop1",
            "testSurvey_q2", "prop2"
    );

    private record TestTargetObj(String prop1, int prop2) {}
}
