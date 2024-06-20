package bio.terra.pearl.api.admin.service.forms;

import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyEnvAuthContext;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.service.exception.NotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

public class SurveyResponseExtServiceTests extends BaseSpringBootTest {
  private final PortalStudyEnvAuthContext emptyAuthContext =
      PortalStudyEnvAuthContext.of(
          new AdminUser(), "someportal", "somestudy", EnvironmentName.sandbox);

  @Autowired private SurveyResponseExtService surveyResponseExtService;
  @MockBean private AuthUtilService authUtilService;

  @Test
  public void updateSurveyResponseRequiresAuth() {
    Assertions.assertThrows(
        NotFoundException.class,
        () ->
            surveyResponseExtService.updateResponse(
                emptyAuthContext, new AdminUser(), null, "someenrollee", null));
    Mockito.verify(authUtilService)
        .authUserToPortalWithPermission(
            emptyAuthContext.getOperator(),
            emptyAuthContext.getPortalShortcode(),
            "survey_response_edit");
  }
}
