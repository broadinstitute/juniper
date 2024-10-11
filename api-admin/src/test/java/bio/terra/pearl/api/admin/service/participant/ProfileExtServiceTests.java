package bio.terra.pearl.api.admin.service.participant;

import bio.terra.pearl.api.admin.AuthAnnotationSpec;
import bio.terra.pearl.api.admin.AuthTestUtils;
import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.api.admin.service.auth.context.PortalEnrolleeAuthContext;
import bio.terra.pearl.core.factory.StudyEnvironmentBundle;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.admin.AdminUserFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.address.MailingAddress;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.audit.ParticipantDataChange;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.participant.ProfileService;
import bio.terra.pearl.core.service.workflow.ParticipantDataChangeService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class ProfileExtServiceTests extends BaseSpringBootTest {
  @Autowired private AdminUserFactory adminUserFactory;
  @Autowired private ProfileExtService profileExtService;
  @Autowired private EnrolleeFactory enrolleeFactory;
  @Autowired private ProfileService profileService;
  @Autowired private ParticipantDataChangeService participantDataChangeService;
  @Autowired private StudyEnvironmentFactory studyEnvironmentFactory;

  @Test
  public void testAllAuthenticated() {
    AuthTestUtils.assertAllMethodsAnnotated(
        profileExtService,
        Map.of(
            "updateProfileForEnrollee",
            AuthAnnotationSpec.withPortalEnrolleePerm("participant_data_edit")));
  }

  @Test
  @Transactional
  public void testUpdateProfileForEnrolleeFailsIfAdminNotInPortal(TestInfo info) {
    StudyEnvironmentBundle studyEnvBundle =
        studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.irb);

    Enrollee enrollee =
        enrolleeFactory.buildPersisted(
            getTestName(info), studyEnvBundle.getStudyEnv(), Profile.builder().build());

    AdminUser operator = adminUserFactory.buildPersisted(getTestName(info), false);

    Assertions.assertThrows(
        NotFoundException.class,
        () -> {
          profileExtService.updateProfileForEnrollee(
              PortalEnrolleeAuthContext.of(
                  operator,
                  studyEnvBundle.getPortal().getShortcode(),
                  studyEnvBundle.getStudy().getShortcode(),
                  EnvironmentName.irb,
                  enrollee.getShortcode()),
              "Asdf",
              Profile.builder().id(enrollee.getProfileId()).givenName("TEST").build());
        });
  }

  @Test
  @Transactional
  public void testUpdateProfileForEnrolleeFailsIfEnrolleeNotInPortal(TestInfo info) {
    StudyEnvironmentBundle studyEnvBundle =
        studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.irb);

    Enrollee wrongStudyEnv = enrolleeFactory.buildPersisted(getTestName(info));

    AdminUser operator = adminUserFactory.buildPersisted(getTestName(info), true);

    Assertions.assertThrows(
        NotFoundException.class,
        () -> {
          profileExtService.updateProfileForEnrollee(
              PortalEnrolleeAuthContext.of(
                  operator,
                  studyEnvBundle.getPortal().getShortcode(),
                  studyEnvBundle.getStudy().getShortcode(),
                  EnvironmentName.irb,
                  wrongStudyEnv.getShortcode()),
              "Asdf",
              Profile.builder().id(wrongStudyEnv.getProfileId()).givenName("TEST").build());
        });
  }

  @Test
  @Transactional
  public void testUpdateProfileForEnrolleeFailsIfWrongEnvironment(TestInfo info) {
    StudyEnvironmentBundle studyEnvBundle =
        studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.irb);

    Enrollee enrollee =
        enrolleeFactory.buildPersisted(
            getTestName(info), studyEnvBundle.getStudyEnv(), Profile.builder().build());

    AdminUser operator = adminUserFactory.buildPersisted(getTestName(info), true);

    Assertions.assertThrows(
        NotFoundException.class,
        () -> {
          profileExtService.updateProfileForEnrollee(
              PortalEnrolleeAuthContext.of(
                  operator,
                  studyEnvBundle.getPortal().getShortcode(),
                  studyEnvBundle.getStudy().getShortcode(),
                  EnvironmentName.sandbox,
                  enrollee.getShortcode()),
              "Asdf",
              Profile.builder().givenName("TEST").build());
        });
  }

  @Test
  @Transactional
  public void testUpdateProfileForEnrolleeDoesNotUpdateWrongProfile(TestInfo info) {
    StudyEnvironmentBundle studyEnvBundle =
        studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.irb);

    Enrollee enrollee =
        enrolleeFactory.buildPersisted(
            getTestName(info),
            studyEnvBundle.getStudyEnv(),
            Profile.builder().mailingAddress(MailingAddress.builder().build()).build());

    StudyEnvironmentBundle wrongStudyEnvBundle =
        studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.irb);

    Enrollee wrongStudyEnv =
        enrolleeFactory.buildPersisted(
            getTestName(info),
            wrongStudyEnvBundle.getStudyEnv(),
            Profile.builder().mailingAddress(MailingAddress.builder().build()).build());

    Profile wrongStudyEnvProfile = profileService.find(wrongStudyEnv.getProfileId()).orElseThrow();

    AdminUser operator = adminUserFactory.buildPersisted(getTestName(info), true);

    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> {
          profileExtService.updateProfileForEnrollee(
              PortalEnrolleeAuthContext.of(
                  operator,
                  studyEnvBundle.getPortal().getShortcode(),
                  studyEnvBundle.getStudy().getShortcode(),
                  EnvironmentName.irb,
                  enrollee.getShortcode()),
              "Asdf",
              Profile.builder().id(wrongStudyEnv.getProfileId()).givenName("TEST").build());
        });

    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> {
          profileExtService.updateProfileForEnrollee(
              PortalEnrolleeAuthContext.of(
                  operator,
                  studyEnvBundle.getPortal().getShortcode(),
                  studyEnvBundle.getStudy().getShortcode(),
                  EnvironmentName.irb,
                  enrollee.getShortcode()),
              "Asdf",
              Profile.builder()
                  .id(enrollee.getProfileId())
                  .mailingAddress(
                      MailingAddress.builder()
                          .id(wrongStudyEnvProfile.getMailingAddressId())
                          .build())
                  .givenName("TEST")
                  .build());
        });
  }

  @Test
  @Transactional
  public void testUpdateProfileForEnrollee(TestInfo info) {
    StudyEnvironmentBundle studyEnvBundle =
        studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.irb);

    Enrollee enrollee =
        enrolleeFactory.buildPersisted(
            getTestName(info), studyEnvBundle.getStudyEnv(), Profile.builder().build());

    AdminUser operator = adminUserFactory.buildPersisted(getTestName(info), true);

    profileExtService.updateProfileForEnrollee(
        PortalEnrolleeAuthContext.of(
            operator,
            studyEnvBundle.getPortal().getShortcode(),
            studyEnvBundle.getStudy().getShortcode(),
            EnvironmentName.irb,
            enrollee.getShortcode()),
        "A good reason",
        Profile.builder().id(enrollee.getProfileId()).givenName("TEST").build());

    Profile newProfile = profileService.find(enrollee.getProfileId()).orElseThrow();

    Assertions.assertEquals("TEST", newProfile.getGivenName());

    List<ParticipantDataChange> records =
        participantDataChangeService.findByEnrollee(enrollee.getId());
    Assertions.assertEquals(1, records.size());

    ParticipantDataChange profileUpdateRecord = records.get(0);

    Assertions.assertEquals("Profile", profileUpdateRecord.getModelName());
    Assertions.assertFalse(profileUpdateRecord.getOldValue().contains("TEST"));
    Assertions.assertTrue(profileUpdateRecord.getNewValue().contains("TEST"));
    Assertions.assertEquals("A good reason", profileUpdateRecord.getJustification());
  }
}
