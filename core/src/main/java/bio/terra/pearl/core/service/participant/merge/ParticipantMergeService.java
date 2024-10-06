package bio.terra.pearl.core.service.participant.merge;

import bio.terra.pearl.core.dao.dataimport.MergeDao;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.audit.ParticipantDataChange;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.participant.Enrollee;
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
        if (mergeAction.getAction().equals(MergeAction.Action.NO_ACTION)) {
            // nothing to do
        } else if (mergeAction.getAction().equals(MergeAction.Action.DELETE_SOURCE)) {
            // delete the source enrollee
            deleteMergedEnrollee(mergeAction.getSource(), auditInfo);
        } else if (mergeAction.getAction().equals(MergeAction.Action.MOVE_SOURCE)) {
            // move the source enrollee to the target user
            moveEnrollee(mergeAction.getSource(), parentMerge.getPpUsers().getTarget(), auditInfo);
        } else if (mergeAction.getAction().equals(MergeAction.Action.MERGE)) {
            // merge the source enrollee into the target enrollee
            mergeEnrolleeData(mergeAction.getSource(), mergeAction.getTarget(), mergeAction.getMergePlan(), parentMerge.getPpUsers().getTarget(), auditInfo);
            deleteMergedEnrollee(mergeAction.getSource(), auditInfo);
        }
    }

    protected void deleteMergedEnrollee(Enrollee enrollee, DataAuditInfo auditInfo) {
        withdrawnEnrolleeService.withdrawEnrollee(enrollee, auditInfo);
    }

    /** merges data from one enrollee into another.  Does not delete the source enrollee */
    protected void mergeEnrolleeData(Enrollee source, Enrollee target, EnrolleeMerge mergePlan, PortalParticipantUser targetPpUser, DataAuditInfo auditInfo) {
        for (MergeAction<ParticipantTask,?> action : mergePlan.getTasks()) {
            if (action.getAction().equals(MergeAction.Action.DELETE_SOURCE)) {
                participantTaskService.delete(action.getSource().getId(), auditInfo);
            } else if (action.getAction().equals(MergeAction.Action.MOVE_SOURCE) || action.getAction().equals(MergeAction.Action.MERGE)) {
                // For now 'merge' is the same as 'move' -- we don't have a plan for merging tasks
                // refetch the target task to make sure we have the latest version and for safety, the reassign ids
                ParticipantTask task = participantTaskService.find(action.getSource().getId()).orElseThrow();
                task.setEnrolleeId(target.getId());
                task.setPortalParticipantUserId(targetPpUser.getId());
                participantTaskService.update(task, auditInfo);
            } else if (action.getAction().equals(MergeAction.Action.NO_ACTION)) {
                // nothing to do
            } else {
                throw new IllegalArgumentException("Unexpected action in ParticipantTask merge plan");
            }
        }

        for (MergeAction<SurveyResponse, ?> action : mergePlan.getSurveyResponses()) {
            if (action.getAction().equals(MergeAction.Action.MOVE_SOURCE) || action.getAction().equals(MergeAction.Action.MERGE)) {
                // For now 'merge' is the same as 'move' -- we don't have a strategy for merging responses
                // refetch the response to make sure we have the latest version and for safety, the reassign ids
                SurveyResponse response = surveyResponseService.find(action.getSource().getId()).orElseThrow();
                response.setEnrolleeId(target.getId());
                if (response.getCreatingParticipantUserId() != null) {
                    response.setCreatingParticipantUserId(targetPpUser.getParticipantUserId());
                }
                surveyResponseService.update(response);
            } else if (action.getAction().equals(MergeAction.Action.NO_ACTION)) {
                // nothing to do
            } else {
                throw new IllegalArgumentException("Unexpected action in SurveyResponse merge plan");
            }
        }
        // for now, just copy over all answers.  Later this will be folded into how we merge responses
        List<Answer> answers = answerService.findByEnrollee(source.getId());
        for (Answer answer : answers) {
            answer.setEnrolleeId(target.getId());
            if (answer.getCreatingParticipantUserId() != null) {
                answer.setCreatingParticipantUserId(targetPpUser.getParticipantUserId());
            }
            answerService.update(answer);
        }

        for (MergeAction<KitRequest, ?> action : mergePlan.getKitRequests()) {
            if (action.getAction().equals(MergeAction.Action.MOVE_SOURCE)) {
                // refetch the kitrequest to make sure we have the latest version and for safety, the reassign ids
                KitRequest request = kitRequestService.find(action.getSource().getId()).orElseThrow();
                request.setEnrolleeId(target.getId());
                kitRequestService.update(request);
            } else if (action.getAction().equals(MergeAction.Action.NO_ACTION)) {
                // nothing to do
            } else {
                throw new IllegalArgumentException("Unexpected action in KitRequest merge plan");
            }
        }

        mergeDao.reassignEnrolleeNotifications(source.getId(), target.getId());
        mergeDao.reassignEnrolleeEvents(source.getId(), target.getId());

        reassignDataChanges(source, target, targetPpUser);
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

        List<Answer> answers = answerService.findByEnrollee(enrollee.getId());
        for (Answer answer : answers) {
            if (answer.getCreatingParticipantUserId() != null) {
                answer.setCreatingParticipantUserId(targetPpUser.getParticipantUserId());
            }
            answerService.update(answer);
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
