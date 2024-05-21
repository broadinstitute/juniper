package bio.terra.pearl.api.admin.service.forms;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpressionParser;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.study.StudyEnvironmentSurveyService;
import bio.terra.pearl.core.service.survey.SurveyService;
import bio.terra.pearl.core.service.workflow.EventService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

@ContextConfiguration(classes = SurveyExtService.class)
@WebMvcTest
public class SurveyExtServiceAuthTests {

  @Autowired private MockMvc mockMvc;
  @Autowired private SurveyExtService surveyExtService;

  @MockBean private AuthUtilService mockAuthUtilService;
  @MockBean private SurveyService mockSurveyService;
  @MockBean private StudyEnvironmentSurveyService mockStudyEnvironmentSurveyService;
  @MockBean private StudyEnvironmentService mockStudyEnvironmentService;
  @MockBean private PortalEnvironmentService mockPortalEnvironmentService;
  @MockBean private EventService mockEventService;
  @MockBean private EnrolleeSearchExpressionParser enrolleeSearchExpressionParser;

  @Test
  public void createConfiguredRequiresPortalAuth() {
    AdminUser user = AdminUser.builder().superuser(false).build();
    when(mockAuthUtilService.authUserToStudy(user, "foo", "bar"))
        .thenThrow(new PermissionDeniedException("test1"));
    Assertions.assertThrows(
        PermissionDeniedException.class,
        () ->
            surveyExtService.createConfiguredSurvey(
                "foo", "bar", EnvironmentName.sandbox, null, user));
  }

  @Test
  public void createConfiguredOnlyInSandbox() {
    AdminUser user = AdminUser.builder().superuser(false).build();
    when(mockStudyEnvironmentService.findByStudy("bar", EnvironmentName.irb))
        .thenReturn(
            Optional.of(StudyEnvironment.builder().environmentName(EnvironmentName.irb).build()));
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () ->
            surveyExtService.createConfiguredSurvey("foo", "bar", EnvironmentName.irb, null, user));
  }

  @Test
  public void getRequiresPortalAuth() {
    AdminUser user = AdminUser.builder().superuser(false).build();
    when(mockAuthUtilService.authUserToPortal(user, "foo"))
        .thenThrow(new PermissionDeniedException("test1"));
    Assertions.assertThrows(
        PermissionDeniedException.class, () -> surveyExtService.get("foo", "blah", 1, user));
  }

  @Test
  public void deleteRequiresPortalAuth() {
    AdminUser user = AdminUser.builder().superuser(false).build();
    when(mockAuthUtilService.authUserToPortal(user, "foo"))
        .thenThrow(new PermissionDeniedException("test1"));
    Assertions.assertThrows(
        PermissionDeniedException.class, () -> surveyExtService.delete("foo", "blah", user));
  }

  @Test
  public void removeRequiresPortalAuth() {
    AdminUser user = AdminUser.builder().superuser(false).build();
    when(mockAuthUtilService.authUserToStudy(user, "foo", "blah"))
        .thenThrow(new PermissionDeniedException("test1"));
    StudyEnvironmentSurvey studyEnvironmentSurvey =
        StudyEnvironmentSurvey.builder().studyEnvironmentId(UUID.randomUUID()).build();
    when(mockStudyEnvironmentSurveyService.find(studyEnvironmentSurvey.getId()))
        .thenReturn(Optional.of(studyEnvironmentSurvey));
    Assertions.assertThrows(
        PermissionDeniedException.class,
        () ->
            surveyExtService.removeConfiguredSurvey(
                "foo", "blah", EnvironmentName.sandbox, studyEnvironmentSurvey.getId(), user));
  }

  @Test
  public void removeOnlyInSandbox() {
    AdminUser user = AdminUser.builder().superuser(true).build();
    StudyEnvironment studyEnv =
        StudyEnvironment.builder()
            .id(UUID.randomUUID())
            .environmentName(EnvironmentName.irb)
            .build();
    when(mockStudyEnvironmentService.findByStudy("bar", EnvironmentName.irb))
        .thenReturn(Optional.of(studyEnv));
    StudyEnvironmentSurvey studyEnvironmentSurvey =
        StudyEnvironmentSurvey.builder().studyEnvironmentId(UUID.randomUUID()).build();
    when(mockStudyEnvironmentSurveyService.find(studyEnvironmentSurvey.getId()))
        .thenReturn(Optional.of(studyEnvironmentSurvey));
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () ->
            surveyExtService.removeConfiguredSurvey("foo", "bar", EnvironmentName.irb, null, user));
  }

  @Test
  public void listVersionsRequiresPortalAuth() {
    AdminUser user = AdminUser.builder().superuser(false).build();
    when(mockAuthUtilService.authUserToPortal(user, "foo"))
        .thenThrow(new PermissionDeniedException("test1"));
    Assertions.assertThrows(
        PermissionDeniedException.class, () -> surveyExtService.listVersions("foo", "blah", user));
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
            portal.getShortcode(), matchedSurvey.getStableId(), matchedSurvey.getVersion(), user),
        notNullValue());
    Assertions.assertThrows(
        NotFoundException.class,
        () ->
            surveyExtService.get(
                portal.getShortcode(),
                unmatchedSurvey.getStableId(),
                unmatchedSurvey.getVersion(),
                user));
  }

  private Survey configureMockSurvey(String stableId, int version, UUID portalId) {
    Survey survey = Survey.builder().stableId(stableId).version(1).portalId(portalId).build();
    when(mockSurveyService.findByStableId(stableId, version, portalId))
        .thenReturn(Optional.of(survey));
    return survey;
  }
}
