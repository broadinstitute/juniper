package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.factory.survey.AnswerFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.MailingAddress;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.Answer;
import bio.terra.pearl.core.model.survey.AnswerMapping;
import bio.terra.pearl.core.model.survey.AnswerMappingMapType;
import bio.terra.pearl.core.model.survey.AnswerMappingTargetType;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.workflow.ObjectWithChangeLog;
import bio.terra.pearl.core.service.participant.ProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;

public class AnswerProcessingServiceTests extends BaseSpringBootTest {
    @Autowired
    private AnswerProcessingService answerProcessingService;
    @Autowired
    private ProfileService profileService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private EnrolleeFactory enrolleeFactory;
    @Autowired
    private StudyEnvironmentFactory studyEnvironmentFactory;
    @Autowired
    private PortalEnvironmentFactory portalEnvironmentFactory;
    @Autowired
    private SurveyFactory surveyFactory;

    @Test
    public void testMapToTypeWithProfile() {
        Profile profile = Profile.builder()
                .mailingAddress(MailingAddress.builder().build()).build();
        List<Answer> answers = AnswerFactory.fromMap(Map.of(
                "testSurvey_q1", "myFirstName",
                "testSurvey_q2", "addressPart1",
                "testSurvey_q3", "11/12/1987"
        ));
        List<AnswerMapping> mappings = List.of(
                AnswerMapping.builder()
                        .targetType(AnswerMappingTargetType.PROFILE)
                        .questionStableId("testSurvey_q1")
                        .targetField("givenName")
                        .mapType(AnswerMappingMapType.STRING_TO_STRING)
                        .build(),
                AnswerMapping.builder()
                        .targetType(AnswerMappingTargetType.PROFILE)
                        .questionStableId("testSurvey_q2")
                        .targetField("mailingAddress.street1")
                        .mapType(AnswerMappingMapType.STRING_TO_STRING)
                        .build(),
                AnswerMapping.builder()
                        .targetType(AnswerMappingTargetType.PROFILE)
                        .questionStableId("testSurvey_q3")
                        .targetField("birthDate")
                        .mapType(AnswerMappingMapType.STRING_TO_LOCAL_DATE)
                        .formatString("MM/dd/yyyy")
                        .build()
        );

        ObjectWithChangeLog<Profile> objChange = answerProcessingService.mapValuesToType(answers, mappings, profile,
                AnswerMappingTargetType.PROFILE);
        assertThat(objChange.obj().getGivenName(), equalTo("myFirstName"));
        assertThat(objChange.obj().getMailingAddress().getStreet1(), equalTo("addressPart1"));
        assertThat(objChange.obj().getBirthDate(), equalTo(LocalDate.of(1987,11, 12)));
        assertThat(objChange.changeRecords(), hasSize(3));
    }

    @Test
    @Transactional
    public void testNoOpWithNoMappings() {
        List<Answer> answers = AnswerFactory.fromMap(Map.of(
                "testSurvey_q1", "myFirstName",
                "testSurvey_q2", "addressPart1"
        ));

        List<Profile> before = profileService.findAll();
        answerProcessingService.processAllAnswerMappings(answers,
                new ArrayList<>(), null, UUID.randomUUID(), null, null);
        List<Profile> after = profileService.findAll();

        // no profiles were added or removed, in other words, no-op
        Assertions.assertEquals(before.size(), after.size());
    }

    @Test
    public void mapToDateHandlesFormatString() {
        AnswerMapping mapping = AnswerMapping.builder().formatString("MM/dd/yyyy").build();
        LocalDate result = AnswerProcessingService.mapToDate("11/12/1987", mapping);
        assertThat(result, equalTo(LocalDate.of(1987, 11, 12)));

        AnswerMapping europeanMapping = AnswerMapping.builder().formatString("dd/MM/yyyy").build();
        result = AnswerProcessingService.mapToDate("11/12/1987", europeanMapping);
        assertThat(result, equalTo(LocalDate.of(1987, 12, 11)));
    }

    @Test
    public void mapToDateHandlesBadStrings() {
        AnswerMapping mapping = AnswerMapping.builder().formatString("MM/dd/yyyy").build();
        LocalDate result = AnswerProcessingService.mapToDate(null, mapping);
        assertThat(result, nullValue());

        result = AnswerProcessingService.mapToDate("", mapping);
        assertThat(result, nullValue());

        result = AnswerProcessingService.mapToDate("foo", mapping);
        assertThat(result, nullValue());

        result = AnswerProcessingService.mapToDate("345567", mapping);
        assertThat(result, nullValue());
    }

    @Test
    public void mapToDateErrorsIfSet() {
        AnswerMapping mapping = AnswerMapping.builder().formatString("MM/dd/yyyy").errorOnFail(true).build();
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            AnswerProcessingService.mapToDate("badDate", mapping);
        });
    }

    @Test
    @Transactional
    public void testProfileUpdate(TestInfo info) {
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(getTestName(info), EnvironmentName.irb);
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, getTestName(info));
        EnrolleeFactory.EnrolleeBundle enrolleeBundle = enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnv, studyEnv);
        Survey survey = surveyFactory.buildPersisted(getTestName(info));

        List<Answer> answers = AnswerFactory.fromMap(Map.of(
                "testSurvey_q1", "myFirstName",
                "testSurvey_q2", "addressPart1",
                "testSurvey_q3", "11/12/1987"
        ));
        List<AnswerMapping> mappings = List.of(
                AnswerMapping.builder()
                        .targetType(AnswerMappingTargetType.PROFILE)
                        .questionStableId("testSurvey_q1")
                        .targetField("givenName")
                        .mapType(AnswerMappingMapType.STRING_TO_STRING)
                        .build(),
                AnswerMapping.builder()
                        .targetType(AnswerMappingTargetType.PROFILE)
                        .questionStableId("testSurvey_q2")
                        .targetField("mailingAddress.street1")
                        .mapType(AnswerMappingMapType.STRING_TO_STRING)
                        .build(),
                AnswerMapping.builder()
                        .targetType(AnswerMappingTargetType.PROFILE)
                        .questionStableId("testSurvey_q3")
                        .targetField("birthDate")
                        .mapType(AnswerMappingMapType.STRING_TO_LOCAL_DATE)
                        .formatString("MM/dd/yyyy")
                        .build()
        );

        answerProcessingService.processAllAnswerMappings(
                answers,
                mappings,
                enrolleeBundle.portalParticipantUser(),
                enrolleeBundle.portalParticipantUser().getParticipantUserId(),
                enrolleeBundle.enrollee().getId(),
                survey.getId());

        Profile after = profileService.loadWithMailingAddress(enrolleeBundle.portalParticipantUser().getProfileId()).orElseThrow();

        Assertions.assertEquals(LocalDate.of(1987, 11, 12), after.getBirthDate());
        Assertions.assertEquals("myFirstName", after.getGivenName());
        Assertions.assertEquals("addressPart1", after.getMailingAddress().getStreet1());


    }
}
