package bio.terra.pearl.core.service.workflow.merge;

import bio.terra.pearl.core.dao.dataimport.MergeDao;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.audit.ParticipantDataChange;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.Answer;
import bio.terra.pearl.core.model.survey.SurveyResponse;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.kit.KitRequestService;
import bio.terra.pearl.core.service.notification.NotificationService;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.participant.ProfileService;
import bio.terra.pearl.core.service.participant.WithdrawnEnrolleeService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.survey.AnswerService;
import bio.terra.pearl.core.service.survey.SurveyResponseService;
import bio.terra.pearl.core.service.workflow.ParticipantDataChangeService;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ParticipantMergeService {
    private final PortalParticipantUserService portalParticipantUserService;
    private final ParticipantTaskService participantTaskService;
    private final ParticipantDataChangeService participantDataChangeService;
    private final ProfileService profileService;
    private final EnrolleeService enrolleeService;
    private final StudyEnvironmentService studyEnvironmentService;
    private final WithdrawnEnrolleeService withdrawnEnrolleeService;
    private final SurveyResponseService surveyResponseService;
    private final AnswerService answerService;
    private final KitRequestService kitRequestService;
    private final NotificationService notificationService;
    private final MergeDao mergeDao;

    public ParticipantMergeService(PortalParticipantUserService portalParticipantUserService,
                                   ParticipantTaskService participantTaskService,
                                   ParticipantDataChangeService participantDataChangeService,
                                   ProfileService profileService,
                                   EnrolleeService enrolleeService,
                                   StudyEnvironmentService studyEnvironmentService,
                                   WithdrawnEnrolleeService withdrawnEnrolleeService,
                                   SurveyResponseService surveyResponseService,
                                   AnswerService answerService,
                                   KitRequestService kitRequestService,
                                   NotificationService notificationService,
                                   MergeDao mergeDao) {
        this.portalParticipantUserService = portalParticipantUserService;
        this.participantTaskService = participantTaskService;
        this.participantDataChangeService = participantDataChangeService;
        this.profileService = profileService;
        this.enrolleeService = enrolleeService;
        this.studyEnvironmentService = studyEnvironmentService;
        this.withdrawnEnrolleeService = withdrawnEnrolleeService;
        this.surveyResponseService = surveyResponseService;
        this.answerService = answerService;
        this.kitRequestService = kitRequestService;
        this.notificationService = notificationService;
        this.mergeDao = mergeDao;
    }

    @Transactional
    public ParticipantUserMerge planMerge(ParticipantUser participantUser, ParticipantUser mergeTarget, Portal portal, DataAuditInfo auditInfo) {
        if (!participantUser.getEnvironmentName().equals(mergeTarget.getEnvironmentName())) {
            throw new IllegalArgumentException("ParticipantUsers must be in the same environment to merge");
        }

        ParticipantUserMerge mergePlan = ParticipantUserMerge.builder()
            .users(new MergeAction<>(new MergePair<>(participantUser, mergeTarget), MergeAction.Action.DELETE_SOURCE))
            .build();

        PortalParticipantUser ppUser = portalParticipantUserService.findOne(participantUser.getId(), portal.getShortcode())
                .orElseThrow(() -> new IllegalArgumentException("ParticipantUser not found in portal"));
        PortalParticipantUser ppMergeTarget = portalParticipantUserService.findOne(mergeTarget.getId(), portal.getShortcode())
                .orElseThrow(() -> new IllegalArgumentException("ParticipantUser not found in portal"));

        mergePlan.setPpUsers(new MergeAction(new MergePair<>(ppUser, ppMergeTarget), MergeAction.Action.DELETE_SOURCE));

        List<MergeAction<Enrollee, EnrolleeMerge>> enrollees = planMergePortalParticipantUsers(mergePlan, portal, auditInfo);

        mergePlan.setEnrollees(enrollees);
        return mergePlan;
    }

    protected List<MergeAction<Enrollee, EnrolleeMerge>> planMergePortalParticipantUsers(ParticipantUserMerge merge, Portal portal, DataAuditInfo auditInfo) {
        List<Enrollee> enrollees = enrolleeService.findByPortalParticipantUser(merge.getPpUsers().getPair().getSource());
        List<Enrollee> targetEnrollees = enrolleeService.findByPortalParticipantUser(merge.getPpUsers().getPair().getTarget());
        List<StudyEnvironment> studyEnvironments = studyEnvironmentService.findAllByPortalAndEnvironment(portal.getId(), merge.getUsers().getPair().getTarget().getEnvironmentName());
        List<MergePair<Enrollee>> enrolleePairs = studyEnvironments.stream()
                .map(studyEnv -> {
                    Enrollee enrollee = enrollees.stream()
                            .filter(e -> e.getStudyEnvironmentId().equals(studyEnv.getId())).findFirst().orElse(null);
                    Enrollee targetEnrollee = targetEnrollees.stream()
                            .filter(target -> enrollee.getStudyEnvironmentId().equals(target.getStudyEnvironmentId())).findFirst().orElse(null);
                    return new MergePair<Enrollee>(enrollee, targetEnrollee);
                })
                .toList();

        List<MergeAction<Enrollee, EnrolleeMerge>> enrolleeMerges = new ArrayList<>();
        for (MergePair<Enrollee> enrolleePair : enrolleePairs) {
            if (enrolleePair.getSource() == null && enrolleePair.getTarget() != null) {
                // nothing to do, there's no data that needs to be merged for this study
            } else if (enrolleePair.getSource() == null && enrolleePair.getTarget() != null) {
                enrolleeMerges.add(new MergeAction<>(enrolleePair, MergeAction.Action.NO_ACTION));
            } else if (enrolleePair.getSource() != null && enrolleePair.getTarget() == null) {
                // move the enrollee to the target user
                enrolleeMerges.add(new MergeAction<>(enrolleePair, MergeAction.Action.MOVE_SOURCE));
            } else {
                // this user has enrolled twice in the same study, we need a merge plan
                enrolleeMerges.add(new MergeAction<>(enrolleePair, MergeAction.Action.MERGE,
                        planMergeEnrollees(enrolleePair.getSource(), enrolleePair.getTarget())));
            }
        }
        return enrolleeMerges;
    }

    protected EnrolleeMerge planMergeEnrollees(Enrollee source, Enrollee target) {
        EnrolleeMerge enrolleeMerge = EnrolleeMerge.builder()
            .enrollees(new MergePair<>(source, target))
            .build();

        List<ParticipantTask> sourceTasks = participantTaskService.findByEnrolleeId(source.getId());
        List<ParticipantTask> targetTasks = participantTaskService.findByEnrolleeId(source.getId());
        List<SurveyResponse> sourceResponses = surveyResponseService.findByEnrolleeId(source.getId());
        List<SurveyResponse> targetResponses = surveyResponseService.findByEnrolleeId(target.getId());

        // find a matched task, if one.  Either way, we'll move the source tasks to target if they're non-empty
        for (ParticipantTask task : sourceTasks) {
            ParticipantTask targetTask = targetTasks.stream()
                    .filter(t -> t.getTargetStableId().equals(task.getTargetStableId()))
                    .findFirst().orElse(null);
            if (targetTask != null) {
                targetTasks.remove(targetTask);
            }
            MergeAction.Action action = MergeAction.Action.MOVE_SOURCE;
            if (task.getSurveyResponseId() == null && task.getKitRequestId() == null) {
                action = MergeAction.Action.DELETE_SOURCE;
            } else if (task.getSurveyResponseId() != null) {
                MergePair<SurveyResponse> surveyResponsePair = new MergePair<>(
                        sourceResponses.stream().filter(r -> r.getId().equals(task.getSurveyResponseId())).findFirst().get(), null);
                if (targetTask != null && targetTask.getSurveyResponseId() != null) {
                    surveyResponsePair.setTarget(targetResponses.stream().filter(r -> r.getId().equals(targetTask.getSurveyResponseId())).findFirst().get());
                }
                enrolleeMerge.getSurveyResponses().add(new MergeAction<>(surveyResponsePair, MergeAction.Action.MOVE_SOURCE));
            }
            enrolleeMerge.getTasks().add(new MergeAction<>(new MergePair<>(task, targetTask), action));
        }
        // for any unmatched target tasks, just keep them and their responses
        for (ParticipantTask task : targetTasks) {
            enrolleeMerge.getTasks().add(new MergeAction<>(new MergePair<>(null, task), MergeAction.Action.NO_ACTION));
            if (task.getSurveyResponseId() != null) {
                enrolleeMerge.getSurveyResponses().add(new MergeAction<>(new MergePair<>(null, targetResponses.stream().filter(r -> r.getId().equals(task.getSurveyResponseId())).findFirst().get()), MergeAction.Action.NO_ACTION));
            }
        }

        // kit requests are always treated as unique, so we'll just move them all
        List<KitRequest> sourceKits = kitRequestService.findByEnrolleeRaw(source);
        for (KitRequest kit : sourceKits) {
            enrolleeMerge.getKitRequests().add(new MergeAction<>(new MergePair<>(kit, null), MergeAction.Action.MOVE_SOURCE));
        }
        List<KitRequest> targetKits = kitRequestService.findByEnrolleeRaw(target);
        for (KitRequest kit : targetKits) {
            enrolleeMerge.getKitRequests().add(new MergeAction<>(new MergePair<>(null, kit), MergeAction.Action.NO_ACTION));
        }
        return enrolleeMerge;
    }



    protected ParticipantUserMerge applyMerge(ParticipantUserMerge merge, DataAuditInfo auditInfo) {
        for (MergeAction<Enrollee, EnrolleeMerge> enrolleeMerge : merge.getEnrollees()) {
            applyMergeEnrollee(enrolleeMerge, auditInfo);
        }
        return merge;
    }

    protected void applyMergeEnrollee(MergeAction<Enrollee, EnrolleeMerge> enrolleeMerge, DataAuditInfo auditInfo) {
        if (enrolleeMerge.getAction().equals(MergeAction.Action.NO_ACTION)) {
            // nothing to do
            return;
        }

        if (enrolleeMerge.getAction().equals(MergeAction.Action.DELETE_SOURCE)) {
            // delete the source enrollee
            Enrollee source = enrolleeMerge.getPair().getSource();
            withdrawnEnrolleeService.withdrawEnrollee(source, auditInfo);
            enrolleeService.delete(source.getId(), CascadeProperty.EMPTY_SET);
            return;
        }

        if (enrollee != null && targetEnrollee == null) {
            // move the enrollee to the target user
            enrollee.setParticipantUserId(mergeTarget.getId());
            enrollee.setProfileId(ppMergeTarget.getProfileId());
            enrolleeService.update(enrollee);
            return;
        }

        // merge the data from enrollee into targetEnrollee
        // delete the enrollee
        List<ParticipantTask> tasks = participantTaskService.findByEnrolleeId(enrollee.getId());
        for (ParticipantTask task : tasks) {
            if (task.getSurveyResponseId() != null || task.getKitRequestId() != null) {
              // this is a task that has data -- move it to the target
              task.setEnrolleeId(targetEnrollee.getId());
              task.setPortalParticipantUserId(ppMergeTarget.getId());
              participantTaskService.update(task, auditInfo);
            }
        }

        // copy across the survey responses
        List<SurveyResponse> responses = surveyResponseService.findByEnrolleeId(enrollee.getId());
        for (SurveyResponse response : responses) {
            response.setEnrolleeId(targetEnrollee.getId());
            if (response.getCreatingParticipantUserId() != null) {
                response.setCreatingParticipantUserId(mergeTarget.getId());
            }
            surveyResponseService.update(response);
        }

        List<Answer> answers = answerService.findByEnrollee(enrollee.getId());
        for (Answer answer : answers) {
            answer.setEnrolleeId(targetEnrollee.getId());
            if (answer.getCreatingParticipantUserId() != null) {
                answer.setCreatingParticipantUserId(mergeTarget.getId());
            }
            answerService.update(answer);
        }

        List<KitRequest> kitRequests = kitRequestService.findByEnrolleeRaw(enrollee);
        for (KitRequest kitRequest : kitRequests) {
            kitRequest.setEnrolleeId(targetEnrollee.getId());
            kitRequestService.update(kitRequest);
        }

        List<ParticipantDataChange> participantDataChanges = participantDataChangeService.findByEnrollee(enrollee.getId());
        for (ParticipantDataChange participantDataChange : participantDataChanges) {
            participantDataChange.setEnrolleeId(targetEnrollee.getId());
            if (participantDataChange.getPortalParticipantUserId() != null) {
                participantDataChange.setPortalParticipantUserId(ppMergeTarget.getId());
            }
            if (participantDataChange.getResponsibleUserId() != null) {
                participantDataChange.setResponsibleUserId(mergeTarget.getId());
            }
            mergeDao.updateParticipantDataChange(participantDataChange);
        }

        mergeDao.reassignEnrolleeNotifications(enrollee.getId(), targetEnrollee.getId());
        mergeDao.reassignEnrolleeEvents(enrollee.getId(), targetEnrollee.getId());
    }
}
