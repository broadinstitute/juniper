package bio.terra.pearl.core.service.workflow;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.dao.survey.AnswerMappingDao;
import bio.terra.pearl.core.factory.StudyEnvironmentBundle;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.participant.ParticipantUserFactory;
import bio.terra.pearl.core.factory.survey.AnswerFactory;
import bio.terra.pearl.core.factory.survey.PreEnrollmentSurveyFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.audit.ResponsibleEntity;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.EnrolleeRelation;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.participant.RelationshipType;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import bio.terra.pearl.core.model.survey.*;
import bio.terra.pearl.core.model.workflow.*;
import bio.terra.pearl.core.service.participant.EnrolleeRelationService;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.ProfileService;
import bio.terra.pearl.core.service.study.StudyEnvironmentConfigService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.study.StudyService;
import bio.terra.pearl.core.service.survey.AnswerService;
import bio.terra.pearl.core.service.survey.SurveyResponseService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** class for high-level tests of workflow operations -- enroll, consent, etc... */
public class EnrollmentWorkflowTests extends BaseSpringBootTest {
    @Autowired
    private StudyEnvironmentFactory studyEnvironmentFactory;
    @Autowired
    private ParticipantUserFactory participantUserFactory;
    @Autowired
    private StudyService studyService;
    @Autowired
    private EnrolleeService enrolleeService;
    @Autowired
    private EnrollmentService enrollmentService;
    @Autowired
    private EnrolleeRelationService enrolleeRelationService;
    @Autowired
    private SurveyFactory surveyFactory;
    @Autowired
    private StudyEnvironmentConfigService studyEnvironmentConfigService;
    @Autowired
    private ParticipantTaskService participantTaskService;
    @Autowired
    private SurveyResponseService surveyResponseService;
    @Autowired
    private PreEnrollmentSurveyFactory preEnrollmentSurveyFactory;
    @Autowired
    private AnswerMappingDao answerMappingDao;
    @Autowired
    private ProfileService profileService;
    @Autowired
    private StudyEnvironmentService studyEnvironmentService;
    @Autowired
    private AnswerService answerService;
    @Autowired
    private EventService eventService;

    @Test
    @Transactional
    public void testEnroll(TestInfo info) {
        StudyEnvironmentBundle bundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);
        PortalEnvironment portalEnv = bundle.getPortalEnv();
        StudyEnvironment studyEnv = bundle.getStudyEnv();
        String studyShortcode =  bundle.getStudy().getShortcode();

        ParticipantUserFactory.ParticipantUserAndPortalUser userBundle = participantUserFactory.buildPersisted(portalEnv, getTestName(info));
        Survey consent = surveyFactory.buildPersisted(surveyFactory.builder(getTestName(info))
                .surveyType(SurveyType.CONSENT)
                .portalId(portalEnv.getPortalId()));
        surveyFactory.attachToEnv(consent, studyEnv.getId(), true);

        HubResponse hubResponse = enrollmentService.enroll(userBundle.ppUser(), studyEnv.getEnvironmentName(), studyShortcode,
                userBundle.user(), userBundle.ppUser(), null, true);
        Enrollee enrollee = hubResponse.getEnrollee();
        assertThat(enrollee.getShortcode(), notNullValue());
        assertThat(enrollee.getParticipantUserId(), equalTo(userBundle.user().getId()));

        assertThat(enrolleeService.findByStudyEnvironment(studyEnv.getId()), contains(enrollee));

        // Because the study environment had a consent attached, a consent task should be created on enrollment
        assertThat(enrollee.getParticipantTasks(), hasSize(1));
        assertThat(enrollee.getParticipantTasks(), contains(hasProperty("taskType", equalTo(TaskType.CONSENT))));
        assertThat(enrollee.isConsented(), equalTo(false));
    }

    /** test of enroll -> consent -> survey */
    @Test
    @Transactional
    public void testParticipantWorkflow(TestInfo info) {
        StudyEnvironmentBundle bundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);
        PortalEnvironment portalEnv = bundle.getPortalEnv();
        StudyEnvironment studyEnv = bundle.getStudyEnv();
        String studyShortcode = bundle.getStudy().getShortcode();
        ParticipantUserFactory.ParticipantUserAndPortalUser userBundle = participantUserFactory.buildPersisted(portalEnv, getTestName(info));

        Survey consent = surveyFactory.buildPersisted(surveyFactory.builder(getTestName(info))
                .surveyType(SurveyType.CONSENT)
                .portalId(portalEnv.getPortalId()));
        surveyFactory.attachToEnv(consent, studyEnv.getId(), true);

        Survey survey = surveyFactory.buildPersisted(surveyFactory.builder(getTestName(info)).portalId(portalEnv.getPortalId()));
        surveyFactory.attachToEnv(survey, studyEnv.getId(), true);

        HubResponse hubResponse = enrollmentService.enroll(userBundle.ppUser(), studyEnv.getEnvironmentName(), studyShortcode,
                userBundle.user(), userBundle.ppUser(), null, true);
        Enrollee enrollee = hubResponse.getEnrollee();
        assertThat(hubResponse.getProfile(), notNullValue());
        // Because the study environment had a consent attached, a consent task should be created on enrollment
        assertThat(enrollee.getParticipantTasks(), hasSize(2));
        ParticipantTask consentTask = enrollee.getParticipantTasks().stream()
                .filter(task -> task.getTaskType().equals(TaskType.CONSENT))
                .findFirst().orElseThrow();
        assertThat(enrollee.isConsented(), equalTo(false));

        SurveyResponse consentResponse = SurveyResponse.builder()
                .answers(AnswerFactory.fromMap(Map.of("sampleQuestion", "foo")))
                .complete(false)
                .resumeData("")
                .build();
        surveyResponseService.updateResponse(consentResponse, new ResponsibleEntity(userBundle.user()), null, userBundle.ppUser(),
                enrollee, consentTask.getId(), consent.getPortalId());
        // since the response is incomplete, no consent event should be fired
        assertThat(getEventsByType(enrollee.getId()).get(EventClass.ENROLLEE_CONSENT_EVENT), nullValue());

        consentResponse = SurveyResponse.builder()
                .answers(AnswerFactory.fromMap(Map.of("sampleQuestion", "foobar")))
                .complete(true)
                .resumeData("")
                .build();
        surveyResponseService.updateResponse(consentResponse, new ResponsibleEntity(userBundle.user()), null, userBundle.ppUser(),
                enrollee, consentTask.getId(), consent.getPortalId());
        assertThat(getEventsByType(enrollee.getId()).get(EventClass.ENROLLEE_CONSENT_EVENT), hasSize(1));
        assertThat(getEventsByType(enrollee.getId()).get(EventClass.ENROLLEE_SURVEY_EVENT), hasSize(2));

        Enrollee refreshedEnrollee = enrolleeService.find(enrollee.getId()).get();
        assertThat(refreshedEnrollee.isConsented(), equalTo(true));

        List<ParticipantTask> tasks = participantTaskService.findByEnrolleeId(enrollee.getId());
        assertThat(tasks, hasSize(2));
        List<ParticipantTask> surveyTasks = tasks.stream().filter(task -> task.getTaskType().equals(TaskType.SURVEY)).toList();
        assertThat(surveyTasks, hasSize(1));
        assertThat(surveyTasks.get(0).getTargetStableId(), equalTo(survey.getStableId()));

        // now try to complete the survey
        SurveyResponse survResponse = SurveyResponse.builder()
                        .answers(AnswerFactory.fromMap(Map.of("sampleQuestion", "foo")))
                        .complete(true)
                        .resumeData("stuff")
                        .build();
        hubResponse = surveyResponseService.updateResponse(survResponse, new ResponsibleEntity(userBundle.user()), null, userBundle.ppUser(),
                enrollee, surveyTasks.get(0).getId(), survey.getPortalId());
        List<ParticipantTask> updatedTasks = participantTaskService.findByEnrolleeId(enrollee.getId());
        assertThat(updatedTasks, containsInAnyOrder(hubResponse.getTasks().toArray()));
        List<ParticipantTask>  updateSurveyTasks = updatedTasks.stream().filter(task -> task.getTaskType().equals(TaskType.SURVEY)).toList();
        assertThat(updatedTasks, hasSize(2));
        assertThat(updateSurveyTasks, hasSize(1));
        assertThat(updateSurveyTasks.get(0).getStatus(), equalTo(TaskStatus.COMPLETE));
        assertThat(updateSurveyTasks.get(0).getCompletedAt(), greaterThan(Instant.now().minusSeconds(20)));
        assertThat(hubResponse.getProfile(), notNullValue());
    }

    @Test
    @Transactional
    public void testBackfillPreEnroll(TestInfo info) {
        StudyEnvironmentBundle studyEnvBundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);
        PortalEnvironment portalEnv = studyEnvBundle.getPortalEnv();
        StudyEnvironment studyEnv = studyEnvBundle.getStudyEnv();
        EnvironmentName envName = studyEnv.getEnvironmentName();
        Study study = studyEnvBundle.getStudy();

        Survey preEnrollmentSurvey = surveyFactory.buildPersisted(getTestName(info));
        StudyEnvironmentSurvey.builder()
                .surveyId(preEnrollmentSurvey.getId())
                .studyEnvironmentId(studyEnv.getId())
                .build();

        studyEnv.setPreEnrollSurveyId(preEnrollmentSurvey.getId());
        studyEnvironmentService.update(
                studyEnv
        );

        String preEnrollmentSurveyResponseData = """
                [{"questionStableId": "proxyQuestion","surveyVersion":0,"viewedLanguage":"en","stringValue":"false"},
                {"createdAt":1710527339.202811000,"lastUpdatedAt":1710527339.202811000,"questionStableId":"qualified","surveyVersion":0,"booleanValue":true}]""";
        ParticipantUserFactory.ParticipantUserAndPortalUser userBundle = participantUserFactory.buildPersisted(portalEnv, getTestName(info));
        PreEnrollmentResponse preEnrollmentResponse = preEnrollmentSurveyFactory.buildPersisted(
                getTestName(info),
                preEnrollmentSurvey.getId(),
                true,
                preEnrollmentSurveyResponseData,
                userBundle.ppUser().getId(),
                userBundle.user().getId(),
                studyEnv.getId());

        HubResponse<Enrollee> hubResponse = enrollmentService.enroll(
                envName,
                study.getShortcode(),
                userBundle.user(),
                userBundle.ppUser(),
                preEnrollmentResponse.getId());
        // confirm that two enrollees were created, and only one is a subject
        assertThat(hubResponse.getEnrollee().isSubject(), equalTo(true));


        List<Answer> answers = answerService.findByEnrolleeAndSurvey(hubResponse.getEnrollee().getId(), preEnrollmentSurvey.getStableId());
        assertEquals(2, answers.size());
        assertTrue(answers.stream().anyMatch(
                answer -> answer.getQuestionStableId().equals("proxyQuestion")
                        && answer.getAnswerType().equals(AnswerType.STRING)
                        && answer.getStringValue().equals("false")));
        assertTrue(answers.stream().anyMatch(
                answer -> answer.getQuestionStableId().equals("qualified")
                        && answer.getAnswerType().equals(AnswerType.BOOLEAN)
                        && answer.getBooleanValue()));

    }


    @Test
    @Transactional
    public void testGovernedUserEnrollment(TestInfo info){
        StudyEnvironmentBundle studyEnvBundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);
        PortalEnvironment portalEnv = studyEnvBundle.getPortalEnv();
        StudyEnvironment studyEnv = studyEnvBundle.getStudyEnv();
        ParticipantUserFactory.ParticipantUserAndPortalUser userBundle = participantUserFactory.buildPersisted(portalEnv,getTestName(info));
        String studyShortcode = studyService.find(studyEnv.getStudyId()).get().getShortcode();

        Survey consent = surveyFactory.buildPersisted(surveyFactory.builder(getTestName(info))
                .surveyType(SurveyType.CONSENT)
                .portalId(portalEnv.getPortalId()));
        surveyFactory.attachToEnv(consent, studyEnv.getId(), true);

        HubResponse<Enrollee> hubResponse = enrollmentService.enrollAsProxy(studyEnv.getEnvironmentName(), studyShortcode, userBundle.user(), userBundle.ppUser(),
                null);
        Enrollee enrollee = hubResponse.getResponse();
        Enrollee proxyEnrollee = hubResponse.getEnrollee();
        assertThat(enrollee.getShortcode(), notNullValue());
        Assertions.assertNotEquals(enrollee.getParticipantUserId(), userBundle.user().getId());
        // check that we've created an enrollee for the proxy too
        Assertions.assertEquals(enrolleeService.findByPortalParticipantUser(userBundle.ppUser()).size(), 1);
        List<EnrolleeRelation> enrolleeRelations = enrolleeRelationService.findAll();
        Assertions.assertEquals(enrolleeRelations.size(), 1);
        assertThat(enrolleeRelations.get(0), samePropertyValuesAs(EnrolleeRelation.builder()
                .enrolleeId(hubResponse.getEnrollee().getId())
                .targetEnrolleeId(hubResponse.getResponse().getId())
                .relationshipType(RelationshipType.PROXY).build(), "id", "beginDate", "endDate", "createdAt", "lastUpdatedAt"));
        assertThat(enrolleeService.findByStudyEnvironment(studyEnv.getId()), containsInAnyOrder(enrollee, proxyEnrollee));

        // Because the study environment had a consent attached, a consent task should be created on enrollment
        assertThat(participantTaskService.findByEnrolleeId(enrollee.getId()), hasSize(1));
        assertThat(enrollee.getParticipantTasks(), hasSize(1));
        assertThat(enrollee.getParticipantTasks(), contains(hasProperty("taskType", equalTo(TaskType.CONSENT))));
        assertThat(enrollee.isConsented(), equalTo(false));

        // the consent form should NOT be given as a task to the proxy
        assertThat(proxyEnrollee.getParticipantTasks(), hasSize(0));
        assertThat(participantTaskService.findByEnrolleeId(proxyEnrollee.getId()), hasSize(0));
    }

    @Test
    @Transactional
    public void testProxyEnrollingMultipleChild(TestInfo info){
        StudyEnvironmentBundle studyEnvBundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);
        PortalEnvironment portalEnv = studyEnvBundle.getPortalEnv();
        StudyEnvironment studyEnv = studyEnvBundle.getStudyEnv();
        ParticipantUserFactory.ParticipantUserAndPortalUser userBundle = participantUserFactory.buildPersisted(portalEnv,getTestName(info));
        String studyShortcode = studyService.find(studyEnv.getStudyId()).get().getShortcode();

        Survey consent = surveyFactory.buildPersisted(surveyFactory.builder(getTestName(info))
                .surveyType(SurveyType.CONSENT)
                .portalId(portalEnv.getPortalId()));
        surveyFactory.attachToEnv(consent, studyEnv.getId(), true);

        HubResponse<Enrollee> hubResponse1 = enrollmentService.enrollAsProxy(studyEnv.getEnvironmentName(), studyShortcode, userBundle.user(), userBundle.ppUser(),
                 null);
        Enrollee proxyEnrollee = hubResponse1.getEnrollee();
        HubResponse<Enrollee> hubResponse2 = enrollmentService.enrollAsProxy(studyEnv.getEnvironmentName(), studyShortcode,userBundle.user(), userBundle.ppUser(),
                null);
        Enrollee governedEnrollee1 = hubResponse1.getResponse();
        Enrollee governedEnrollee2 = hubResponse2.getResponse();

        Assertions.assertNotEquals(governedEnrollee1.getShortcode(), governedEnrollee2.getShortcode());
        Assertions.assertNotEquals(governedEnrollee1.getParticipantUserId(), governedEnrollee2.getParticipantUserId());
        //participant user id for governed user and proxy user should differ
        Assertions.assertNotEquals(governedEnrollee1.getParticipantUserId(), proxyEnrollee.getParticipantUserId());
        //assert that the proxy was enrolled
        Assertions.assertEquals(enrolleeService.findByPortalParticipantUser(userBundle.ppUser()).size(), 1);
        List<EnrolleeRelation> proxyEnrolleeRelations = enrolleeRelationService.findAll();
        assertThat(proxyEnrolleeRelations, hasSize(2));
        List<EnrolleeRelation> relations1 = enrolleeRelationService.findByTargetEnrolleeId(governedEnrollee1.getId());
        assertThat(relations1, hasSize(1));
        assertThat(relations1.get(0), samePropertyValuesAs(EnrolleeRelation.builder()
                .enrolleeId(proxyEnrollee.getId())
                .targetEnrolleeId(governedEnrollee1.getId())
                .relationshipType(RelationshipType.PROXY).build(), "id", "beginDate", "endDate", "createdAt", "lastUpdatedAt"));

        List<EnrolleeRelation> relations2 = enrolleeRelationService.findByTargetEnrolleeId(governedEnrollee2.getId());
        assertThat(relations2, hasSize(1));
        assertThat(relations2.get(0), samePropertyValuesAs(EnrolleeRelation.builder()
                .enrolleeId(proxyEnrollee.getId())
                .targetEnrolleeId(governedEnrollee2.getId())
                .relationshipType(RelationshipType.PROXY).build(), "id", "beginDate", "endDate", "createdAt", "lastUpdatedAt"));

        // Because the study environment had a consent attached, a consent task should be created on enrollment
        assertThat(participantTaskService.findByEnrolleeId(governedEnrollee1.getId()), hasSize(1));
        assertThat(participantTaskService.findByEnrolleeId(governedEnrollee2.getId()), hasSize(1));
    }

    @Test
    @Transactional
    public void testDetectingProxyWhileEnrollment(TestInfo info) {
        StudyEnvironmentBundle studyEnvBundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);
        PortalEnvironment portalEnv = studyEnvBundle.getPortalEnv();
        StudyEnvironment studyEnv = studyEnvBundle.getStudyEnv();
        EnvironmentName envName = studyEnv.getEnvironmentName();
        String studyShortcode = studyEnvBundle.getStudy().getShortcode();
        Study study = studyEnvBundle.getStudy();

        StudyEnvironmentConfig config = studyEnvironmentConfigService.find(studyEnv.getStudyEnvironmentConfigId()).get();
        config.setAcceptingProxyEnrollment(true);
        studyEnvironmentConfigService.update(config);

        Survey preEnrollmentSurvey = surveyFactory.buildPersisted(getTestName(info));
        StudyEnvironmentSurvey.builder()
                .surveyId(preEnrollmentSurvey.getId())
                .studyEnvironmentId(studyEnv.getId())
                .build();
        String proxyQuestionStableId = "proxyQuestion";
        preEnrollmentSurveyFactory.buildPersistedProxyAnswerMapping(getTestName(info), preEnrollmentSurvey.getId(), proxyQuestionStableId);
        String preEnrollmentSurveyResponseForProxy = """
                [{"questionStableId":"proxyQuestion","surveyVersion":0,"viewedLanguage":"en","stringValue":"true"},
                {"createdAt":1710527339.202811000,"lastUpdatedAt":1710527339.202811000,"questionStableId":"qualified","surveyVersion":0,"booleanValue":true}]""";
        ParticipantUserFactory.ParticipantUserAndPortalUser userProxyBundle = participantUserFactory.buildPersisted(portalEnv, getTestName(info));
        PreEnrollmentResponse preEnrollmentResponseProxy = preEnrollmentSurveyFactory.buildPersisted(getTestName(info), preEnrollmentSurvey.getId(), true, preEnrollmentSurveyResponseForProxy, userProxyBundle.ppUser().getId(), userProxyBundle.user().getId(), studyEnv.getId());
        assertTrue(enrollmentService.isProxyEnrollment(envName, studyShortcode, preEnrollmentResponseProxy.getId()));

        String preEnrollmentSurveyResponseForRegularUser = """
                [{"questionStableId": "proxyQuestion","surveyVersion":0,"viewedLanguage":"en","stringValue":"false"},
                {"createdAt":1710527339.202811000,"lastUpdatedAt":1710527339.202811000,"questionStableId":"qualified","surveyVersion":0,"booleanValue":true}]""";
        ParticipantUserFactory.ParticipantUserAndPortalUser userBundle = participantUserFactory.buildPersisted(portalEnv, getTestName(info));
        PreEnrollmentResponse preEnrollmentResponse = preEnrollmentSurveyFactory.buildPersisted(getTestName(info), preEnrollmentSurvey.getId(), true, preEnrollmentSurveyResponseForRegularUser, userBundle.ppUser().getId(), userBundle.user().getId(), studyEnv.getId());
        preEnrollmentResponseProxy.setQualified(true);
        Assertions.assertFalse(enrollmentService.isProxyEnrollment(envName, studyShortcode, preEnrollmentResponse.getId()));

        HubResponse<Enrollee> hubResponse = enrollmentService.enroll(envName, study.getShortcode(),
                userProxyBundle.user(), userProxyBundle.ppUser(), preEnrollmentResponseProxy.getId());
        // confirm that two enrollees were created, and only one is a subject
        assertThat(hubResponse.getEnrollee().isSubject(), equalTo(false));
        assertThat(hubResponse.getResponse().isSubject(), equalTo(true));
    }

    @Test
    @Transactional
    public void testNotAcceptingProxyEnrollment(TestInfo info) {
        StudyEnvironmentBundle studyEnvBundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);
        PortalEnvironment portalEnv = studyEnvBundle.getPortalEnv();
        StudyEnvironment studyEnv = studyEnvBundle.getStudyEnv();
        EnvironmentName envName = studyEnv.getEnvironmentName();
        String studyShortcode = studyEnvBundle.getStudy().getShortcode();
        Study study = studyEnvBundle.getStudy();

        StudyEnvironmentConfig config = studyEnvironmentConfigService.find(studyEnv.getStudyEnvironmentConfigId()).get();
        config.setAcceptingProxyEnrollment(false); // NOT accepting proxy enrollment
        studyEnvironmentConfigService.update(config);

        Survey preEnrollmentSurvey = surveyFactory.buildPersisted(getTestName(info));
        StudyEnvironmentSurvey.builder()
                .surveyId(preEnrollmentSurvey.getId())
                .studyEnvironmentId(studyEnv.getId())
                .build();
        String proxyQuestionStableId = "proxyQuestion";
        preEnrollmentSurveyFactory.buildPersistedProxyAnswerMapping(getTestName(info), preEnrollmentSurvey.getId(), proxyQuestionStableId);
        String preEnrollmentSurveyResponseForProxy = """
                [{"questionStableId":"proxyQuestion","surveyVersion":0,"viewedLanguage":"en","stringValue":"true"},
                {"createdAt":1710527339.202811000,"lastUpdatedAt":1710527339.202811000,"questionStableId":"qualified","surveyVersion":0,"booleanValue":true}]""";
        ParticipantUserFactory.ParticipantUserAndPortalUser userProxyBundle = participantUserFactory.buildPersisted(portalEnv, getTestName(info));
        PreEnrollmentResponse preEnrollmentResponseProxy = preEnrollmentSurveyFactory.buildPersisted(getTestName(info), preEnrollmentSurvey.getId(), true, preEnrollmentSurveyResponseForProxy, userProxyBundle.ppUser().getId(), userProxyBundle.user().getId(), studyEnv.getId());

        // stops proxy enrollment even though question exists
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> enrollmentService.isProxyEnrollment(envName, studyShortcode, preEnrollmentResponseProxy.getId()));
    }

    @Test
    @Transactional
    public void testMappingProxyAndGovernedUserProfileFromPreEnroll(TestInfo info) {
        StudyEnvironmentBundle studyEnvBundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);
        PortalEnvironment portalEnv = studyEnvBundle.getPortalEnv();
        StudyEnvironment studyEnv = studyEnvBundle.getStudyEnv();
        StudyEnvironmentConfig config = studyEnvironmentConfigService.find(studyEnv.getStudyEnvironmentConfigId()).get();
        config.setAcceptingProxyEnrollment(true);
        studyEnvironmentConfigService.update(config);
        Survey preEnrollmentSurvey = surveyFactory.buildPersisted(getTestName(info));
        StudyEnvironmentSurvey.builder()
                .surveyId(preEnrollmentSurvey.getId())
                .studyEnvironmentId(studyEnv.getId())
                .build();
        studyEnv.setPreEnrollSurveyId(preEnrollmentSurvey.getId());
        studyEnvironmentService.update(studyEnv);
        String proxyQuestionStableId = "proxyQuestion";
        preEnrollmentSurveyFactory.buildPersistedProxyAnswerMapping(getTestName(info), preEnrollmentSurvey.getId(), proxyQuestionStableId);
        answerMappingDao.create(
                AnswerMapping
                        .builder()
                        .mapType(AnswerMappingMapType.STRING_TO_STRING)
                        .targetField("givenName")
                        .targetType(AnswerMappingTargetType.PROXY_PROFILE)
                        .questionStableId("proxyGivenName")
                        .surveyId(preEnrollmentSurvey.getId())
                        .build()
        );
        answerMappingDao.create(
                AnswerMapping
                        .builder()
                        .mapType(AnswerMappingMapType.STRING_TO_STRING)
                        .targetField("familyName")
                        .targetType(AnswerMappingTargetType.PROXY_PROFILE)
                        .questionStableId("proxyFamilyName")
                        .surveyId(preEnrollmentSurvey.getId())
                        .build()
        );
        answerMappingDao.create(
                AnswerMapping
                        .builder()
                        .mapType(AnswerMappingMapType.STRING_TO_STRING)
                        .targetField("givenName")
                        .targetType(AnswerMappingTargetType.PROFILE)
                        .questionStableId("governedUserGivenName")
                        .surveyId(preEnrollmentSurvey.getId())
                        .build()
        );
        answerMappingDao.create(
                AnswerMapping
                        .builder()
                        .mapType(AnswerMappingMapType.STRING_TO_STRING)
                        .targetField("familyName")
                        .targetType(AnswerMappingTargetType.PROFILE)
                        .questionStableId("governedUserFamilyName")
                        .surveyId(preEnrollmentSurvey.getId())
                        .build()
        );
        String preEnrollmentSurveyResponseForProxy = """
                [
                {"questionStableId":"proxyQuestion","surveyVersion":0,"viewedLanguage":"en","stringValue":"true"},
                {"createdAt":1710527339.202811000,"lastUpdatedAt":1710527339.202811000,"questionStableId":"qualified","surveyVersion":0,"booleanValue":true},
                {"questionStableId":"proxyGivenName","surveyVersion":0,"viewedLanguage":"en","stringValue":"John"},
                {"questionStableId":"proxyFamilyName","surveyVersion":0,"viewedLanguage":"en","stringValue":"Doe"},
                {"questionStableId":"governedUserGivenName","surveyVersion":0,"viewedLanguage":"en","stringValue":"Emily"},
                {"questionStableId":"governedUserFamilyName","surveyVersion":0,"viewedLanguage":"en","stringValue":"Smith"}
                ]""";
        ParticipantUserFactory.ParticipantUserAndPortalUser userProxyBundle = participantUserFactory.buildPersisted(portalEnv, getTestName(info));
        PreEnrollmentResponse preEnrollmentResponseProxy = preEnrollmentSurveyFactory.buildPersisted(getTestName(info), preEnrollmentSurvey.getId(), true, preEnrollmentSurveyResponseForProxy, userProxyBundle.ppUser().getId(), userProxyBundle.user().getId(), studyEnv.getId());

        Study study = studyService.find(studyEnv.getStudyId()).get();
        HubResponse<Enrollee> hubResponse = enrollmentService.enroll(
                studyEnv.getEnvironmentName(),
                study.getShortcode(),
                userProxyBundle.user(),
                userProxyBundle.ppUser(),
                preEnrollmentResponseProxy.getId());
        // confirm that two enrollees were created, and only one is a subject
        assertThat(hubResponse.getEnrollee().isSubject(), equalTo(false));
        assertThat(hubResponse.getResponse().isSubject(), equalTo(true));
        Profile governedProfile = profileService.find(hubResponse.getResponse().getProfileId()).get();
        Profile proxyProfile = profileService.find(hubResponse.getEnrollee().getProfileId()).get();

        assertThat(governedProfile.getGivenName(), equalTo("Emily"));
        assertThat(governedProfile.getFamilyName(), equalTo("Smith"));
        assertThat(proxyProfile.getGivenName(), equalTo("John"));
        assertThat(proxyProfile.getFamilyName(), equalTo("Doe"));

    }

    @Test
    @Transactional
    public void testDetectingProxyNoPreEnroll(TestInfo info) {
        StudyEnvironmentBundle studyEnvBundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);
        StudyEnvironment studyEnv = studyEnvBundle.getStudyEnv();
        EnvironmentName envName = studyEnv.getEnvironmentName();
        String studyShortcode = studyEnvBundle.getStudy().getShortcode();

        Assertions.assertFalse(enrollmentService.isProxyEnrollment(envName, studyShortcode, null));
        Assertions.assertFalse(enrollmentService.isProxyEnrollment(envName, studyShortcode, UUID.randomUUID()));
    }

    public Map<EventClass, List<Event>> getEventsByType(UUID enrolleeId) {
        return eventService.findAllEventsByEnrolleeId(enrolleeId).stream()
                .collect(Collectors.toMap(Event::getEventClass, List::of, (a, b) -> Stream.of(a, b).flatMap(List::stream).toList()));
    }



}
