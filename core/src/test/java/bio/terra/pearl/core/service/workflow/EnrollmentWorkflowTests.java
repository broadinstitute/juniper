package bio.terra.pearl.core.service.workflow;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.consent.ConsentFormFactory;
import bio.terra.pearl.core.factory.participant.ParticipantUserFactory;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.factory.survey.AnswerFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.model.consent.ConsentForm;
import bio.terra.pearl.core.model.consent.ConsentResponseDto;
import bio.terra.pearl.core.model.consent.StudyEnvironmentConsent;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.RelationshipType;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironment;
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
import java.time.Instant;
import java.util.List;
import java.util.Map;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/** class for high-level tests of workflow operations -- enroll, consent, etc... */
public class EnrollmentWorkflowTests extends BaseSpringBootTest {
    @Test
    @Transactional
    public void testEnroll() {
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted("testEnroll");
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, "testEnroll");
        ParticipantUserFactory.ParticipantUserAndPortalUser userBundle = participantUserFactory.buildPersisted(portalEnv,"testEnroll");
        String studyShortcode = studyService.find(studyEnv.getStudyId()).get().getShortcode();

        ConsentForm consent = consentFormFactory.buildPersisted("testEnroll");
        StudyEnvironmentConsent studyEnvConsent = StudyEnvironmentConsent.builder()
                .consentFormId(consent.getId())
                .studyEnvironmentId(studyEnv.getId())
                .build();
        studyEnvironmentConsentService.create(studyEnvConsent);

        String portalShortcode = portalService.find(portalEnv.getPortalId()).get().getShortcode();

        HubResponse hubResponse = enrollmentService.enroll(portalShortcode, userBundle.user(), userBundle.ppUser(),
                studyEnv.getEnvironmentName(), studyShortcode, null, false);
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
    public void testParticipantWorkflow() {
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted("testEnrollAndConsent");
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, "testEnrollAndConsent");
        ParticipantUserFactory.ParticipantUserAndPortalUser userBundle = participantUserFactory.buildPersisted(portalEnv,"testEnrollAndConsent");


        ConsentForm consent = consentFormFactory.buildPersisted("testEnrollAndConsent");
        StudyEnvironmentConsent studyEnvConsent = StudyEnvironmentConsent.builder()
                .consentFormId(consent.getId())
                .studyEnvironmentId(studyEnv.getId())
                .build();
        studyEnvironmentConsentService.create(studyEnvConsent);

        Survey survey = surveyFactory.buildPersisted("testEnrollAndConsent");
        StudyEnvironmentSurvey studyEnvSurvey = StudyEnvironmentSurvey.builder()
                .surveyId(survey.getId())
                .studyEnvironmentId(studyEnv.getId())
                .build();
        studyEnvironmentSurveyService.create(studyEnvSurvey);

        String portalShortcode = portalService.find(portalEnv.getPortalId()).get().getShortcode();

        String studyShortcode = studyService.find(studyEnv.getStudyId()).get().getShortcode();
        HubResponse hubResponse = enrollmentService.enroll(portalShortcode, userBundle.user(), userBundle.ppUser(),
                studyEnv.getEnvironmentName(), studyShortcode, null, false);
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
                enrollee, surveyTasks.get(0).getId());
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
    public void testGovernedUserEnrollment(){
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted("testGovernedUserEnrollment");
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, "testGovernedUserEnrollment");
        ParticipantUserFactory.ParticipantUserAndPortalUser userBundle = participantUserFactory.buildPersisted(portalEnv,"testGovernedUserEnrollment");
        String studyShortcode = studyService.find(studyEnv.getStudyId()).get().getShortcode();

        String portalShortcode = portalService.find(portalEnv.getPortalId()).get().getShortcode();

        ConsentForm consent = consentFormFactory.buildPersisted("testGovernedUserEnrollment");
        StudyEnvironmentConsent studyEnvConsent = StudyEnvironmentConsent.builder()
                .consentFormId(consent.getId())
                .studyEnvironmentId(studyEnv.getId())
                .build();
        studyEnvironmentConsentService.create(studyEnvConsent);

        HubResponse hubResponse = enrollmentService.enroll(portalShortcode, userBundle.user(), userBundle.ppUser(),
                studyEnv.getEnvironmentName(), studyShortcode, null, true);
        Enrollee enrollee = hubResponse.getEnrollee();
        assertThat(enrollee.getShortcode(), notNullValue());
        Assertions.assertNotEquals(enrollee.getParticipantUserId(),userBundle.user().getId());
        Assertions.assertEquals(enrolleeService.findByPortalParticipantUser(userBundle.ppUser()).size(), 0);
        Assertions.assertEquals(enrolleeRelationService.findByParticipantUserIdAndPortalId(userBundle.user().getId(), portalEnv.getPortalId()).size(), 1);
        Assertions.assertEquals(enrolleeRelationService.findByParticipantUserIdAndPortalId(userBundle.user().getId(), portalEnv.getPortalId()).get(0).getRelationshipType(), RelationshipType.PROXY);
        Assertions.assertEquals(enrolleeRelationService.findByParticipantUserIdAndPortalId(userBundle.user().getId(), portalEnv.getPortalId()).get(0).getEnrolleeId(), enrollee.getId());
        assertThat(enrolleeService.findByStudyEnvironment(studyEnv.getId()), contains(enrollee));

        // Because the study environment had a consent attached, a consent task should be created on enrollment
        assertThat(enrollee.getParticipantTasks(), hasSize(1));
        assertThat(enrollee.getParticipantTasks(), contains(hasProperty("taskType", equalTo(TaskType.CONSENT))));
        assertThat(enrollee.isConsented(), equalTo(false));
    }

    @Test
    @Transactional
    public void testProxyEnrollingMultipleChild(){
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted("testProxyEnrollingMultipleChild");
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, "testProxyEnrollingMultipleChild");
        ParticipantUserFactory.ParticipantUserAndPortalUser userBundle = participantUserFactory.buildPersisted(portalEnv,"testProxyEnrollingMultipleChild");
        String studyShortcode = studyService.find(studyEnv.getStudyId()).get().getShortcode();

        String portalShortcode = portalService.find(portalEnv.getPortalId()).get().getShortcode();

        ConsentForm consent = consentFormFactory.buildPersisted("testProxyEnrollingMultipleChild");
        StudyEnvironmentConsent studyEnvConsent = StudyEnvironmentConsent.builder()
                .consentFormId(consent.getId())
                .studyEnvironmentId(studyEnv.getId())
                .build();
        studyEnvironmentConsentService.create(studyEnvConsent);

        HubResponse hubResponse1 = enrollmentService.enroll(portalShortcode, userBundle.user(), userBundle.ppUser(),
                studyEnv.getEnvironmentName(), studyShortcode, null, true);

        HubResponse hubResponse2 = enrollmentService.enroll(portalShortcode, userBundle.user(), userBundle.ppUser(),
                studyEnv.getEnvironmentName(), studyShortcode, null, true);
        Enrollee enrollee1 = hubResponse1.getEnrollee();
        Enrollee enrollee2 = hubResponse2.getEnrollee();
        assertThat(enrollee1.getShortcode(), notNullValue());
        assertThat(enrollee2.getShortcode(), notNullValue());
        Assertions.assertNotEquals(enrollee1.getShortcode(), enrollee2.getShortcode());
        Assertions.assertNotEquals(enrollee1.getParticipantUserId(), enrollee2.getParticipantUserId());
        Assertions.assertNotEquals(enrollee1.getParticipantUserId(), userBundle.user().getId());
        Assertions.assertEquals(enrolleeService.findByPortalParticipantUser(userBundle.ppUser()).size(), 0);
        Assertions.assertEquals(enrolleeRelationService.findByParticipantUserIdAndPortalId(userBundle.user().getId(), portalEnv.getPortalId()).size(), 2);
        Assertions.assertEquals(enrolleeRelationService.findByParticipantUserIdAndPortalId(userBundle.user().getId(), portalEnv.getPortalId()).get(0).getRelationshipType(), RelationshipType.PROXY);
        Assertions.assertEquals(enrolleeRelationService.findByParticipantUserIdAndPortalId(userBundle.user().getId(), portalEnv.getPortalId()).get(0).getEnrolleeId(), enrollee1.getId());
        Assertions.assertEquals(enrolleeRelationService.findByParticipantUserIdAndPortalId(userBundle.user().getId(), portalEnv.getPortalId()).get(1).getEnrolleeId(), enrollee2.getId());
        Assertions.assertEquals(enrolleeService.findByStudyEnvironment(studyEnv.getId()).size(), 2);
        Assertions.assertTrue(enrolleeService.findByStudyEnvironment(studyEnv.getId()).stream().anyMatch(enrollee -> enrollee.equals(enrollee1)));
        Assertions.assertTrue(enrolleeService.findByStudyEnvironment(studyEnv.getId()).stream().anyMatch(enrollee -> enrollee.equals(enrollee2)));

        // Because the study environment had a consent attached, a consent task should be created on enrollment
        assertThat(enrollee1.getParticipantTasks(), hasSize(1));
        assertThat(enrollee2.getParticipantTasks(), hasSize(1));
        assertThat(enrollee1.getParticipantTasks(), contains(hasProperty("taskType", equalTo(TaskType.CONSENT))));
        assertThat(enrollee2.getParticipantTasks(), contains(hasProperty("taskType", equalTo(TaskType.CONSENT))));
        assertThat(enrollee1.isConsented(), equalTo(false));
        assertThat(enrollee2.isConsented(), equalTo(false));
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
}
