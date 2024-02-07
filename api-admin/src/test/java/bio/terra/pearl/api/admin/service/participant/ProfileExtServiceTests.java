package bio.terra.pearl.api.admin.service.participant;

import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.admin.AdminUserFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.audit.DataChangeRecord;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import bio.terra.pearl.core.service.participant.ProfileService;
import bio.terra.pearl.core.service.workflow.DataChangeRecordService;
import java.util.List;
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
  @Autowired private DataChangeRecordService dataChangeRecordService;
  @Autowired private StudyEnvironmentFactory studyEnvironmentFactory;

  @Test
  @Transactional
  public void testUpdateProfileForEnrolleeFailsIfNotInPortal(TestInfo info) {
    StudyEnvironmentFactory.StudyEnvironmentBundle studyEnvBundle =
        studyEnvironmentFactory.buildBundle("updateConfigAuthsToStudy", EnvironmentName.irb);

    Enrollee enrollee =
        enrolleeFactory.buildPersisted(
            getTestName(info), studyEnvBundle.getStudyEnv(), Profile.builder().build());

    AdminUser operator = adminUserFactory.buildPersisted(getTestName(info), false);

    Assertions.assertThrows(
        PermissionDeniedException.class,
        () -> {
          profileExtService.updateProfileForEnrollee(
              operator,
              enrollee.getShortcode(),
              "Asdf",
              Profile.builder().id(enrollee.getProfileId()).givenName("TEST").build());
        });
  }

  @Test
  @Transactional
  public void testUpdateProfileForEnrollee(TestInfo info) {
    StudyEnvironmentFactory.StudyEnvironmentBundle studyEnvBundle =
        studyEnvironmentFactory.buildBundle("updateConfigAuthsToStudy", EnvironmentName.irb);

    Enrollee enrollee =
        enrolleeFactory.buildPersisted(
            getTestName(info), studyEnvBundle.getStudyEnv(), Profile.builder().build());

    AdminUser operator = adminUserFactory.buildPersisted(getTestName(info), true);

    profileExtService.updateProfileForEnrollee(
        operator,
        enrollee.getShortcode(),
        "A good reason",
        Profile.builder().id(enrollee.getProfileId()).givenName("TEST").build());

    Profile newProfile = profileService.find(enrollee.getProfileId()).orElseThrow();

    Assertions.assertEquals("TEST", newProfile.getGivenName());

    List<DataChangeRecord> records = dataChangeRecordService.findByEnrollee(enrollee.getId());
    Assertions.assertEquals(1, records.size());

    DataChangeRecord profileUpdateRecord = records.get(0);

    Assertions.assertEquals("Profile", profileUpdateRecord.getModelName());
    Assertions.assertFalse(profileUpdateRecord.getOldValue().contains("TEST"));
    Assertions.assertTrue(profileUpdateRecord.getNewValue().contains("TEST"));
    Assertions.assertEquals("A good reason", profileUpdateRecord.getJustification());
  }
}
