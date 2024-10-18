package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.dao.admin.AdminUserDao;
import bio.terra.pearl.core.dao.dataimport.TimeShiftDao;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantNote;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.participant.ParticipantNoteService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import bio.terra.pearl.populate.dto.participant.ParticipantNotePopDto;
import bio.terra.pearl.populate.dto.participant.ParticipantTaskPopDto;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ParticipantNotePopulator {
    private final ParticipantTaskService participantTaskService;
    private final PortalParticipantUserService portalParticipantUserService;
    private final AdminUserDao adminUserDao;
    private final ParticipantNoteService participantNoteService;
    private final TimeShiftDao timeShiftDao;

    public ParticipantNotePopulator(AdminUserDao adminUserDao,
                                    ParticipantNoteService participantNoteService,
                                    TimeShiftDao timeShiftDao,
                                    ParticipantTaskService participantTaskService,
                                    PortalParticipantUserService portalParticipantUserService) {
        this.adminUserDao = adminUserDao;
        this.participantNoteService = participantNoteService;
        this.timeShiftDao = timeShiftDao;
        this.participantTaskService = participantTaskService;
        this.portalParticipantUserService = portalParticipantUserService;
    }

    public ParticipantNote populate(Enrollee enrollee, ParticipantNotePopDto notePopDto) {
        AdminUser creatingUser = adminUserDao.findByUsername(notePopDto.getCreatingAdminUsername()).orElseThrow();
        PortalParticipantUser portalParticipantUser = portalParticipantUserService.findForEnrollee(enrollee);
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
            timeShiftDao.changeParticipantNoteTime(participantNote.getId(), notePopDto.shiftedInstant());
        }

        if (notePopDto.getTask() != null) {
            ParticipantTaskPopDto taskDto = notePopDto.getTask();
            AdminUser assignedUser = adminUserDao.findByUsername(taskDto.getAssignedToUsername()).get();
            UUID taskCreatingUserId = creatingUser.getId();
            if (taskDto.getCreatingAdminUsername() != null) {
                taskCreatingUserId = adminUserDao.findByUsername(taskDto.getCreatingAdminUsername()).get().getId();
            }
            ParticipantTask adminTask = ParticipantTask.builder()
                    .studyEnvironmentId(enrollee.getStudyEnvironmentId())
                    .assignedAdminUserId(assignedUser.getId())
                    .participantNoteId(participantNote.getId())
                    .portalParticipantUserId(portalParticipantUser.getId())
                    .enrolleeId(enrollee.getId())
                    .taskType(TaskType.ADMIN_NOTE)
                    .status(taskDto.getStatus())
                    .build();
            adminTask = participantTaskService.create(adminTask, null);
            if (notePopDto.isTimeShifted() && !taskDto.isTimeShifted()) {
                timeShiftDao.changeAdminTaskCreationTime(participantNote.getId(), notePopDto.shiftedInstant());
            }
            if (taskDto.isTimeShifted()) {
                timeShiftDao.changeAdminTaskCreationTime(adminTask.getId(), taskDto.shiftedInstant());
            }
        }
        return participantNote;
    }

}
