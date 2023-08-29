package bio.terra.pearl.core.service.workflow;

import bio.terra.pearl.core.dao.workflow.AdminTaskDao;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantNote;
import bio.terra.pearl.core.model.workflow.AdminTask;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.service.CrudService;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.ParticipantNoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminTaskService extends CrudService<AdminTask, AdminTaskDao> {
    private static final Logger logger = LoggerFactory.getLogger(AdminTaskService.class);
    private EnrolleeService enrolleeService;
    private ParticipantNoteService participantNoteService;

    public AdminTaskService(AdminTaskDao dao, @Lazy EnrolleeService enrolleeService, ParticipantNoteService participantNoteService) {
        super(dao);
        this.enrolleeService = enrolleeService;
        this.participantNoteService = participantNoteService;
    }

    public List<AdminTask> findByEnrolleeId(UUID enrolleeId) {
        return dao.findByEnrolleeId(enrolleeId);
    }

    @Transactional
    public void deleteByEnrolleId(UUID enrolleeId) { dao.deleteByEnrolleeId(enrolleeId); }
    @Transactional
    public void deleteByStudyEnvironmentId(UUID studyEnvId) { dao.deleteByStudyEnvironmentId(studyEnvId);}

    public List<AdminTask> findByAssignee(UUID adminUserId) {
        return dao.findByAssignee(adminUserId);
    }

    public AdminTaskListDto findByStudyEnvironmentId(UUID studyEnvId) {
        return findByStudyEnvironmentId(studyEnvId, List.of());
    }

    public AdminTaskListDto findByStudyEnvironmentId(UUID studyEnvId, List<String> includedRelations) {
        List<AdminTask> tasks = dao.findByStudyEnvironmentId(studyEnvId);
        List<Enrollee> enrollees = List.of();
        List<ParticipantNote> notes = List.of();
        if (includedRelations.contains("enrollee")) {
            List<UUID> enrolleeIds = tasks.stream().map(AdminTask::getEnrolleeId).toList();
            enrollees = enrolleeService.findAll(enrolleeIds);
        }
        if (includedRelations.contains("participantNote")) {
            List<UUID> noteIds = tasks.stream().map(AdminTask::getParticipantNoteId).toList();
            notes = participantNoteService.findAll(noteIds);
        }
        return new AdminTaskListDto(tasks, enrollees, notes);
    }

    @Transactional
    @Override
    public AdminTask update(AdminTask task) {
        if (task.getStatus().isTerminalStatus() && task.getCompletedAt() == null) {
            task.setCompletedAt(Instant.now());
        }
        return dao.update(task);
    }

    public record AdminTaskListDto(List<AdminTask> tasks, List<Enrollee> enrollees, List<ParticipantNote> participantNotes) {}
}


