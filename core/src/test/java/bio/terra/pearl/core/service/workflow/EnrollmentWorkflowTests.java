package bio.terra.pearl.core.service.workflow;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.consent.ConsentFormFactory;
import bio.terra.pearl.core.factory.participant.ParticipantUserFactory;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.factory.survey.AnswerFactory;
import bio.terra.pearl.core.factory.survey.PreEnrollmentSurveyFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.model.consent.ConsentForm;
import bio.terra.pearl.core.model.consent.ConsentResponseDto;
import bio.terra.pearl.core.model.consent.StudyEnvironmentConsent;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.EnrolleeRelation;
import bio.terra.pearl.core.model.participant.RelationshipType;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.PreEnrollmentResponse;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.survey.SurveyResponse;
import bio.terra.pearl.core.model.workflow.HubResponse;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.consent.ConsentResponseService;
import bio.terra.pearl.core.service.participant.EnrolleeRelationService;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.study.StudyEnvironmentConsentService;
import bio.terra.pearl.core.service.study.StudyEnvironmentSurveyService;
import bio.terra.pearl.core.service.study.StudyService;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.samePropertyValuesAs;

/** class for high-level tests of workflow operations -- enroll, consent, etc... */
public class EnrollmentWorkflowTests extends BaseSpringBootTest {
    @Test
    @Transactional
    public void testEnroll(TestInfo info) {
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(getTestName(info));
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, getTestName(info));
        ParticipantUserFactory.ParticipantUserAndPortalUser userBundle = participantUserFactory.buildPersisted(portalEnv, getTestName(info));
        String studyShortcode = studyService.find(studyEnv.getStudyId()).get().getShortcode();

        ConsentForm consent = consentFormFactory.buildPersisted(getTestName(info));
        StudyEnvironmentConsent studyEnvConsent = StudyEnvironmentConsent.builder()
                .consentFormId(consent.getId())
                .studyEnvironmentId(studyEnv.getId())
                .build();
        studyEnvironmentConsentService.create(studyEnvConsent);

        String portalShortcode = portalService.find(portalEnv.getPortalId()).get().getShortcode();

        HubResponse hubResponse = enrollmentService.enroll(studyEnv.getEnvironmentName(), studyShortcode,
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
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(getTestName(info));
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, getTestName(info));
        ParticipantUserFactory.ParticipantUserAndPortalUser userBundle = participantUserFactory.buildPersisted(portalEnv, getTestName(info));


        ConsentForm consent = consentFormFactory.buildPersisted(getTestName(info));
        StudyEnvironmentConsent studyEnvConsent = StudyEnvironmentConsent.builder()
                .consentFormId(consent.getId())
                .studyEnvironmentId(studyEnv.getId())
                .build();
        studyEnvironmentConsentService.create(studyEnvConsent);

        Survey survey = surveyFactory.buildPersisted(getTestName(info));
        StudyEnvironmentSurvey studyEnvSurvey = StudyEnvironmentSurvey.builder()
                .surveyId(survey.getId())
                .studyEnvironmentId(studyEnv.getId())
                .build();
        studyEnvironmentSurveyService.create(studyEnvSurvey);

        String portalShortcode = portalService.find(portalEnv.getPortalId()).get().getShortcode();

        String studyShortcode = studyService.find(studyEnv.getStudyId()).get().getShortcode();
        HubResponse hubResponse = enrollmentService.enroll(studyEnv.getEnvironmentName(), studyShortcode,
                userBundle.user(), userBundle.ppUser(), null, true);
        Enrollee enrollee = hubResponse.getEnrollee();
        assertThat(hubResponse.getProfile(), notNullValue());
        // Because the study environment had a consent attached, a consent task should be created on enrollment
        assertThat(enrollee.getParticipantTasks(), hasSize(2));
        ParticipantTask consentTask = enrollee.getParticipantTasks().stream()
                .filter(task -> task.getTaskType().equals(TaskType.CONSENT))
                .findFirst().get();
        assertThat(enrollee.isConsented(), equalTo(false));

        ConsentResponseDto responseDto = ConsentResponseDto.builder()
                        .consented(true)
                        .consentFormId(consent.getId())
                        .fullData("{\"items\": []}")
                        .build();
        consentResponseService.submitResponse(userBundle.user().getId(), userBundle.ppUser(),
            enrollee, responseDto);

        Enrollee refreshedEnrollee = enrolleeService.find(enrollee.getId()).get();
        assertThat(refreshedEnrollee.isConsented(), equalTo(true));

        List<ParticipantTask> tasks = participantTaskService.findByEnrolleeId(enrollee.getId());
        assertThat(tasks, hasSize(2));
        List<ParticipantTask> surveyTasks = tasks.stream().filter(task -> task.getTaskType().equals(TaskType.SURVEY)).toList();
        assertThat(surveyTasks, hasSize(1));
        assertThat(surveyTasks.get(0).getTargetStableId(), equalTo(survey.getStableId()));

        // now try to complete the survey
        SurveyResponse survResponseDto = SurveyResponse.builder()
                        .answers(AnswerFactory.fromMap(Map.of("sampleQuestion", "foo")))
                        .complete(true)
                        .resumeData("stuff")
                        .build();
        hubResponse = surveyResponseService.updateResponse(survResponseDto, userBundle.user().getId(), userBundle.ppUser(),
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
    public void testGovernedUserEnrollment(TestInfo testInfo){
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(getTestName(testInfo));
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, getTestName(testInfo));
        ParticipantUserFactory.ParticipantUserAndPortalUser userBundle = participantUserFactory.buildPersisted(portalEnv,getTestName(testInfo));
        String studyShortcode = studyService.find(studyEnv.getStudyId()).get().getShortcode();
        String portalShortcode = portalService.find(portalEnv.getPortalId()).get().getShortcode();

        ConsentForm consent = consentFormFactory.buildPersisted(getTestName(testInfo));
        StudyEnvironmentConsent studyEnvConsent = StudyEnvironmentConsent.builder()
                .consentFormId(consent.getId())
                .studyEnvironmentId(studyEnv.getId())
                .build();
        studyEnvironmentConsentService.create(studyEnvConsent);

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
    public void testProxyEnrollingMultipleChild(TestInfo testInfo){
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(getTestName(testInfo));
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, getTestName(testInfo));
        ParticipantUserFactory.ParticipantUserAndPortalUser userBundle = participantUserFactory.buildPersisted(portalEnv,getTestName(testInfo));
        String studyShortcode = studyService.find(studyEnv.getStudyId()).get().getShortcode();

        ConsentForm consent = consentFormFactory.buildPersisted(getTestName(testInfo));
        StudyEnvironmentConsent studyEnvConsent = StudyEnvironmentConsent.builder()
                .consentFormId(consent.getId())
                .studyEnvironmentId(studyEnv.getId())
                .build();
        studyEnvironmentConsentService.create(studyEnvConsent);

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
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(getTestName(info));
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, getTestName(info));
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
        Assertions.assertTrue(enrollmentService.isProxyEnrollment(preEnrollmentResponseProxy.getId()));

        String preEnrollmentSurveyResponseForRegularUser = """
                [{"questionStableId": "proxyQuestion","surveyVersion":0,"viewedLanguage":"en","stringValue":"false"},
                {"createdAt":1710527339.202811000,"lastUpdatedAt":1710527339.202811000,"questionStableId":"qualified","surveyVersion":0,"booleanValue":true}]""";
        ParticipantUserFactory.ParticipantUserAndPortalUser userBundle = participantUserFactory.buildPersisted(portalEnv, getTestName(info));
        PreEnrollmentResponse preEnrollmentResponse = preEnrollmentSurveyFactory.buildPersisted(getTestName(info), preEnrollmentSurvey.getId(), true, preEnrollmentSurveyResponseForRegularUser, userBundle.ppUser().getId(), userBundle.user().getId(), studyEnv.getId());
        preEnrollmentResponseProxy.setQualified(true);
        Assertions.assertFalse(enrollmentService.isProxyEnrollment(preEnrollmentResponse.getId()));

        Study study = studyService.find(studyEnv.getStudyId()).get();
        HubResponse<Enrollee> hubResponse = enrollmentService.enroll(studyEnv.getEnvironmentName(), study.getShortcode(),
                userProxyBundle.user(), userProxyBundle.ppUser(), preEnrollmentResponseProxy.getId());
        // confirm that two enrollees were created, and only one is a subject
        assertThat(hubResponse.getEnrollee().isSubject(), equalTo(false));
        assertThat(hubResponse.getResponse().isSubject(), equalTo(true));


    }

    @Test
    @Transactional
    public void testDetectingProxyNoPreEnroll(TestInfo info) {
        Assertions.assertFalse(enrollmentService.isProxyEnrollment(null));
        Assertions.assertFalse(enrollmentService.isProxyEnrollment(UUID.randomUUID()));
    }


    @Autowired
    private StudyEnvironmentFactory studyEnvironmentFactory;
    @Autowired
    private ParticipantUserFactory participantUserFactory;
    @Autowired
    private StudyService studyService;
    @Autowired
    private PortalService portalService;
    @Autowired
    private EnrolleeService enrolleeService;

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private EnrolleeRelationService enrolleeRelationService;

    @Autowired
    private PortalEnvironmentFactory portalEnvironmentFactory;
    @Autowired
    private ConsentFormFactory consentFormFactory;
    @Autowired
    private SurveyFactory surveyFactory;
    @Autowired
    private StudyEnvironmentConsentService studyEnvironmentConsentService;
    @Autowired
    private StudyEnvironmentSurveyService studyEnvironmentSurveyService;
    @Autowired
    private ParticipantTaskService participantTaskService;
    @Autowired
    private ConsentResponseService consentResponseService;
    @Autowired
    private SurveyResponseService surveyResponseService;
    @Autowired
    private PreEnrollmentSurveyFactory preEnrollmentSurveyFactory;
}
