package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.dao.admin.AdminUserDao;
import bio.terra.pearl.core.dao.workflow.AdminTaskDao;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantNote;
import bio.terra.pearl.core.model.workflow.AdminTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.service.participant.ParticipantNoteService;
import bio.terra.pearl.core.service.workflow.AdminTaskService;
import bio.terra.pearl.populate.dao.TimeShiftPopulateDao;
import bio.terra.pearl.populate.dto.participant.ParticipantNotePopDto;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ParticipantNotePopulator {
    private AdminUserDao adminUserDao;
    private ParticipantNoteService participantNoteService;
    private TimeShiftPopulateDao timeShiftPopulateDao;
    private AdminTaskService adminTaskService;

    public ParticipantNotePopulator(AdminUserDao adminUserDao,
                                    ParticipantNoteService participantNoteService,
                                    TimeShiftPopulateDao timeShiftPopulateDao,
                                    AdminTaskService adminTaskService) {
        this.adminUserDao = adminUserDao;
        this.participantNoteService = participantNoteService;
        this.timeShiftPopulateDao = timeShiftPopulateDao;
        this.adminTaskService = adminTaskService;
    }

    public ParticipantNote populate(Enrollee enrollee, ParticipantNotePopDto notePopDto) {
        AdminUser creatingUser = adminUserDao.findByUsername(notePopDto.getCreatingAdminUsername()).get();
        UUID kitRequestId = null;
        if (notePopDto.getKitRequestIndex() != null) {
            kitRequestId = enrollee.getKitRequests().get(notePopDto.getKitRequestIndex()).getId();
        }
        ParticipantNote participantNote = ParticipantNote.builder()
                .enrolleeId(enrollee.getId())
                .creatingAdminUserId(creatingUser.getId())
                .kitRequestId(kitRequestId)
                .text(notePopDto.getText())
                .build();
        participantNote = participantNoteService.create(participantNote);
        if (notePopDto.isTimeShifted()) {
            timeShiftPopulateDao.changeParticipantNoteTime(participantNote.getId(), notePopDto.shiftedInstant());
        }

        if (notePopDto.getAssignedToUsername() != null) {
            AdminUser assignedUser = adminUserDao.findByUsername(notePopDto.getAssignedToUsername()).get();
            AdminTask adminTask = AdminTask.builder()
                    .studyEnvironmentId(enrollee.getStudyEnvironmentId())
                    .creatingAdminUserId(creatingUser.getId())
                    .assignedAdminUserId(assignedUser.getId())
                    .participantNoteId(participantNote.getId())
                    .enrolleeId(enrollee.getId())
                    .taskType(notePopDto.getTaskType())
                    .status(TaskStatus.NEW)
                    .build();
            adminTask = adminTaskService.create(adminTask);
            if (notePopDto.isTimeShifted()) {
                timeShiftPopulateDao.changeAdminTaskCreationTime(adminTask.getId(), notePopDto.shiftedInstant());
            }
        }
        return participantNote;
    }

}
