package bio.terra.pearl.api.admin.service.enrollee;

import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.admin.AdminUserFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.workflow.DataChangeRecord;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import bio.terra.pearl.core.service.participant.ProfileService;
import bio.terra.pearl.core.service.workflow.DataChangeRecordService;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;

public class EnrolleeExtServiceTests extends BaseSpringBootTest {
  @Autowired private AdminUserFactory adminUserFactory;
  @Autowired private EnrolleeExtService enrolleeExtService;
  @Autowired private EnrolleeFactory enrolleeFactory;
  @Autowired private ProfileService profileService;
  @Autowired private DataChangeRecordService dataChangeRecordService;
  @Autowired private StudyEnvironmentFactory studyEnvironmentFactory;

  @Test
  public void testUpdateProfileFailsIfNotInPortal(TestInfo info) {
    var studyEnvBundle =
        studyEnvironmentFactory.buildBundle("updateConfigAuthsToStudy", EnvironmentName.irb);

    Enrollee enrollee =
        enrolleeFactory.buildPersisted(
            getTestName(info), studyEnvBundle.getStudyEnv(), Profile.builder().build());

    AdminUser operator = adminUserFactory.buildPersisted("updateConfigAuthsToStudy", false);

    Assertions.assertThrows(
        PermissionDeniedException.class,
        () -> {
          enrolleeExtService.updateProfile(
              operator,
              enrollee.getShortcode(),
              Profile.builder().id(enrollee.getProfileId()).givenName("TEST").build());
        });
  }

  @Test
  public void testUpdateProfile(TestInfo info) {
    var studyEnvBundle =
        studyEnvironmentFactory.buildBundle("updateConfigAuthsToStudy", EnvironmentName.irb);

    Enrollee enrollee =
        enrolleeFactory.buildPersisted(
            getTestName(info), studyEnvBundle.getStudyEnv(), Profile.builder().build());

    AdminUser operator = adminUserFactory.buildPersisted(getTestName(info), true);

    enrolleeExtService.updateProfile(
        operator,
        enrollee.getShortcode(),
        Profile.builder().id(enrollee.getProfileId()).givenName("TEST").build());

    Profile newProfile = profileService.find(enrollee.getProfileId()).orElseThrow();

    Assertions.assertEquals("TEST", newProfile.getGivenName());

    List<DataChangeRecord> records = dataChangeRecordService.findByEnrollee(enrollee.getId());
    Assertions.assertEquals(1, records.size());

    DataChangeRecord profileUpdateRecord = records.get(0);

    Assertions.assertEquals("Profile", profileUpdateRecord.getModelName());
    Assertions.assertFalse(profileUpdateRecord.getOldValue().contains("TEST"));
    Assertions.assertTrue(profileUpdateRecord.getNewValue().contains("TEST"));
  }
}
