package bio.terra.pearl.core.service.publishing;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.StudyFactory;
import bio.terra.pearl.core.factory.consent.ConsentFormFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.consent.ConsentForm;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.consent.ConsentFormService;
import bio.terra.pearl.core.service.study.StudyEnvironmentConfigService;
import bio.terra.pearl.core.service.study.StudyEnvironmentConsentService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.survey.SurveyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

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

        var changes = portalDiffService.diffStudyEnvs(study.getShortcode(), EnvironmentName.irb, EnvironmentName.live);
        var loadedLiveEnv = portalDiffService.loadStudyEnvForProcessing(study.getShortcode(), EnvironmentName.live);
        studyPublishingService.applyChanges(loadedLiveEnv, changes, null);

        var liveConfig = studyEnvironmentConfigService.find(liveEnv.getStudyEnvironmentConfigId()).get();
        assertThat(liveConfig.getPassword(), equalTo("foobar"));
        assertThat(liveConfig.isPasswordProtected(), equalTo(true));
    }

    @Test
    @Transactional
    public void testApplyChangesPublishSurvey() throws Exception {
        String testName = "testApplyChangesPublishSurvey";
        Study study = studyFactory.buildPersisted(testName);
        StudyEnvironment irbEnv = studyEnvironmentFactory.buildPersisted(EnvironmentName.irb, study.getId(), testName);
        StudyEnvironment liveEnv = studyEnvironmentFactory.buildPersisted(EnvironmentName.live, study.getId(), testName);
        Survey survey = surveyFactory.buildPersisted(testName);

        irbEnv.setPreEnrollSurveyId(survey.getId());
        studyEnvironmentService.update(irbEnv);

        var changes = portalDiffService.diffStudyEnvs(study.getShortcode(), EnvironmentName.irb, EnvironmentName.live);
        var loadedLiveEnv = portalDiffService.loadStudyEnvForProcessing(study.getShortcode(), EnvironmentName.live);
        studyPublishingService.applyChanges(loadedLiveEnv, changes, null);

        liveEnv = studyEnvironmentService.find(liveEnv.getId()).get();
        survey = surveyService.find(survey.getId()).get();
        assertThat(survey.getPublishedVersion(), equalTo(1));
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

        var changes = portalDiffService.diffStudyEnvs(study.getShortcode(), EnvironmentName.irb, EnvironmentName.live);
        var loadedLiveEnv = portalDiffService.loadStudyEnvForProcessing(study.getShortcode(), EnvironmentName.live);
        studyPublishingService.applyChanges(loadedLiveEnv, changes, null);

        assertThat(studyEnvironmentConsentService.findByConsentForm(liveEnv.getId(), form.getId()).isPresent(), equalTo(true));

        form = consentFormService.find(form.getId()).get();
        assertThat(form.getPublishedVersion(), equalTo(1));
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
    private SurveyService surveyService;
    @Autowired
    private ConsentFormFactory consentFormFactory;
    @Autowired
    private ConsentFormService consentFormService;
    @Autowired
    private SurveyFactory surveyFactory;
    @Autowired
    private PortalDiffService portalDiffService;
}
