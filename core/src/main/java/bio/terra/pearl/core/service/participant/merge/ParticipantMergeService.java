package bio.terra.pearl.core.service.participant.merge;

import bio.terra.pearl.core.dao.dataimport.MergeDao;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.audit.ParticipantDataChange;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.EnrolleeWithdrawalReason;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.survey.Answer;
import bio.terra.pearl.core.model.survey.SurveyResponse;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.kit.KitRequestService;
import bio.terra.pearl.core.service.participant.*;
import bio.terra.pearl.core.service.survey.AnswerService;
import bio.terra.pearl.core.service.survey.SurveyResponseService;
import bio.terra.pearl.core.service.workflow.ParticipantDataChangeService;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ParticipantMergeService {
    private final PortalParticipantUserService portalParticipantUserService;
    private final ParticipantTaskService participantTaskService;
    private final ParticipantDataChangeService participantDataChangeService;
    private final EnrolleeService enrolleeService;
    private final WithdrawnEnrolleeService withdrawnEnrolleeService;
    private final SurveyResponseService surveyResponseService;
    private final AnswerService answerService;
    private final KitRequestService kitRequestService;
    private final ParticipantUserService participantUserService;
    private final MergeDao mergeDao;

    public ParticipantMergeService(PortalParticipantUserService portalParticipantUserService,
                                   ParticipantTaskService participantTaskService,
                                   ParticipantDataChangeService participantDataChangeService,
                                   EnrolleeService enrolleeService,
                                   WithdrawnEnrolleeService withdrawnEnrolleeService,
                                   SurveyResponseService surveyResponseService,
                                   AnswerService answerService,
                                   KitRequestService kitRequestService,
                                   ParticipantUserService participantUserService,
                                   MergeDao mergeDao) {
        this.portalParticipantUserService = portalParticipantUserService;
        this.participantTaskService = participantTaskService;
        this.participantDataChangeService = participantDataChangeService;
        this.enrolleeService = enrolleeService;
        this.withdrawnEnrolleeService = withdrawnEnrolleeService;
        this.surveyResponseService = surveyResponseService;
        this.answerService = answerService;
        this.kitRequestService = kitRequestService;
        this.participantUserService = participantUserService;
        this.mergeDao = mergeDao;
    }

    @Transactional
    public ParticipantUserMerge applyMerge(ParticipantUserMerge merge, DataAuditInfo auditInfo) {
        for (MergeAction<Enrollee, EnrolleeMerge> enrolleeMerge : merge.getEnrollees()) {
            applyMergeEnrollee(enrolleeMerge, merge, auditInfo);
        }
        portalParticipantUserService.delete(merge.getPpUsers().getSource().getId(), CascadeProperty.EMPTY_SET);
        participantUserService.delete(merge.getUsers().getSource().getId(), CascadeProperty.EMPTY_SET);
        return merge;
    }

    protected void applyMergeEnrollee(MergeAction<Enrollee, EnrolleeMerge> mergeAction, ParticipantUserMerge parentMerge, DataAuditInfo auditInfo) {
        Enrollee source = validateEnrollee(mergeAction.getSource(), parentMerge.getUsers().getSource());
        Enrollee target = validateEnrollee(mergeAction.getTarget(), parentMerge.getUsers().getTarget());

        if (mergeAction.getAction().equals(MergeAction.Action.NO_ACTION)) {
            // nothing to do
        } else if (mergeAction.getAction().equals(MergeAction.Action.DELETE_SOURCE)) {
            // delete the source enrollee
            deleteMergedEnrollee(source, auditInfo);
        } else if (mergeAction.getAction().equals(MergeAction.Action.MOVE_SOURCE)) {
            // move the source enrollee to the target user
            moveEnrollee(source, parentMerge.getPpUsers().getTarget(), auditInfo);
        } else if (mergeAction.getAction().equals(MergeAction.Action.MERGE)) {
            // merge the source enrollee into the target enrollee
            mergeEnrolleeData(source, target, mergeAction.getMergePlan(), parentMerge.getPpUsers().getTarget(),  parentMerge.getUsers().getTarget(), auditInfo);
            deleteMergedEnrollee(source, auditInfo);
        }
    }

    private Enrollee validateEnrollee(Enrollee enrollee, ParticipantUser parent) {
        if (enrollee != null) {
            Enrollee fetched = enrolleeService.find(enrollee.getId()).orElseThrow();
            if (!fetched.getParticipantUserId().equals(parent.getId())) {
                throw new IllegalArgumentException("Enrollee not associated with participant");
            }
            return fetched;
        }
        return null;
    }

    protected void deleteMergedEnrollee(Enrollee enrollee, DataAuditInfo auditInfo) {
        withdrawnEnrolleeService.withdrawEnrollee(enrollee, EnrolleeWithdrawalReason.DUPLICATE, auditInfo);
    }

    /** merges data from one enrollee into another.  Does not delete the source enrollee */
    protected void mergeEnrolleeData(Enrollee source, Enrollee target, EnrolleeMerge mergePlan, PortalParticipantUser targetPpUser, ParticipantUser targetUser, DataAuditInfo auditInfo) {
        for (MergeAction<ParticipantTask,?> action : mergePlan.getTasks()) {
            ParticipantTask sourceTask = validateTask(action.getSource(), source);
            ParticipantTask targetTask = validateTask(action.getTarget(), target);
            if (action.getAction().equals(MergeAction.Action.DELETE_SOURCE)) {
                deleteTask(sourceTask, auditInfo);
            } else if (action.getAction().equals(MergeAction.Action.MOVE_SOURCE) ||
                    action.getAction().equals(MergeAction.Action.MERGE) ||
                    action.getAction().equals(MergeAction.Action.MOVE_SOURCE_DELETE_TARGET)) {
                moveTask(sourceTask.getId(), target, targetPpUser, auditInfo);
                if (action.getAction().equals(MergeAction.Action.MOVE_SOURCE_DELETE_TARGET)) {
                    deleteTask(targetTask, auditInfo);
                }
            } else if (action.getAction().equals(MergeAction.Action.NO_ACTION)) {
                // nothing to do
            } else {
                throw new IllegalArgumentException("Unexpected action in ParticipantTask merge plan");
            }
        }

        // for now, we reassign all notifications and events to the target enrollee
        mergeDao.reassignEnrolleeNotifications(source.getId(), target.getId(), targetUser.getId());
        mergeDao.reassignEnrolleeEvents(source.getId(), target.getId());
        mergeDao.reassignParticipantNotes(source.getId(), target.getId());
        mergeDao.reassignFamily(source.getId(), target.getId());
        reassignDataChanges(source, target, targetPpUser);
    }

    private ParticipantTask validateTask(ParticipantTask task, Enrollee enrollee) {
        if (task != null) {
            ParticipantTask fetched = participantTaskService.find(task.getId()).orElseThrow();
            if (!task.getEnrolleeId().equals(enrollee.getId())) {
                throw new IllegalArgumentException("Task %s not associated with enrollee".formatted(task.getId()));
            }
            return fetched;
        }
        return null;
    }

    private void moveTask(UUID taskId, Enrollee targetEnrollee, PortalParticipantUser targetPpUser, DataAuditInfo auditInfo) {
        // refetch the target task to make sure we have the latest version and for safety (since the task
        // may come from a Merge object from the client), the reassign ids
        ParticipantTask task = participantTaskService.find(taskId).orElseThrow();
        task.setEnrolleeId(targetEnrollee.getId());
        task.setPortalParticipantUserId(targetPpUser.getId());
        participantTaskService.update(task, auditInfo);

        if (task.getSurveyResponseId() != null) {
            moveResponse(task.getSurveyResponseId(), targetEnrollee, targetPpUser, auditInfo);
        } else if (task.getKitRequestId() != null) {
            moveKitRequest(task.getKitRequestId(), targetEnrollee, targetPpUser, auditInfo);
        }
    }

    private void deleteTask(ParticipantTask task, DataAuditInfo auditInfo) {
        // for now, we only support deleting tasks with no data attached
        task = participantTaskService.find(task.getId()).orElseThrow();
        if (ParticipantMergePlanService.hasMergeData(task)) {
            throw new IllegalArgumentException("Cannot delete task %s with data".formatted(task.getId()));
        }
        participantTaskService.delete(task.getId(), auditInfo);
    }

    private void moveResponse(UUID responseId, Enrollee targetEnrollee, PortalParticipantUser targetPpUser, DataAuditInfo auditInfo) {
        SurveyResponse response = surveyResponseService.find(responseId).orElseThrow();
        response.setEnrolleeId(targetEnrollee.getId());
        if (response.getCreatingParticipantUserId() != null) {
            response.setCreatingParticipantUserId(targetPpUser.getParticipantUserId());
        }
        surveyResponseService.update(response);
        List<Answer> answers = answerService.findByResponse(responseId);
        for (Answer answer : answers) {
            answer.setEnrolleeId(targetEnrollee.getId());
            if (answer.getCreatingParticipantUserId() != null) {
                answer.setCreatingParticipantUserId(targetPpUser.getParticipantUserId());
            }
            answerService.update(answer);
        }
    }

    private void moveKitRequest(UUID kitRequestId, Enrollee targetEnrollee, PortalParticipantUser targetPpUser, DataAuditInfo auditInfo) {
        KitRequest kitRequest = kitRequestService.find(kitRequestId).orElseThrow();
        kitRequest.setEnrolleeId(targetEnrollee.getId());
        kitRequestService.update(kitRequest);
    }

    private void moveEnrollee(Enrollee enrollee, PortalParticipantUser targetPpUser, DataAuditInfo auditInfo) {
        enrollee.setParticipantUserId(targetPpUser.getParticipantUserId());
        enrollee.setProfileId(targetPpUser.getProfileId());
        enrolleeService.update(enrollee);

        // merge the data from enrollee into targetEnrollee
        // delete the enrollee
        List<ParticipantTask> tasks = participantTaskService.findByEnrolleeId(enrollee.getId());
        for (ParticipantTask task : tasks) {
            // this is a task that has data -- move it to the target
            task.setPortalParticipantUserId(targetPpUser.getId());
            participantTaskService.update(task, auditInfo);
        }

        // copy across the survey responses
        List<SurveyResponse> responses = surveyResponseService.findByEnrolleeId(enrollee.getId());
        for (SurveyResponse response : responses) {
            if (response.getCreatingParticipantUserId() != null) {
                response.setCreatingParticipantUserId(targetPpUser.getParticipantUserId());
                surveyResponseService.update(response);
            }
        }
        reassignDataChanges(enrollee, enrollee, targetPpUser);
    }

    protected void reassignDataChanges(Enrollee sourceEnrollee, Enrollee targetEnrollee, PortalParticipantUser targetPpUser) {
        List<ParticipantDataChange> participantDataChanges = participantDataChangeService.findByEnrollee(sourceEnrollee.getId());
        for (ParticipantDataChange participantDataChange : participantDataChanges) {
            participantDataChange.setEnrolleeId(targetEnrollee.getId());
            if (participantDataChange.getPortalParticipantUserId() != null) {
                participantDataChange.setPortalParticipantUserId(targetPpUser.getId());
            }
            if (participantDataChange.getResponsibleUserId() != null) {
                participantDataChange.setResponsibleUserId(targetPpUser.getParticipantUserId());
            }
            mergeDao.updateParticipantDataChange(participantDataChange);
        }
    }
}
