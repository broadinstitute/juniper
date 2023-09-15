package bio.terra.pearl.api.admin.service.forms;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.study.StudyEnvironmentSurveyService;
import bio.terra.pearl.core.service.survey.SurveyService;
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
public class SurveyExtServiceTests {

  @Autowired private MockMvc mockMvc;
  @Autowired private SurveyExtService surveyExtService;

  @MockBean private AuthUtilService mockAuthUtilService;
  @MockBean private SurveyService mockSurveyService;
  @MockBean private StudyEnvironmentSurveyService mockStudyEnvironmentSurveyService;
  @MockBean private StudyEnvironmentService studyEnvironmentService;

  @Test
  public void createConfiguredRequiresPortalAuth() {
    AdminUser user = AdminUser.builder().superuser(false).build();
    when(mockAuthUtilService.authUserToPortal(user, "foo"))
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
    IllegalArgumentException thrownException =
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () ->
                surveyExtService.createConfiguredSurvey(
                    "foo", "bar", EnvironmentName.irb, null, user));
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
    when(mockAuthUtilService.authUserToPortal(user, portal.getShortcode())).thenReturn(portal);
    Survey matchedSurvey = configureMockSurvey("testMatchedToPortal", 1, portal.getId());
    Survey unmatchedSurvey = configureMockSurvey("testUnmatchedToPortal", 1, UUID.randomUUID());
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
    when(mockSurveyService.findByStableId(stableId, version)).thenReturn(Optional.of(survey));
    return survey;
  }
}
