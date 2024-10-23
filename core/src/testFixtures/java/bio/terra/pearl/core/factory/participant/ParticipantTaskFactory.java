package bio.terra.pearl.core.factory.participant;

import bio.terra.pearl.core.dao.dataimport.TimeShiftDao;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class ParticipantTaskFactory {
  @Autowired
  ParticipantTaskService participantTaskService;
  @Autowired
  TimeShiftDao timeShiftDao;

  public static ParticipantTask.ParticipantTaskBuilder DEFAULT_BUILDER = ParticipantTask.builder()
          .status(TaskStatus.NEW)
          .taskType(TaskType.SURVEY)
          .targetName("test")
          .taskOrder(1);

  public ParticipantTask buildPersisted(EnrolleeBundle enrolleeBundle,
                                        TaskStatus status, TaskType type) {
    return buildPersisted(enrolleeBundle, null, status, type);
  }

  public ParticipantTask buildPersisted(EnrolleeBundle enrolleeBundle, String targetStableId,
                                        TaskStatus status, TaskType type) {
    return buildPersisted(enrolleeBundle, targetStableId, RandomStringUtils.randomAlphabetic(6), status, type);
  }

  public ParticipantTask buildPersisted(EnrolleeBundle enrolleeBundle, String targetStableId,
                                        String targetName, TaskStatus status, TaskType type) {
    DataAuditInfo auditInfo = DataAuditInfo.builder().systemProcess(
            DataAuditInfo.systemProcessName(getClass(), "buildPersisted")
    ).build();
    ParticipantTask task = ParticipantTask.builder()
        .status(status)
        .enrolleeId(enrolleeBundle.enrollee().getId())
        .taskType(type)
        .targetStableId(targetStableId)
        .studyEnvironmentId(enrolleeBundle.enrollee().getStudyEnvironmentId())
        .targetName(targetName)
        .portalParticipantUserId(enrolleeBundle.portalParticipantUser().getId())
        .build();
    return participantTaskService.create(task, auditInfo);
  }

  /** auto-sets the enrollee and environment-related fields, otherwise builds the task as provided */
  public ParticipantTask buildPersisted(EnrolleeBundle enrolleeBundle, ParticipantTask.ParticipantTaskBuilder builder) {
    return buildPersisted(enrolleeBundle, builder, null);
  }

  public ParticipantTask buildPersisted(EnrolleeBundle enrolleeBundle, ParticipantTask.ParticipantTaskBuilder builder, Instant createdAt) {
    DataAuditInfo auditInfo = DataAuditInfo.builder().systemProcess(
            DataAuditInfo.systemProcessName(getClass(), "buildPersisted")
    ).build();
    ParticipantTask task = builder
            .enrolleeId(enrolleeBundle.enrollee().getId())
            .studyEnvironmentId(enrolleeBundle.enrollee().getStudyEnvironmentId())
            .portalParticipantUserId(enrolleeBundle.portalParticipantUser().getId())
            .build();
    task = participantTaskService.create(task, auditInfo);
    if (createdAt != null) {
      timeShiftDao.changeTaskCreationTime(task.getId(), createdAt);
      task.setCreatedAt(createdAt);
    }
    return task;
  }

}
