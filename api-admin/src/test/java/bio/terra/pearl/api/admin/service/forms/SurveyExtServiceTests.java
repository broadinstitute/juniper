package bio.terra.pearl.api.admin.service.forms;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import bio.terra.pearl.api.admin.AuthAnnotationSpec;
import bio.terra.pearl.api.admin.AuthTestUtils;
import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.api.admin.service.auth.SandboxOnly;
import bio.terra.pearl.api.admin.service.auth.context.PortalAuthContext;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyEnvAuthContext;
import bio.terra.pearl.core.factory.StudyEnvironmentBundle;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.admin.AdminUserBundle;
import bio.terra.pearl.core.factory.admin.AdminUserFactory;
import bio.terra.pearl.core.factory.admin.PortalAdminUserFactory;
import bio.terra.pearl.core.factory.portal.PortalFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.study.StudyEnvironmentSurveyService;
import bio.terra.pearl.core.service.survey.SurveyService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
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
  @Autowired private PortalAdminUserFactory portalAdminUserFactory;
  @Autowired private PortalFactory portalFactory;

  @Test
  public void assertAllMethods() {
    AuthTestUtils.assertAllMethodsAnnotated(
        surveyExtService,
        Map.of(
            "get",
            AuthAnnotationSpec.withPortalPerm("BASE"),
            "listVersions",
            AuthAnnotationSpec.withPortalPerm("BASE"),
            "findWithSurveyNoContent",
            AuthAnnotationSpec.withPortalStudyPerm("BASE"),
            "create",
            AuthAnnotationSpec.withPortalPerm("survey_edit"),
            "delete",
            AuthAnnotationSpec.withPortalPerm("survey_edit"),
            "createNewVersion",
            AuthAnnotationSpec.withPortalPerm("survey_edit"),
            "createConfiguredSurvey",
            AuthAnnotationSpec.withPortalStudyEnvPerm("survey_edit", List.of(SandboxOnly.class)),
            "updateConfiguredSurveys",
            AuthAnnotationSpec.withPortalStudyEnvPerm("survey_edit", List.of(SandboxOnly.class)),
            "removeConfiguredSurvey",
            AuthAnnotationSpec.withPortalStudyEnvPerm("survey_edit", List.of(SandboxOnly.class)),
            "replace",
            AuthAnnotationSpec.withPortalStudyEnvPerm("survey_edit", List.of(SandboxOnly.class))));
  }

  @Test
  @Transactional
  public void getRequiresSurveyMatchedToPortal(TestInfo info) {
    Portal portal = portalFactory.buildPersisted(getTestName(info));
    AdminUserBundle userBundle =
        portalAdminUserFactory.buildPersistedWithPortals(getTestName(info), List.of(portal));
    Portal otherPortal = portalFactory.buildPersisted(getTestName(info));
    Survey survey = surveyFactory.buildPersisted(getTestName(info), portal.getId());
    assertThat(
        surveyExtService.get(
            PortalAuthContext.of(userBundle.user(), portal.getShortcode()),
            survey.getStableId(),
            survey.getVersion()),
        equalTo(survey));

    // not found if attempted to retrieve via the other portal
    Assertions.assertThrows(
        NotFoundException.class,
        () ->
            surveyExtService.get(
                PortalAuthContext.of(userBundle.user(), otherPortal.getShortcode()),
                survey.getStableId(),
                survey.getVersion()));
  }

  @Test
  @Transactional
  public void replaceStudyEnvSurvey(TestInfo testInfo) {
    AdminUser operator = adminUserFactory.buildPersisted(getTestName(testInfo), true);
    StudyEnvironmentBundle bundle =
        studyEnvironmentFactory.buildBundle(getTestName(testInfo), EnvironmentName.sandbox);
    Survey survey1 = surveyFactory.buildPersisted(getTestName(testInfo));
    Survey survey2 = surveyService.createNewVersion(bundle.getPortal().getId(), survey1);
    StudyEnvironmentSurvey studyEnvSurvey1 =
        surveyFactory.attachToEnv(survey1, bundle.getStudyEnv().getId(), true, 1);

    surveyExtService.replace(
        PortalStudyEnvAuthContext.of(
            operator,
            bundle.getPortal().getShortcode(),
            bundle.getStudy().getShortcode(),
            bundle.getStudyEnv().getEnvironmentName()),
        StudyEnvironmentSurvey.builder()
            .surveyId(survey2.getId())
            .studyEnvironmentId(bundle.getStudyEnv().getId())
            .active(true)
            .surveyOrder(1)
            .build());

    // there now should be two studyEnvSurveys, one active, and one inactive
    List<StudyEnvironmentSurvey> studyEnvSurveys =
        studyEnvironmentSurveyService.findAllByStudyEnvId(bundle.getStudyEnv().getId(), null);
    StudyEnvironmentSurvey activeEnvSurvey =
        studyEnvSurveys.stream().filter(StudyEnvironmentSurvey::isActive).findFirst().orElseThrow();
    assertThat(activeEnvSurvey.getSurveyId(), equalTo(survey2.getId()));

    StudyEnvironmentSurvey inactiveEnvSurvey =
        studyEnvSurveys.stream().filter(ses -> !ses.isActive()).findFirst().orElseThrow();
    assertThat(inactiveEnvSurvey.getSurveyId(), equalTo(survey1.getId()));
  }

  @Test
  @Transactional
  public void updateConfiguredSurveys(TestInfo testInfo) {
    AdminUser operator = adminUserFactory.buildPersisted(getTestName(testInfo), true);
    StudyEnvironmentBundle bundle =
        studyEnvironmentFactory.buildBundle(getTestName(testInfo), EnvironmentName.sandbox);
    Survey survey1 =
        surveyFactory.buildPersisted(getTestName(testInfo), bundle.getPortal().getId());
    Survey survey2 =
        surveyFactory.buildPersisted(getTestName(testInfo), bundle.getPortal().getId());
    StudyEnvironmentSurvey studyEnvSurvey1 =
        surveyFactory.attachToEnv(survey1, bundle.getStudyEnv().getId(), true, 1);
    StudyEnvironmentSurvey studyEnvSurvey2 =
        surveyFactory.attachToEnv(survey2, bundle.getStudyEnv().getId(), true, 2);

    // now switch the order of the surveys
    studyEnvSurvey1.setSurveyOrder(2);
    studyEnvSurvey2.setSurveyOrder(1);
    surveyExtService.updateConfiguredSurveys(
        PortalStudyEnvAuthContext.of(
            operator,
            bundle.getPortal().getShortcode(),
            bundle.getStudy().getShortcode(),
            bundle.getStudyEnv().getEnvironmentName()),
        List.of(studyEnvSurvey1, studyEnvSurvey2));

    // confirm the order switch was persisted
    List<StudyEnvironmentSurvey> studyEnvSurveys =
        studyEnvironmentSurveyService.findAllByStudyEnvId(bundle.getStudyEnv().getId(), null);
    assertThat(
        studyEnvSurveys.stream()
            .filter(ses -> ses.getSurveyOrder() == 1)
            .findFirst()
            .orElseThrow()
            .getSurveyId(),
        equalTo(survey2.getId()));
    assertThat(
        studyEnvSurveys.stream()
            .filter(ses -> ses.getSurveyOrder() == 2)
            .findFirst()
            .orElseThrow()
            .getSurveyId(),
        equalTo(survey1.getId()));
  }
}
