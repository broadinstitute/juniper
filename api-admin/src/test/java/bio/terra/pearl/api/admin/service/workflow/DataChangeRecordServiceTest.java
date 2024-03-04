package bio.terra.pearl.api.admin.service.workflow;

import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.audit.DataChangeRecord;
import bio.terra.pearl.core.service.workflow.DataChangeRecordService;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;

public class DataChangeRecordServiceTest extends BaseSpringBootTest {
  @Autowired DataChangeRecordService dataChangeRecordService;
  @Autowired EnrolleeFactory enrolleeFactory;

  @Test
  public void testFindAllRecordsForEnrollee(TestInfo info) {
    EnrolleeFactory.EnrolleeBundle enrolleeBundle =
        enrolleeFactory.buildWithPortalUser(info.getDisplayName());

    Assertions.assertEquals(
        0, dataChangeRecordService.findAllRecordsForEnrollee(enrolleeBundle.enrollee()).size());

    DataChangeRecord enrolleeRecord =
        dataChangeRecordService.create(
            DataChangeRecord.builder()
                .responsibleUserId(enrolleeBundle.enrollee().getParticipantUserId())
                .enrolleeId(enrolleeBundle.enrollee().getId())
                .modelName("Profile")
                .oldValue("old")
                .newValue("new")
                .build());

    DataChangeRecord portalUserRecord =
        dataChangeRecordService.create(
            DataChangeRecord.builder()
                .responsibleUserId(enrolleeBundle.enrollee().getParticipantUserId())
                .portalParticipantUserId(enrolleeBundle.portalParticipantUser().getId())
                .modelName("Profile")
                .oldValue("old")
                .newValue("new")
                .build());

    List<DataChangeRecord> records =
        dataChangeRecordService.findAllRecordsForEnrollee(enrolleeBundle.enrollee());

    Assertions.assertEquals(2, records.size());
    Assertions.assertTrue(
        records.stream().anyMatch(record -> record.getId().equals(enrolleeRecord.getId())));
    Assertions.assertTrue(
        records.stream().anyMatch(record -> record.getId().equals(portalUserRecord.getId())));
  }
}
