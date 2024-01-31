package bio.terra.pearl.api.admin.service.forms;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.admin.AdminUserFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.study.StudyEnvironmentSurveyService;
import bio.terra.pearl.core.service.survey.SurveyService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class SurveyExtServiceTests extends BaseSpringBootTest {
  @Autowired private StudyEnvironmentFactory studyEnvironmentFactory;
  @Autowired private SurveyFactory surveyFactory;
  @Autowired private SurveyService surveyService;
  @Autowired private SurveyExtService surveyExtService;
  @Autowired private StudyEnvironmentSurveyService studyEnvironmentSurveyService;
  @Autowired private AdminUserFactory adminUserFactory;

  @Test
  @Transactional
  public void replaceStudyEnvSurvey(TestInfo testInfo) {
    AdminUser operator = adminUserFactory.buildPersisted(getTestName(testInfo), true);
    StudyEnvironmentFactory.StudyEnvironmentBundle bundle =
        studyEnvironmentFactory.buildBundle(getTestName(testInfo), EnvironmentName.sandbox);
    Survey survey1 = surveyFactory.buildPersisted(getTestName(testInfo));
    Survey survey2 = surveyService.createNewVersion(bundle.getPortal().getId(), survey1);
    StudyEnvironmentSurvey studyEnvSurvey1 =
        surveyFactory.attachToEnv(survey1, bundle.getStudyEnv().getId(), true);

    surveyExtService.replace(
        bundle.getPortal().getShortcode(),
        bundle.getStudy().getShortcode(),
        EnvironmentName.sandbox,
        studyEnvSurvey1.getId(),
        StudyEnvironmentSurvey.builder()
            .surveyId(survey2.getId())
            .studyEnvironmentId(bundle.getStudyEnv().getId())
            .active(true)
            .surveyOrder(1)
            .build(),
        operator);

    // there now should be two studyEnvSurveys, one active, and one inactive
    List<StudyEnvironmentSurvey> studyEnvSurveys =
        studyEnvironmentSurveyService.findAllByStudyEnvId(bundle.getStudyEnv().getId(), null);
    StudyEnvironmentSurvey activeEnvSurvey =
        studyEnvSurveys.stream().filter(ses -> ses.isActive()).findFirst().orElseThrow();
    assertThat(activeEnvSurvey.getSurveyId(), equalTo(survey2.getId()));

    StudyEnvironmentSurvey inactiveEnvSurvey =
        studyEnvSurveys.stream().filter(ses -> !ses.isActive()).findFirst().orElseThrow();
    assertThat(inactiveEnvSurvey.getSurveyId(), equalTo(survey1.getId()));
  }
}
