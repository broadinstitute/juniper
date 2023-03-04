package bio.terra.pearl.core.service.workflow;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.PortalEnvironmentFactory;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.consent.ConsentFormFactory;
import bio.terra.pearl.core.factory.participant.ParticipantUserFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.model.consent.ConsentForm;
import bio.terra.pearl.core.model.consent.ConsentResponseDto;
import bio.terra.pearl.core.model.consent.StudyEnvironmentConsent;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.ResponseSnapshotDto;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.workflow.HubResponse;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.consent.ConsentResponseService;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.ParticipantTaskService;
import bio.terra.pearl.core.service.study.StudyEnvironmentConsentService;
import bio.terra.pearl.core.service.study.StudyEnvironmentSurveyService;
import bio.terra.pearl.core.service.study.StudyService;
import bio.terra.pearl.core.service.survey.SurveyResponseService;
import java.util.List;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
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

        HubResponse hubResponse = enrollmentService.enroll(userBundle.user(), userBundle.ppUser(),
                studyEnv.getEnvironmentName(), studyShortcode, null);
        Enrollee enrollee = hubResponse.getEnrollee();
        assertThat(enrollee.getShortcode(), notNullValue());
        assertThat(enrollee.getParticipantUserId(), equalTo(userBundle.user().getId()));

        assertThat(enrolleeService.findByStudyEnvironmentAdminLoad(studyEnv.getId()), contains(enrollee));

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

        String studyShortcode = studyService.find(studyEnv.getStudyId()).get().getShortcode();
        HubResponse hubResponse = enrollmentService.enroll(userBundle.user(), userBundle.ppUser(),
                studyEnv.getEnvironmentName(), studyShortcode, null);
        Enrollee enrollee = hubResponse.getEnrollee();
        // Because the study environment had a consent attached, a consent task should be created on enrollment
        assertThat(enrollee.getParticipantTasks(), hasSize(2));
        ParticipantTask consentTask = enrollee.getParticipantTasks().stream()
                .filter(task -> task.getTaskType().equals(TaskType.CONSENT))
                .findFirst().get();
        assertThat(enrollee.isConsented(), equalTo(false));

        ConsentResponseDto responseDto = ConsentResponseDto.builder()
                        .consented(true)
                        .consentFormId(consent.getId())
                        .fullData("{\"foo\": 1}")
                        .build();
        consentResponseService.submitResponse(userBundle.user().getId(), userBundle.ppUser(),
            enrollee.getShortcode(), consentTask.getId(),  responseDto);

        Enrollee refreshedEnrollee = enrolleeService.find(enrollee.getId()).get();
        assertThat(refreshedEnrollee.isConsented(), equalTo(true));

        List<ParticipantTask> tasks = participantTaskService.findByEnrolleeId(enrollee.getId());
        assertThat(tasks, hasSize(2));
        List<ParticipantTask> surveyTasks = tasks.stream().filter(task -> task.getTaskType().equals(TaskType.SURVEY)).toList();
        assertThat(surveyTasks, hasSize(1));
        assertThat(surveyTasks.get(0).getTargetStableId(), equalTo(survey.getStableId()));

        // now try to complete the survey
        ResponseSnapshotDto snapDto = ResponseSnapshotDto.builder()
                        .fullData("{ \"items\": [{ \"simpleValue\": 1, \"stableId\": \"foo\"]}")
                        .complete(true)
                        .resumeData("stuff")
                        .build();
        hubResponse = surveyResponseService.submitResponse(userBundle.user().getId(), userBundle.ppUser(),
                enrollee.getShortcode(), surveyTasks.get(0).getId(), snapDto);
        List<ParticipantTask> updatedTasks = participantTaskService.findByEnrolleeId(enrollee.getId());
        assertThat(updatedTasks, containsInAnyOrder(hubResponse.getTasks().toArray()));
        List<ParticipantTask>  updateSurveyTasks = updatedTasks.stream().filter(task -> task.getTaskType().equals(TaskType.SURVEY)).toList();
        assertThat(updatedTasks, hasSize(2));
        assertThat(updateSurveyTasks, hasSize(1));
        assertThat(updateSurveyTasks.get(0).getStatus(), equalTo(TaskStatus.COMPLETE));
    }



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
