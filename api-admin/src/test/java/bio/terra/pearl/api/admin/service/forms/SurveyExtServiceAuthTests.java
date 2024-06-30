package bio.terra.pearl.api.admin.service.forms;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import bio.terra.pearl.api.admin.AuthAnnotationSpec;
import bio.terra.pearl.api.admin.AuthTestUtils;
import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.EnforcePortalPermission;
import bio.terra.pearl.api.admin.service.auth.EnforcePortalStudyEnvPermission;
import bio.terra.pearl.api.admin.service.auth.SandboxOnly;
import bio.terra.pearl.api.admin.service.auth.context.PortalAuthContext;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.study.StudyEnvironmentSurveyService;
import bio.terra.pearl.core.service.survey.SurveyService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

public class SurveyExtServiceAuthTests extends BaseSpringBootTest {

  @Autowired private SurveyExtService surveyExtService;

  @MockBean private AuthUtilService mockAuthUtilService;
  @MockBean private SurveyService mockSurveyService;
  @MockBean private StudyEnvironmentSurveyService mockStudyEnvironmentSurveyService;
  @MockBean private StudyEnvironmentService mockStudyEnvironmentService;

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
  public void getRequiresSurveyMatchedToPortal() {
    AdminUser user = AdminUser.builder().superuser(false).build();
    Portal portal = Portal.builder().shortcode("testSurveyGet").id(UUID.randomUUID()).build();
    Survey matchedSurvey = configureMockSurvey("testMatchedToPortal", 1, portal.getId());
    Survey unmatchedSurvey = configureMockSurvey("testUnmatchedToPortal", 1, UUID.randomUUID());
    when(mockAuthUtilService.authUserToPortal(user, portal.getShortcode())).thenReturn(portal);
    when(mockAuthUtilService.authSurveyToPortal(portal, "testMatchedToPortal", 1))
        .thenReturn(matchedSurvey);
    when(mockAuthUtilService.authSurveyToPortal(portal, "testUnmatchedToPortal", 1))
        .thenThrow(new NotFoundException("not found"));

    assertThat(
        surveyExtService.get(
            PortalAuthContext.of(user, portal.getShortcode()),
            matchedSurvey.getStableId(),
            matchedSurvey.getVersion()),
        notNullValue());
    Assertions.assertThrows(
        NotFoundException.class,
        () ->
            surveyExtService.get(
                PortalAuthContext.of(user, portal.getShortcode()),
                unmatchedSurvey.getStableId(),
                unmatchedSurvey.getVersion()));
  }

  private Survey configureMockSurvey(String stableId, int version, UUID portalId) {
    Survey survey = Survey.builder().stableId(stableId).version(1).portalId(portalId).build();
    when(mockSurveyService.findByStableId(stableId, version, portalId))
        .thenReturn(Optional.of(survey));
    return survey;
  }

  public record AuthTestSpec(
      String methodName, Class<?> authClass, String permission, List<Class<?>> otherAnnots) {}
}
