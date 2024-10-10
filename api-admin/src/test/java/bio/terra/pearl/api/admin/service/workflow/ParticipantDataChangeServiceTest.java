package bio.terra.pearl.api.admin.service.workflow;

import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.core.factory.participant.EnrolleeBundle;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.audit.ParticipantDataChange;
import bio.terra.pearl.core.service.workflow.ParticipantDataChangeService;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;

public class ParticipantDataChangeServiceTest extends BaseSpringBootTest {
  @Autowired ParticipantDataChangeService participantDataChangeService;
  @Autowired EnrolleeFactory enrolleeFactory;

  @Test
  public void testFindAllRecordsForEnrollee(TestInfo info) {
    EnrolleeBundle enrolleeBundle = enrolleeFactory.buildWithPortalUser(info.getDisplayName());

    Assertions.assertEquals(
        0,
        participantDataChangeService.findAllRecordsForEnrollee(enrolleeBundle.enrollee()).size());

    ParticipantDataChange enrolleeRecord =
        participantDataChangeService.create(
            ParticipantDataChange.builder()
                .responsibleUserId(enrolleeBundle.enrollee().getParticipantUserId())
                .enrolleeId(enrolleeBundle.enrollee().getId())
                .modelName("Profile")
                .oldValue("old")
                .newValue("new")
                .build());

    ParticipantDataChange portalUserRecord =
        participantDataChangeService.create(
            ParticipantDataChange.builder()
                .responsibleUserId(enrolleeBundle.enrollee().getParticipantUserId())
                .portalParticipantUserId(enrolleeBundle.portalParticipantUser().getId())
                .modelName("Profile")
                .oldValue("old")
                .newValue("new")
                .build());

    List<ParticipantDataChange> records =
        participantDataChangeService.findAllRecordsForEnrollee(enrolleeBundle.enrollee());

    Assertions.assertEquals(2, records.size());
    Assertions.assertTrue(
        records.stream().anyMatch(record -> record.getId().equals(enrolleeRecord.getId())));
    Assertions.assertTrue(
        records.stream().anyMatch(record -> record.getId().equals(portalUserRecord.getId())));
  }
}
