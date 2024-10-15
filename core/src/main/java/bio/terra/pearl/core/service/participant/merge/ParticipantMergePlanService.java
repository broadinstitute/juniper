package bio.terra.pearl.core.service.participant.merge;

import bio.terra.pearl.core.dao.dataimport.MergeDao;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
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
import java.util.Objects;

@Service
public class ParticipantMergePlanService {
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

    public ParticipantMergePlanService(PortalParticipantUserService portalParticipantUserService,
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

    public ParticipantUserMerge planMerge(ParticipantUser participantUser, ParticipantUser mergeTarget, Portal portal) {
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
        portalParticipantUserService.attachProfiles(List.of(ppUser, ppMergeTarget));

        mergePlan.setPpUsers(new MergeAction(new MergePair<>(ppUser, ppMergeTarget), MergeAction.Action.DELETE_SOURCE));

        List<MergeAction<Enrollee, EnrolleeMerge>> enrollees = planMergePortalParticipantUsers(mergePlan, portal);

        mergePlan.setEnrollees(enrollees);
        return mergePlan;
    }

    protected List<MergeAction<Enrollee, EnrolleeMerge>> planMergePortalParticipantUsers(ParticipantUserMerge merge, Portal portal) {
        List<Enrollee> enrollees = enrolleeService.findByPortalParticipantUser(merge.getPpUsers().getSource());
        List<Enrollee> targetEnrollees = enrolleeService.findByPortalParticipantUser(merge.getPpUsers().getTarget());

        List<MergePair<Enrollee>> enrolleePairs = MergePair.pairLists(enrollees, targetEnrollees,
                        (e1, e2) -> e1.getStudyEnvironmentId().equals(e2.getStudyEnvironmentId()));

        List<MergeAction<Enrollee, EnrolleeMerge>> enrolleeMerges = new ArrayList<>();
        for (MergePair<Enrollee> enrolleePair : enrolleePairs) {
            if (enrolleePair.getPairType().equals(MergePair.PairType.TARGET_ONLY)) {
                enrolleeMerges.add(new MergeAction<>(enrolleePair, MergeAction.Action.NO_ACTION));
            } else if (enrolleePair.getPairType().equals(MergePair.PairType.SOURCE_ONLY)) {
                // move the enrollee to the target user
                enrolleeMerges.add(new MergeAction<>(enrolleePair, MergeAction.Action.MOVE_SOURCE));
            } else if (enrolleePair.getPairType().equals(MergePair.PairType.BOTH)){
                // this user has enrolled twice in the same study, we need a merge plan
                enrolleeMerges.add(new MergeAction<>(enrolleePair, MergeAction.Action.MERGE,
                        planMergeEnrollees(enrolleePair.getSource(), enrolleePair.getTarget())));
            }
        }
        return enrolleeMerges;
    }

    protected EnrolleeMerge planMergeEnrollees(Enrollee source, Enrollee target) {
        EnrolleeMerge enrolleeMerge = new EnrolleeMerge();
        List<ParticipantTask> sourceTasks = participantTaskService.findByEnrolleeId(source.getId());
        List<ParticipantTask> targetTasks = participantTaskService.findByEnrolleeId(target.getId());

        List<MergePair<ParticipantTask>> taskPairs = MergePair.pairLists(sourceTasks, targetTasks,
                (t1, t2) -> Objects.equals(t1.getTaskType(), t2.getTaskType()) &&
                        Objects.equals(t1.getTargetStableId(), t2.getTargetStableId()));
        for (MergePair<ParticipantTask> taskPair : taskPairs) {
            if (taskPair.getPairType().equals(MergePair.PairType.TARGET_ONLY)) {
                enrolleeMerge.getTasks().add(new MergeAction<>(taskPair, MergeAction.Action.NO_ACTION));
            } else if (taskPair.getPairType().equals(MergePair.PairType.SOURCE_ONLY)) {
                enrolleeMerge.getTasks().add(new MergeAction<>(taskPair, MergeAction.Action.MOVE_SOURCE));
            } else if (taskPair.getPairType().equals(MergePair.PairType.BOTH)) {
                // this task has been assigned twice, we need to determine a merge action based on content
                if (!hasMergeData(taskPair.getSource())) {
                    // if no source data, just drop the source task
                    enrolleeMerge.getTasks().add(new MergeAction<>(taskPair, MergeAction.Action.DELETE_SOURCE));
                } else if (!hasMergeData(taskPair.getTarget())) {
                    // if no target data, just drop the target task
                    enrolleeMerge.getTasks().add(new MergeAction<>(taskPair, MergeAction.Action.MOVE_SOURCE_DELETE_TARGET));
                } else {
                    // otherwise, we need to keep both  (eventually we might want to merge, but for now, keep everything)
                    enrolleeMerge.getTasks().add(new MergeAction<>(taskPair, MergeAction.Action.MOVE_SOURCE,
                            null));
                }
            }
        }
        return enrolleeMerge;
    }

    public static boolean hasMergeData(ParticipantTask task) {
        return task.getSurveyResponseId() != null || task.getKitRequestId() != null || task.getParticipantNoteId() != null;
    }
}
