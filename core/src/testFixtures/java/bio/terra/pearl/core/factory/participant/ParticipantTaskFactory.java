package bio.terra.pearl.core.factory.participant;

import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ParticipantTaskFactory {
  @Autowired
  ParticipantTaskService participantTaskService;

  public ParticipantTask buildPersisted(EnrolleeFactory.EnrolleeBundle enrolleeBundle,
                                                             TaskStatus status, TaskType type) {
    return buildPersisted(enrolleeBundle, null, status, type);
  }

  public ParticipantTask buildPersisted(EnrolleeFactory.EnrolleeBundle enrolleeBundle, String targetStableId,
                                        TaskStatus status, TaskType type) {
    ParticipantTask task = ParticipantTask.builder()
        .status(status)
        .enrolleeId(enrolleeBundle.enrollee().getId())
        .taskType(type)
        .targetStableId(targetStableId)
        .studyEnvironmentId(enrolleeBundle.enrollee().getStudyEnvironmentId())
        .targetName(RandomStringUtils.randomAlphabetic(6))
        .portalParticipantUserId(enrolleeBundle.portalParticipantUser().getId())
        .build();
    return participantTaskService.create(task);
  }
}
