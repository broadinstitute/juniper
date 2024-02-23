package bio.terra.pearl.core.service.publishing;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.dao.study.StudyEnvironmentSurveyDao;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.StudyFactory;
import bio.terra.pearl.core.factory.consent.ConsentFormFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.participant.ParticipantTaskFactory;
import bio.terra.pearl.core.factory.portal.PortalFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.audit.DataChangeRecord;
import bio.terra.pearl.core.model.consent.ConsentForm;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.publishing.StudyEnvironmentChange;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.workflow.Event;
import bio.terra.pearl.core.model.workflow.EventClass;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.service.consent.ConsentFormService;
import bio.terra.pearl.core.service.study.StudyEnvironmentConfigService;
import bio.terra.pearl.core.service.study.StudyEnvironmentConsentService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.study.StudyEnvironmentSurveyService;
import bio.terra.pearl.core.service.survey.SurveyService;
import bio.terra.pearl.core.service.workflow.EventService;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class StudyPublishingServiceTests extends BaseSpringBootTest {
    @Test
    @Transactional
    public void testApplyStudyConfigChanges(TestInfo info) throws Exception {
        String testName = getTestName(info);
        Study study = studyFactory.buildPersisted(testName);
        StudyEnvironment irbEnv = studyEnvironmentFactory.buildPersisted(EnvironmentName.irb, study.getId(), testName);
        StudyEnvironment liveEnv = studyEnvironmentFactory.buildPersisted(EnvironmentName.live, study.getId(), testName);

        StudyEnvironmentConfig irbConfig = studyEnvironmentConfigService.find(irbEnv.getStudyEnvironmentConfigId()).get();
        irbConfig.setPassword("foobar");
        irbConfig.setPasswordProtected(true);
        studyEnvironmentConfigService.update(irbConfig);

        diffAndApplyChanges(study.getShortcode(), EnvironmentName.irb, EnvironmentName.live);

        StudyEnvironmentConfig liveConfig = studyEnvironmentConfigService.find(liveEnv.getStudyEnvironmentConfigId()).get();
        assertThat(liveConfig.getPassword(), equalTo("foobar"));
        assertThat(liveConfig.isPasswordProtected(), equalTo(true));
    }

    @Test
    @Transactional
    public void testApplyChangesPublishSurvey(TestInfo testInfo) throws Exception {
        String testName = getTestName(testInfo);
        Study study = studyFactory.buildPersisted(testName);
        StudyEnvironment irbEnv = studyEnvironmentFactory.buildPersisted(EnvironmentName.irb, study.getId(), testName);
        StudyEnvironment liveEnv = studyEnvironmentFactory.buildPersisted(EnvironmentName.live, study.getId(), testName);
        Survey survey = surveyFactory.buildPersisted(testName);

        irbEnv.setPreEnrollSurveyId(survey.getId());
        studyEnvironmentService.update(irbEnv);

        diffAndApplyChanges(study.getShortcode(), EnvironmentName.irb, EnvironmentName.live);

        liveEnv = studyEnvironmentService.find(liveEnv.getId()).get();
        assertThat(liveEnv.getPreEnrollSurveyId(), equalTo(survey.getId()));
        survey = surveyService.find(survey.getId()).get();
        assertThat(survey.getPublishedVersion(), equalTo(1));
    }

    @Test
    @Transactional
    public void testApplyChangesPublishSurveyConfig(TestInfo testInfo) throws Exception {
        String testName = getTestName(testInfo);
        Study study = studyFactory.buildPersisted(testName);
        StudyEnvironment irbEnv = studyEnvironmentFactory.buildPersisted(EnvironmentName.irb, study.getId(), testName);
        StudyEnvironment liveEnv = studyEnvironmentFactory.buildPersisted(EnvironmentName.live, study.getId(), testName);
        Survey survey = surveyFactory.buildPersisted(testName);
        StudyEnvironmentSurvey surveyConfig = surveyFactory.attachToEnv(survey, irbEnv.getId(), true);
        diffAndApplyChanges(study.getShortcode(), EnvironmentName.irb, EnvironmentName.live);

        List<StudyEnvironmentSurvey> liveSurveys = studyEnvironmentSurveyService.findAllByStudyEnvIdWithSurvey(liveEnv.getId());
        assertThat(liveSurveys, hasSize(1));
        assertThat(liveSurveys.get(0).getSurveyId(), equalTo(survey.getId()));
        //confirm that an event was published
        List<Event> events = eventService.findAllByStudyEnvAndClass(liveEnv.getId(), EventClass.SURVEY_PUBLISHED_EVENT);
        assertThat(events, hasSize(1));
        assertThat(events.get(0), samePropertyValuesAs(Event.builder()
                .surveyId(survey.getId())
                .studyEnvironmentId(liveEnv.getId())
                .eventClass(EventClass.SURVEY_PUBLISHED_EVENT).build(),
                "id", "createdAt", "lastUpdatedAt"));

        // now test that we can publish a removal
        studyEnvironmentSurveyService.deactivate(surveyConfig.getId());
        diffAndApplyChanges(study.getShortcode(), EnvironmentName.irb, EnvironmentName.live);

        liveSurveys = studyEnvironmentSurveyService.findAllByStudyEnvIdWithSurvey(liveEnv.getId());
        assertThat(liveSurveys, hasSize(0));
        // confirm the deactivated config is still there
        assertThat(studyEnvironmentSurveyDao.findAll(List.of(liveEnv.getId()), null, false), hasSize(1));
    }

    @Test
    @Transactional
    public void testApplyChangesPublishSurveyUpdate(TestInfo testInfo) throws Exception {
        String testName = getTestName(testInfo);
        StudyEnvironmentFactory.StudyEnvironmentBundle sandboxBundle = studyEnvironmentFactory.buildBundle(testName, EnvironmentName.sandbox);
        StudyEnvironmentFactory.StudyEnvironmentBundle irbBundle = studyEnvironmentFactory.buildBundle(testName, EnvironmentName.irb,
                sandboxBundle.getPortal(), sandboxBundle.getStudy());

        // attach v1 to irb, and v2 in sandbox, then try copying sandbox to irb.
        Survey survey = surveyFactory.buildPersisted(testName);
        surveyFactory.attachToEnv(survey, irbBundle.getStudyEnv().getId(), true);
        survey.setAutoUpdateTaskAssignments(true);
        Survey surveyV2 = surveyService.createNewVersion(sandboxBundle.getPortal().getId(), survey);
        surveyFactory.attachToEnv(surveyV2, sandboxBundle.getStudyEnv().getId(), true);

        // assign v1 of the survey to an enrollee in the irb environment
        EnrolleeFactory.EnrolleeBundle irbEnrollee = enrolleeFactory.buildWithPortalUser(testName, irbBundle.getPortalEnv(), irbBundle.getStudyEnv());
        participantTaskFactory.buildPersisted(irbEnrollee, ParticipantTaskFactory.DEFAULT_BUILDER
                .targetStableId(survey.getStableId())
                .targetAssignedVersion(survey.getVersion()));

        // after publishing, the irb should have the new version, and the task should be updated too
        diffAndApplyChanges(sandboxBundle.getStudy().getShortcode(), EnvironmentName.sandbox, EnvironmentName.irb);

        List<StudyEnvironmentSurvey> irbSurveys = studyEnvironmentSurveyService.findAllByStudyEnvIdWithSurvey(irbBundle.getStudyEnv().getId());
        assertThat(irbSurveys, hasSize(1));
        assertThat(irbSurveys.get(0).getSurveyId(), equalTo(surveyV2.getId()));

        // confirm the task version got updated too
        List<ParticipantTask> tasks = participantTaskService.findByEnrolleeId(irbEnrollee.enrollee().getId());
        assertThat(tasks, hasSize(1));
        assertThat(tasks.get(0).getTargetAssignedVersion(), equalTo(2));
    }

    @Test
    @Transactional
    public void testPublishSurveyAssignExisting(TestInfo testInfo) throws Exception {
        String testName = getTestName(testInfo);
        StudyEnvironmentFactory.StudyEnvironmentBundle sandboxBundle = studyEnvironmentFactory.buildBundle(testName, EnvironmentName.sandbox);
        StudyEnvironmentFactory.StudyEnvironmentBundle irbBundle = studyEnvironmentFactory.buildBundle(testName, EnvironmentName.irb,
                sandboxBundle.getPortal(), sandboxBundle.getStudy());
        Survey survey = surveyFactory.buildPersisted(testName);
        Survey autoAssignSurvey = surveyFactory.buildPersisted(surveyFactory.builder(getTestName(testInfo)).assignToExistingEnrollees(true));

        // create an enrollee in the irb environment with no survey tasks
        EnrolleeFactory.EnrolleeBundle irbEnrollee = enrolleeFactory.buildWithPortalUser(testName, irbBundle.getPortalEnv(), irbBundle.getStudyEnv());

        // attach surveys to sandbox, then copy sandbox to irb
        surveyFactory.attachToEnv(survey, sandboxBundle.getStudyEnv().getId(), true);
        surveyFactory.attachToEnv(autoAssignSurvey, sandboxBundle.getStudyEnv().getId(), true);

        // after publishing, the irb should have the surveys, and the enrollee should have a task for the auto-assigned survey
        diffAndApplyChanges(sandboxBundle.getStudy().getShortcode(), EnvironmentName.sandbox, EnvironmentName.irb);

        List<StudyEnvironmentSurvey> irbSurveys = studyEnvironmentSurveyService.findAllByStudyEnvIdWithSurvey(irbBundle.getStudyEnv().getId());
        assertThat(irbSurveys, hasSize(2));
        // confirm the task version got updated too
        List<ParticipantTask> tasks = participantTaskService.findByEnrolleeId(irbEnrollee.enrollee().getId());
        assertThat(tasks, hasSize(1));
        assertThat(tasks.get(0).getTargetStableId(), equalTo(autoAssignSurvey.getStableId()));
    }


    @Test
    @Transactional
    public void testApplyChangesConsents(TestInfo info) throws Exception {
        String testName = getTestName(info);
        Study study = studyFactory.buildPersisted(testName);
        StudyEnvironment irbEnv = studyEnvironmentFactory.buildPersisted(EnvironmentName.irb, study.getId(), testName);
        StudyEnvironment liveEnv = studyEnvironmentFactory.buildPersisted(EnvironmentName.live, study.getId(), testName);
        ConsentForm form = consentFormFactory.buildPersisted(testName);
        consentFormFactory.addConsentToStudyEnv(irbEnv.getId(), form.getId());

        diffAndApplyChanges(study.getShortcode(), EnvironmentName.irb, EnvironmentName.live);

        assertThat(studyEnvironmentConsentService.findByConsentForm(liveEnv.getId(), form.getId()).isPresent(), equalTo(true));

        form = consentFormService.find(form.getId()).get();
        assertThat(form.getPublishedVersion(), equalTo(1));
    }

    private void diffAndApplyChanges(String studyShortcode, EnvironmentName src, EnvironmentName dest) throws Exception {
        StudyEnvironmentChange changes = portalDiffService.diffStudyEnvs(studyShortcode, src, dest);
        StudyEnvironment loadedLiveEnv = portalDiffService.loadStudyEnvForProcessing(studyShortcode, dest);
        studyPublishingService.applyChanges(loadedLiveEnv, changes, null);
    }

    @Autowired
    private StudyFactory studyFactory;
    @Autowired
    private StudyPublishingService studyPublishingService;
    @Autowired
    private StudyEnvironmentFactory studyEnvironmentFactory;
    @Autowired
    private StudyEnvironmentService studyEnvironmentService;
    @Autowired
    private StudyEnvironmentConfigService studyEnvironmentConfigService;
    @Autowired
    private StudyEnvironmentConsentService studyEnvironmentConsentService;
    @Autowired
    private StudyEnvironmentSurveyService studyEnvironmentSurveyService;
    @Autowired
    private SurveyService surveyService;
    @Autowired
    private ConsentFormFactory consentFormFactory;
    @Autowired
    private ConsentFormService consentFormService;
    @Autowired
    private SurveyFactory surveyFactory;
    @Autowired
    private PortalDiffService portalDiffService;
    @Autowired
    private StudyEnvironmentSurveyDao studyEnvironmentSurveyDao;
    @Autowired
    private EventService eventService;
    @Autowired
    private PortalFactory portalFactory;
    @Autowired
    private ParticipantTaskFactory participantTaskFactory;
    @Autowired
    private ParticipantTaskService participantTaskService;
    @Autowired
    private EnrolleeFactory enrolleeFactory;
}
