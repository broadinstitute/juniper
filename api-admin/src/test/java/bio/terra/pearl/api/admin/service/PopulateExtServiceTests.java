package bio.terra.pearl.api.admin.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import bio.terra.pearl.api.admin.AuthAnnotationSpec;
import bio.terra.pearl.api.admin.AuthTestUtils;
import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.api.admin.service.auth.SuperuserOnly;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyEnvAuthContext;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.admin.AdminUserFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.ParticipantUserService;
import bio.terra.pearl.populate.service.EnrolleePopulateType;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class PopulateExtServiceTests extends BaseSpringBootTest {
  private PopulateExtService emptyService =
      new PopulateExtService(null, null, null, null, null, null, null, null);

  @Autowired private PopulateExtService populateExtService;
  @Autowired private StudyEnvironmentFactory studyEnvironmentFactory;
  @Autowired private AdminUserFactory adminUserFactory;
  @Autowired private EnrolleeService enrolleeService;
  @Autowired private ParticipantUserService participantUserService;

  @Test
  public void allMethodsAuthed(TestInfo info) {
    AuthTestUtils.assertAllMethodsAnnotated(
        populateExtService,
        Map.of(
            "populateBaseSeed",
                AuthAnnotationSpec.withOtherAnnotations(List.of(SuperuserOnly.class)),
            "populateAdminConfig",
                AuthAnnotationSpec.withOtherAnnotations(List.of(SuperuserOnly.class)),
            "populatePortal", AuthAnnotationSpec.withOtherAnnotations(List.of(SuperuserOnly.class)),
            "populateSurvey", AuthAnnotationSpec.withOtherAnnotations(List.of(SuperuserOnly.class)),
            "populateSiteContent",
                AuthAnnotationSpec.withOtherAnnotations(List.of(SuperuserOnly.class)),
            "populateEnrollee", AuthAnnotationSpec.withPortalStudyEnvPerm("BASE"),
            "bulkPopulateEnrollees",
                AuthAnnotationSpec.withOtherAnnotations(List.of(SuperuserOnly.class)),
            "extractPortal", AuthAnnotationSpec.withOtherAnnotations(List.of(SuperuserOnly.class)),
            "populateCommand",
                AuthAnnotationSpec.withOtherAnnotations(List.of(SuperuserOnly.class))));
  }

  @Test
  @Transactional
  public void populatesNewEnrolleeType(TestInfo info) {
    StudyEnvironmentFactory.StudyEnvironmentBundle bundle =
        studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.live);
    AdminUser operator = adminUserFactory.buildPersisted(getTestName(info), true);
    populateExtService.populateEnrollee(
        PortalStudyEnvAuthContext.of(
            operator,
            bundle.getPortal().getShortcode(),
            bundle.getStudy().getShortcode(),
            bundle.getPortalEnv().getEnvironmentName()),
        EnrolleePopulateType.NEW,
        "someone@user.com");
    List<Enrollee> enrollees = enrolleeService.findByStudyEnvironment(bundle.getStudyEnv().getId());
    assertThat(enrollees, hasSize(1));
    ParticipantUser pUser =
        participantUserService.find(enrollees.get(0).getParticipantUserId()).orElseThrow();
    assertThat(pUser.getUsername(), equalTo("someone@user.com"));
  }
}
