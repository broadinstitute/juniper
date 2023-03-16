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
import com.fasterxml.jackson.databind.node.TextNode;
import java.time.LocalDate;
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
                "testSurvey_q2", "addressPart1",
                "testSurvey_q3", "11/12/1987"
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
                        .build(),
                AnswerMapping.builder()
                        .targetType(AnswerMappingTargetType.PROFILE)
                        .questionStableId("testSurvey_q3")
                        .targetField("birthDate")
                        .mapType(AnswerMappingMapType.TEXT_NODE_TO_LOCAL_DATE)
                        .formatString("MM/dd/yyyy")
                        .build()
        );

        ObjectWithChangeLog<Profile> objChange = snapshotProcessingService.mapValuesToType(snapshot.getParsedData(), mappings, profile,
                AnswerMappingTargetType.PROFILE);
        assertThat(objChange.obj().getGivenName(), equalTo("myFirstName"));
        assertThat(objChange.obj().getMailingAddress().getStreet1(), equalTo("addressPart1"));
        assertThat(objChange.obj().getBirthDate(), equalTo(LocalDate.of(1987,11, 12)));
        assertThat(objChange.changeRecords(), hasSize(3));
    }

    @Test
    public void testNoOpWithNoMappings() {
        ParsedSnapshot snapshot = ParsedSnapshotFactory.fromMap(Map.of(
                "testSurvey_q1", "myFirstName",
                "testSurvey_q2", "addressPart1"
        ));
        List<DataChangeRecord> changeRecords = snapshotProcessingService.processAllAnswerMappings(snapshot.getParsedData(),
                new ArrayList<>(), null, UUID.randomUUID(), null, null);
        assertThat(changeRecords, hasSize(0));
    }

    @Test
    public void mapToDateHandlesFormatString() {
        AnswerMapping mapping = AnswerMapping.builder().formatString("MM/dd/yyyy").build();
        LocalDate result = SnapshotProcessingService.mapToDate(new TextNode("11/12/1987"), mapping);
        assertThat(result, equalTo(LocalDate.of(1987, 11, 12)));

        AnswerMapping europeanMapping = AnswerMapping.builder().formatString("dd/MM/yyyy").build();
        result = SnapshotProcessingService.mapToDate(new TextNode("11/12/1987"), europeanMapping);
        assertThat(result, equalTo(LocalDate.of(1987, 12, 11)));
    }

    @Test
    public void mapToDateHandlesBadStrings() {
        AnswerMapping mapping = AnswerMapping.builder().formatString("MM/dd/yyyy").build();
        LocalDate result = SnapshotProcessingService.mapToDate(new TextNode(null), mapping);
        assertThat(result, nullValue());

        result = SnapshotProcessingService.mapToDate(new TextNode(""), mapping);
        assertThat(result, nullValue());

        result = SnapshotProcessingService.mapToDate(new TextNode("foo"), mapping);
        assertThat(result, nullValue());

        result = SnapshotProcessingService.mapToDate(new TextNode("345567"), mapping);
        assertThat(result, nullValue());
    }

    @Test
    public void mapToDateErrorsIfSet() {
        AnswerMapping mapping = AnswerMapping.builder().formatString("MM/dd/yyyy").errorOnFail(true).build();
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            SnapshotProcessingService.mapToDate(new TextNode("badDate"), mapping);
        });
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
