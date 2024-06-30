package bio.terra.pearl.api.admin.service.forms;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import bio.terra.pearl.api.admin.AuthAnnotationSpec;
import bio.terra.pearl.api.admin.AuthTestUtils;
import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.api.admin.service.auth.SandboxOnly;
import bio.terra.pearl.api.admin.service.auth.context.PortalAuthContext;
import bio.terra.pearl.core.factory.admin.AdminUserBundle;
import bio.terra.pearl.core.factory.admin.AdminUserFactory;
import bio.terra.pearl.core.factory.admin.PortalAdminUserFactory;
import bio.terra.pearl.core.factory.portal.PortalFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.exception.NotFoundException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class SurveyExtServiceAuthTests extends BaseSpringBootTest {

  @Autowired private SurveyExtService surveyExtService;
  @Autowired private PortalFactory portalFactory;
  @Autowired private SurveyFactory surveyFactory;
  @Autowired private AdminUserFactory adminUserFactory;
  @Autowired private PortalAdminUserFactory portalAdminUserFactory;

  @Test
  public void assertAllMethods() {
    AuthTestUtils.assertAllMethodsAnnotated(
        surveyExtService,
        Map.of(
            "get", AuthAnnotationSpec.withPortalPerm("BASE"),
            "listVersions", AuthAnnotationSpec.withPortalPerm("BASE"),
            "findWithSurveyNoContent", AuthAnnotationSpec.withPortalStudyEnvPerm("BASE"),
            "create", AuthAnnotationSpec.withPortalPerm("survey_edit"),
            "delete", AuthAnnotationSpec.withPortalPerm("survey_edit"),
            "createNewVersion", AuthAnnotationSpec.withPortalPerm("survey_edit"),
            "createConfiguredSurvey",
                AuthAnnotationSpec.withPortalStudyEnvPerm(
                    "survey_edit", List.of(SandboxOnly.class)),
            "updateConfiguredSurvey",
                AuthAnnotationSpec.withPortalStudyEnvPerm(
                    "survey_edit", List.of(SandboxOnly.class)),
            "removeConfiguredSurvey",
                AuthAnnotationSpec.withPortalStudyEnvPerm(
                    "survey_edit", List.of(SandboxOnly.class)),
            "replace",
                AuthAnnotationSpec.withPortalStudyEnvPerm(
                    "survey_edit", List.of(SandboxOnly.class))));
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

  public record AuthTestSpec(
      String methodName, Class<?> authClass, String permission, List<Class<?>> otherAnnots) {}
}
