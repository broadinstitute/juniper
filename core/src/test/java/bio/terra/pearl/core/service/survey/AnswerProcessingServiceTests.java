package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.factory.survey.AnswerFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.address.MailingAddress;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.audit.ResponsibleEntity;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.Answer;
import bio.terra.pearl.core.model.survey.AnswerMapping;
import bio.terra.pearl.core.model.survey.AnswerMappingMapType;
import bio.terra.pearl.core.model.survey.AnswerMappingTargetType;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.workflow.ObjectWithChangeLog;
import bio.terra.pearl.core.service.participant.ParticipantUserService;
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
    private ParticipantUserService participantUserService;
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
        answerProcessingService.processAllAnswerMappings(null, answers,
                new ArrayList<>(), null, null, DataAuditInfo.builder().build());
        List<Profile> after = profileService.findAll();

        // no profiles were added or removed, in other words, no-op
        Assertions.assertEquals(before.size(), after.size());
    }

    @Test
    public void mapStringToStringTrims() {
        AnswerMapping mapping = new AnswerMapping();
        Object result = AnswerProcessingService.JSON_MAPPERS.get(AnswerMappingMapType.STRING_TO_STRING)
                .apply(Answer.builder().stringValue("  foo  ").build(), mapping);
        assertThat((String) result, equalTo("foo"));
    }

    @Test
    public void mapStringToBoolean() {
        AnswerMapping mapping = new AnswerMapping();
        Object result = AnswerProcessingService.JSON_MAPPERS.get(AnswerMappingMapType.STRING_TO_BOOLEAN)
                .apply(Answer.builder().stringValue("true").build(), mapping);
        assertThat((Boolean) result, equalTo(true));

        result = AnswerProcessingService.JSON_MAPPERS.get(AnswerMappingMapType.STRING_TO_BOOLEAN)
                .apply(Answer.builder().stringValue("false").build(), mapping);
        assertThat((Boolean) result, equalTo(false));
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
                "testSurvey_q2", "addressPart1 ",
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
                enrolleeBundle.enrollee(),
                answers,
                mappings,
                enrolleeBundle.portalParticipantUser(),
                new ResponsibleEntity(enrolleeBundle.participantUser()),
                DataAuditInfo.builder()
                        .responsibleUserId(enrolleeBundle.portalParticipantUser().getParticipantUserId())
                        .enrolleeId(enrolleeBundle.enrollee().getId())
                        .surveyId(survey.getId())
                        .build());

        Profile after = profileService.loadWithMailingAddress(enrolleeBundle.portalParticipantUser().getProfileId()).orElseThrow();

        Assertions.assertEquals(LocalDate.of(1987, 11, 12), after.getBirthDate());
        Assertions.assertEquals("myFirstName", after.getGivenName());
        Assertions.assertEquals("addressPart1", after.getMailingAddress().getStreet1());
    }

    @Test
    @Transactional
    public void testProxyProfileUpdate(TestInfo info) {
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(getTestName(info), EnvironmentName.irb);
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, getTestName(info));
        EnrolleeFactory.EnrolleeAndProxy enrolleeAndProxy = enrolleeFactory.buildProxyAndGovernedEnrollee(getTestName(info), portalEnv, studyEnv);
        ParticipantUser pUser = participantUserService.find(enrolleeAndProxy.proxyPpUser().getParticipantUserId()).orElseThrow();
        Survey survey = surveyFactory.buildPersisted(getTestName(info));

        List<Answer> answers = AnswerFactory.fromMap(Map.of(
                "testSurvey_q1", "myFirstName",
                "testSurvey_q2", "addressPart1",
                "testSurvey_q3", "11/12/1987",
                "testSurvey_q4", "governedUserName",
                "testSurvey_q5", "another address",
                "testSurvey_q6", "01/01/2018"
        ));
        List<AnswerMapping> mappings = List.of(
                AnswerMapping.builder()
                        .targetType(AnswerMappingTargetType.PROXY_PROFILE)
                        .questionStableId("testSurvey_q1")
                        .targetField("givenName")
                        .mapType(AnswerMappingMapType.STRING_TO_STRING)
                        .build(),
                AnswerMapping.builder()
                        .targetType(AnswerMappingTargetType.PROXY_PROFILE)
                        .questionStableId("testSurvey_q2")
                        .targetField("mailingAddress.street1")
                        .mapType(AnswerMappingMapType.STRING_TO_STRING)
                        .build(),
                AnswerMapping.builder()
                        .targetType(AnswerMappingTargetType.PROXY_PROFILE)
                        .questionStableId("testSurvey_q3")
                        .targetField("birthDate")
                        .mapType(AnswerMappingMapType.STRING_TO_LOCAL_DATE)
                        .formatString("MM/dd/yyyy")
                        .build(),
                AnswerMapping.builder()
                        .targetType(AnswerMappingTargetType.PROFILE)
                        .questionStableId("testSurvey_q4")
                        .targetField("givenName")
                        .mapType(AnswerMappingMapType.STRING_TO_STRING)
                        .build(),
                AnswerMapping.builder()
                        .targetType(AnswerMappingTargetType.PROFILE)
                        .questionStableId("testSurvey_q5")
                        .targetField("mailingAddress.street1")
                        .mapType(AnswerMappingMapType.STRING_TO_STRING)
                        .build(),
                AnswerMapping.builder()
                        .targetType(AnswerMappingTargetType.PROFILE)
                        .questionStableId("testSurvey_q6")
                        .targetField("birthDate")
                        .mapType(AnswerMappingMapType.STRING_TO_LOCAL_DATE)
                        .formatString("MM/dd/yyyy")
                        .build()
        );

        answerProcessingService.processAllAnswerMappings(
                enrolleeAndProxy.governedEnrollee(),
                answers,
                mappings,
                enrolleeAndProxy.proxyPpUser(),
                new ResponsibleEntity(pUser),
                DataAuditInfo.builder()
                        .responsibleUserId(enrolleeAndProxy.proxyPpUser().getParticipantUserId())
                        .enrolleeId(enrolleeAndProxy.governedEnrollee().getId())
                        .surveyId(survey.getId())
                        .build());


        Profile governedUserProfile = profileService.loadWithMailingAddress(enrolleeAndProxy.governedEnrollee().getProfileId()).orElseThrow();

        Assertions.assertEquals(LocalDate.of(2018, 1, 1), governedUserProfile.getBirthDate());
        Assertions.assertEquals("governedUserName", governedUserProfile.getGivenName());
        Assertions.assertEquals("another address", governedUserProfile.getMailingAddress().getStreet1());

        Profile proxyProfile = profileService.loadWithMailingAddress(enrolleeAndProxy.proxyPpUser().getProfileId()).orElseThrow();

        Assertions.assertEquals(LocalDate.of(1987, 11, 12), proxyProfile.getBirthDate());
        Assertions.assertEquals("myFirstName", proxyProfile.getGivenName());
        Assertions.assertEquals("addressPart1", proxyProfile.getMailingAddress().getStreet1());

    }
}
