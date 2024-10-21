package bio.terra.pearl.core.service.workflow;

import bio.terra.pearl.core.dao.workflow.ParticipantTaskDao;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.audit.ResponsibleEntity;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantNote;
import bio.terra.pearl.core.model.workflow.ParticipantTask;

import java.time.Instant;
import java.util.*;

import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.DataAuditedService;
import bio.terra.pearl.core.service.ParticipantDataAuditedService;
import bio.terra.pearl.core.service.exception.internal.InternalServerException;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.ParticipantNoteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class ParticipantTaskService extends ParticipantDataAuditedService<ParticipantTask, ParticipantTaskDao> {
    private final EnrolleeService enrolleeService;
    private final ParticipantNoteService participantNoteService;

    public ParticipantTaskService(ParticipantTaskDao dao, ParticipantDataChangeService participantDataChangeService, ObjectMapper objectMapper, @Lazy EnrolleeService enrolleeService, ParticipantNoteService participantNoteService) {
        super(dao, participantDataChangeService, objectMapper);
        this.enrolleeService = enrolleeService;
        this.participantNoteService = participantNoteService;
    }

    public List<ParticipantTask> findByEnrolleeId(UUID enrolleeId) {
        return dao.findByEnrolleeId(enrolleeId);
    }

    public Map<UUID, List<ParticipantTask>> findByEnrolleeIds(List<UUID> enrolleeIds) {
        return dao.findByEnrolleeIds(enrolleeIds);
    }

    public List<ParticipantTask> findByPortalParticipantUserId(UUID ppUserId) {
        return dao.findByPortalParticipantUserId(ppUserId);
    }

    public List<ParticipantTask> findByStudyEnvironmentId(UUID studyEnvId) {
        return dao.findByStudyEnvironmentId(studyEnvId);
    }

    public List<ParticipantTask> findTasksByStudyAndTarget(UUID studyEnvId, List<String> targetStableIds) {
        return dao.findTasksByStudyAndTarget(studyEnvId, targetStableIds);
    }

    public void deleteByEnrolleeId(UUID enrolleeId) { dao.deleteByEnrolleeId(enrolleeId);}

    public Optional<ParticipantTask> authTaskToEnrolleeId(UUID taskId, UUID enrolleeId) {
        return dao.findByEnrolleeId(taskId, enrolleeId);
    }

    public Optional<ParticipantTask> findTaskForActivity(UUID ppUserId, UUID studyEnvironmentId, String activityStableId) {
        return dao.findTaskForActivity(ppUserId, studyEnvironmentId, activityStableId);
    }

    public Optional<ParticipantTask> findTaskForActivity(Enrollee enrollee, UUID studyEnvironmentId, String activityStableId) {
        return dao.findTaskForActivity(enrollee, studyEnvironmentId, activityStableId);
    }

    public Optional<ParticipantTask> findByKitRequestId(UUID kitRequestId) {
        return dao.findByKitRequestId(kitRequestId);
    }

    public Optional<ParticipantTask> findByConsentResponseId(UUID consentResponseId) {
        return dao.findByConsentResponseId(consentResponseId);
    }

    @Transactional
    @Override
    public ParticipantTask update(ParticipantTask task, DataAuditInfo dataAuditInfo) {
        if (task.getStatus().isTerminalStatus() && task.getCompletedAt() == null) {
            task.setCompletedAt(Instant.now());
        }
        return super.update(task, dataAuditInfo);
    }

    /**
     * applies the task updates to the given environment. Returns a list of the updated tasks This is
     * assumed to be a relatively rare operation, so this is not particularly optimized for
     * performance.
     */
    @Transactional
    public List<ParticipantTask> updateTasks(
            UUID studyEnvId,
            ParticipantTaskUpdateDto updateDto,
            ResponsibleEntity operator) {
        List<String> targetStableIds =
                updateDto.updates().stream().map(update -> update.targetStableId()).toList();
        List<ParticipantTask> participantTasks =
                findTasksByStudyAndTarget(studyEnvId, targetStableIds);
        List<ParticipantTask> tasksToUpdate =
                participantTasks.stream()
                        .filter(
                                task ->
                                        // take the task for updating if either we're updating all tasks, or if it's in
                                        // the user list
                                        updateDto.updateAll()
                                                || updateDto
                                                .portalParticipantUserIds()
                                                .contains(task.getPortalParticipantUserId()))
                        .toList();
        List<ParticipantTask> updatedTasks = new ArrayList<>();
        for (ParticipantTask task : tasksToUpdate) {
            ParticipantTaskUpdateDto.TaskUpdateSpec updateSpec =
                    updateDto.updates().stream()
                            .filter(update -> update.targetStableId().equals(task.getTargetStableId()))
                            .findFirst()
                            .orElseThrow(() -> new InternalServerException("unexpected query result"));
            ParticipantTask updatedTask = updateTask(task, updateSpec, operator);
            if (updatedTask != null) {
                updatedTasks.add(updatedTask);
            }
        }

        return updatedTasks;
    }

    protected ParticipantTask updateTask(
            ParticipantTask task,
            ParticipantTaskUpdateDto.TaskUpdateSpec updateSpec,
            ResponsibleEntity operator) {
        if (updateSpec.updateFromVersion() == null
                || updateSpec.updateFromVersion().equals(task.getTargetAssignedVersion())) {
            task.setTargetAssignedVersion(updateSpec.updateToVersion());
            if (updateSpec.newStatus() != null) {
                task.setStatus(updateSpec.newStatus());
            }
            DataAuditInfo auditInfo =
                    DataAuditInfo.builder()
                            .enrolleeId(task.getEnrolleeId())
                            .portalParticipantUserId(task.getPortalParticipantUserId())
                            .build();
            auditInfo.setResponsibleEntity(operator);
            return update(task, auditInfo);
        }
        return null;
    }

    public ParticipantTaskTaskListDto findAdminTasksByStudyEnvironmentId(UUID studyEnvId, List<String> includedRelations) {
        List<ParticipantTask> tasks = dao.findByStudyEnvironmentIdAndTaskType(studyEnvId, List.of(TaskType.ADMIN_NOTE, TaskType.ADMIN_FORM)).stream().toList();
        List<Enrollee> enrollees = List.of();
        List<ParticipantNote> notes = List.of();
        if (includedRelations.contains("enrollee")) {
            List<UUID> enrolleeIds = tasks.stream().map(ParticipantTask::getEnrolleeId).toList();
            enrollees = enrolleeService.findAll(enrolleeIds);
        }
        if (includedRelations.contains("participantNote")) {
            List<UUID> noteIds = tasks.stream().map(ParticipantTask::getParticipantNoteId).toList();
            notes = participantNoteService.findAll(noteIds);
        }
        return new ParticipantTaskTaskListDto(tasks, enrollees, notes);
    }

    public record ParticipantTaskTaskListDto(List<ParticipantTask> tasks, List<Enrollee> enrollees, List<ParticipantNote> participantNotes) {}
}
