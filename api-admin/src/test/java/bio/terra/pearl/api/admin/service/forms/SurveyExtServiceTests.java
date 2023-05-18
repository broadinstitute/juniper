package bio.terra.pearl.api.admin.service.forms;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
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
  @MockBean private StudyEnvironmentSurveyService studyEnvironmentSurveyService;

  @Test
  public void createNewVersionRequiresSuperuser() {
    AdminUser user = AdminUser.builder().superuser(false).build();
    Assertions.assertThrows(
        PermissionDeniedException.class,
        () -> surveyExtService.createNewVersion("foo", null, user));
  }

  @Test
  public void createRequiresSuperuser() {
    AdminUser user = AdminUser.builder().superuser(false).build();
    Assertions.assertThrows(
        PermissionDeniedException.class, () -> surveyExtService.create("foo", null, user));
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
  public void getRequiresSurveyMatchedToPortal() {
    AdminUser user = AdminUser.builder().superuser(false).build();
    Portal portal = Portal.builder().shortcode("testSurveyGet").id(UUID.randomUUID()).build();
    when(mockAuthUtilService.authUserToPortal(user, portal.getShortcode())).thenReturn(portal);
    Survey matchedSurvey =
        Survey.builder()
            .stableId("testMatchedToPortal")
            .version(1)
            .portalId(portal.getId())
            .build();
    when(mockSurveyService.findByStableId(matchedSurvey.getStableId(), matchedSurvey.getVersion()))
        .thenReturn(Optional.of(matchedSurvey));
    Survey unmatchedSurvey =
        Survey.builder()
            .stableId("testMatchedToPortalUnmatched")
            .version(1)
            .portalId(UUID.randomUUID())
            .build();
    when(mockSurveyService.findByStableId(
            unmatchedSurvey.getStableId(), unmatchedSurvey.getVersion()))
        .thenReturn(Optional.of(unmatchedSurvey));

    assertThat(
        surveyExtService
            .get(
                portal.getShortcode(),
                matchedSurvey.getStableId(),
                matchedSurvey.getVersion(),
                user)
            .isPresent(),
        equalTo(true));
    Assertions.assertThrows(
        PermissionDeniedException.class,
        () ->
            surveyExtService.get(
                portal.getShortcode(),
                unmatchedSurvey.getStableId(),
                unmatchedSurvey.getVersion(),
                user));
  }
}
