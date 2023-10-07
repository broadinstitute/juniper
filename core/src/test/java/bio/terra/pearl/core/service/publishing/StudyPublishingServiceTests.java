package bio.terra.pearl.core.service.publishing;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.dao.study.StudyEnvironmentSurveyDao;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.StudyFactory;
import bio.terra.pearl.core.factory.consent.ConsentFormFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.consent.ConsentForm;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.consent.ConsentFormService;
import bio.terra.pearl.core.service.study.StudyEnvironmentConfigService;
import bio.terra.pearl.core.service.study.StudyEnvironmentConsentService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.study.StudyEnvironmentSurveyService;
import bio.terra.pearl.core.service.survey.SurveyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

public class StudyPublishingServiceTests extends BaseSpringBootTest {
    @Test
    @Transactional
    public void testApplyStudyConfigChanges() throws Exception {
        String testName = "testApplyStudyConfigChanges";
        Study study = studyFactory.buildPersisted(testName);
        StudyEnvironment irbEnv = studyEnvironmentFactory.buildPersisted(EnvironmentName.irb, study.getId(), testName);
        StudyEnvironment liveEnv = studyEnvironmentFactory.buildPersisted(EnvironmentName.live, study.getId(), testName);

        var irbConfig = studyEnvironmentConfigService.find(irbEnv.getStudyEnvironmentConfigId()).get();
        irbConfig.setPassword("foobar");
        irbConfig.setPasswordProtected(true);
        studyEnvironmentConfigService.update(irbConfig);

        diffAndApplyChanges(study.getShortcode(), EnvironmentName.irb, EnvironmentName.live);

        var liveConfig = studyEnvironmentConfigService.find(liveEnv.getStudyEnvironmentConfigId()).get();
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
        var surveyConfig = studyEnvironmentSurveyService.create(StudyEnvironmentSurvey.builder()
                .surveyId(survey.getId())
                .studyEnvironmentId(irbEnv.getId())
                .build());

        diffAndApplyChanges(study.getShortcode(), EnvironmentName.irb, EnvironmentName.live);

        List<StudyEnvironmentSurvey> liveSurveys = studyEnvironmentSurveyService.findAllByStudyEnvIdWithSurvey(liveEnv.getId());
        assertThat(liveSurveys, hasSize(1));
        assertThat(liveSurveys.get(0).getSurveyId(), equalTo(survey.getId()));

        // now test that we can publish a removal
        studyEnvironmentSurveyService.deactivate(surveyConfig.getId());
        diffAndApplyChanges(study.getShortcode(), EnvironmentName.irb, EnvironmentName.live);

        liveSurveys = studyEnvironmentSurveyService.findAllByStudyEnvIdWithSurvey(liveEnv.getId());
        assertThat(liveSurveys, hasSize(0));
        // confirm the deactivated config is still there
        assertThat(studyEnvironmentSurveyDao.findAllByStudyEnvironmentId(liveEnv.getId(), false), hasSize(1));
    }

    @Test
    @Transactional
    public void testApplyChangesConsents() throws Exception {
        String testName = "testApplyChangesConsents";
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
        var changes = portalDiffService.diffStudyEnvs(studyShortcode, src, dest);
        var loadedLiveEnv = portalDiffService.loadStudyEnvForProcessing(studyShortcode, dest);
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
}
