package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.dao.survey.SurveyResponseDao;
import bio.terra.pearl.core.model.consent.ConsentResponse;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.survey.*;
import bio.terra.pearl.core.model.workflow.HubResponse;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.CrudService;
import bio.terra.pearl.core.service.TransactionHandler;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.ParticipantTaskService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.study.StudyEnvironmentSurveyService;
import bio.terra.pearl.core.service.workflow.EnrolleeEventService;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SurveyResponseService extends CrudService<SurveyResponse, SurveyResponseDao> {
    private ResponseSnapshotService responseSnapshotService;
    private EnrolleeService enrolleeService;
    private SurveyService surveyService;
    private ParticipantTaskService participantTaskService;
    private StudyEnvironmentSurveyService studyEnvironmentSurveyService;
    private PortalParticipantUserService portalParticipantUserService;
    private TransactionHandler transactionHandler;
    private EnrolleeEventService enrolleeEventService;

    public SurveyResponseService(SurveyResponseDao dao, ResponseSnapshotService responseSnapshotService,
                                 EnrolleeService enrolleeService, SurveyService surveyService,
                                 ParticipantTaskService participantTaskService,
                                 StudyEnvironmentSurveyService studyEnvironmentSurveyService,
                                 PortalParticipantUserService portalParticipantUserService,
                                 TransactionHandler transactionHandler, EnrolleeEventService enrolleeEventService) {
        super(dao);
        this.responseSnapshotService = responseSnapshotService;
        this.enrolleeService = enrolleeService;
        this.surveyService = surveyService;
        this.participantTaskService = participantTaskService;
        this.studyEnvironmentSurveyService = studyEnvironmentSurveyService;
        this.portalParticipantUserService = portalParticipantUserService;
        this.transactionHandler = transactionHandler;
        this.enrolleeEventService = enrolleeEventService;
    }

    public List<SurveyResponse> findByEnrolleeId(UUID enrolleeId) {
        return dao.findByEnrolleeId(enrolleeId);
    }

    public Optional<SurveyResponse> findOneWithLastSnapshot(UUID responseId) {
        return dao.findOneWithLastSnapshot(responseId);
    }

    @Override
    public SurveyResponse create(SurveyResponse response) {
        SurveyResponse savedResponse = super.create(response);
        ResponseSnapshot lastSnapshot = null;
        for (ResponseSnapshot snapshot : response.getSnapshots()) {
            snapshot.setSurveyResponseId(savedResponse.getId());
            // if no explicit user is specified on the snapshot, it's the same as the user of the response
            if (snapshot.getCreatingAdminUserId() == null && snapshot.getCreatingParticipantUserId() == null) {
                snapshot.setCreatingParticipantUserId(response.getCreatingParticipantUserId());
                snapshot.setCreatingAdminUserId(response.getCreatingAdminUserId());
            }
            ResponseSnapshot savedSnap = responseSnapshotService.create(snapshot);
            savedResponse.getSnapshots().add(savedSnap);
            if (lastSnapshot == null || savedSnap.getCreatedAt().isAfter(lastSnapshot.getCreatedAt())) {
                lastSnapshot = savedSnap;
            }
        }
        if (lastSnapshot != null) {
            savedResponse.setLastSnapshotId(lastSnapshot.getId());
            savedResponse = dao.update(savedResponse);
            savedResponse.setLastSnapshot(lastSnapshot);
        }
        return savedResponse;
    }

    /**
     * will load the survey and the  surveyResponse associated with the task,
     * or the most recent survey response, with the lastSnapshot attached.
     * @param taskId (optional) a task associated with this retrieval -- will be used to help identify a
     *               specific response in cases where the enrollee has multiple responses
     */
    public SurveyWithResponse findWithActiveResponse(UUID studyEnvId, String stableId, Integer version,
                                                     String enrolleeShortcode, UUID participantUserId, UUID taskId) {
        Enrollee enrollee = enrolleeService.findOneByShortcode(enrolleeShortcode).get();
        enrolleeService.authParticipantUserToEnrollee(participantUserId, enrollee.getId());
        Survey form = surveyService.findByStableId(stableId, version).get();
        SurveyResponse lastResponse = null;
        if (taskId != null) {
            // if there is an associated task, try to find an associated response
            Optional<ParticipantTask> attachedTask = participantTaskService.find(taskId);
            if (attachedTask.isPresent() && attachedTask.get().getSurveyResponseId() != null) {
                lastResponse = dao.findOneWithLastSnapshot(attachedTask.get().getSurveyResponseId()).orElse(null);
            }
        }
        if (lastResponse == null) {
            // if there's no response already associated with the task, grab the most recently created
            lastResponse = dao.findMostRecent(enrollee.getId(), form.getId()).orElse(null);
            if (lastResponse != null && lastResponse.getLastSnapshotId() != null) {
                lastResponse.setLastSnapshot(responseSnapshotService.find(lastResponse.getLastSnapshotId())
                        .orElse(null));
            }
        }
        // TODO -- this lookup should be by stableId, not formId
        StudyEnvironmentSurvey configSurvey = studyEnvironmentSurveyService
                .findBySurvey(studyEnvId, form.getId()).get();
        configSurvey.setSurvey(form);
        return new SurveyWithResponse(
                configSurvey, lastResponse
        );
    }

    /**
     * Creates a survey response and fires appropriate downstream events.  Note this method is *not*
     * transactional, as if an error occurs in downstream event processing, we still want to save the survey data
     */
    @Transactional
    public HubResponse<ConsentResponse> submitResponse(UUID participantUserId, PortalParticipantUser ppUser,
                                                       String enrolleeShortcode, UUID taskId,
                                                       ResponseSnapshotDto snapDto) {

        Enrollee enrollee = enrolleeService.authParticipantUserToEnrollee(participantUserId, enrolleeShortcode);
        ParticipantTask task = participantTaskService.authTaskToPortalParticipantUser(taskId, ppUser.getId()).get();
        Survey survey = surveyService.findByStableId(task.getTargetStableId(), task.getTargetAssignedVersion()).get();
        validateResponse(survey, task, snapDto);
        // find or create the SurveyResponse object to attach the snapshot
        SurveyResponse response = createSnapshot(snapDto, task, enrollee, participantUserId);

        // now update the task status and response id
        task.setStatus(snapDto.isComplete() ? TaskStatus.COMPLETE : TaskStatus.IN_PROGRESS);
        task.setSurveyResponseId(response.getId());
        participantTaskService.update(task);

        EnrolleeSurveyEvent event = enrolleeEventService.publishEnrolleeSurveyEvent(enrollee, response, ppUser);
        logger.info("SurveyReponse received -- enrollee: {}, surveyStabledId: {}");
        HubResponse hubResponse = HubResponse.builder()
                .response(event.getSurveyResponse())
                .tasks(event.getEnrollee().getParticipantTasks().stream().toList())
                .enrollee(event.getEnrollee()).build();
        return hubResponse;
    }

    /**
     * creates a new snapshot (along with a SurveyResponse container if needed) for the given task
     * This method does not do any validation or authorization -- callers should ensure the user
     * is authorized to update the given task/enrollee, and that the task corresponds to the snapshot
     */
    @Transactional
    protected SurveyResponse createSnapshot(ResponseSnapshotDto snapDto, ParticipantTask task,
                                      Enrollee enrollee, UUID participantUserId) {
        UUID taskResponseId = task.getSurveyResponseId();
        Survey survey = surveyService.findByStableId(task.getTargetStableId(), task.getTargetAssignedVersion()).get();
        SurveyResponse existingResponse = null;
        if (taskResponseId != null) {
            existingResponse = dao.find(taskResponseId).get();
        } else {
            SurveyResponse newResponse = SurveyResponse.builder()
                    .enrolleeId(enrollee.getId())
                    .creatingParticipantUserId(participantUserId)
                    .surveyId(survey.getId())
                    .complete(snapDto.isComplete())
                    .build();
            existingResponse = dao.create(newResponse);
        }
        ResponseSnapshot snap = ResponseSnapshot.builder()
                .surveyResponseId(existingResponse.getId())
                .creatingParticipantUserId(participantUserId)
                .fullData(snapDto.getFullData())
                .resumeData(snapDto.getResumeData())
                .build();
        snap = responseSnapshotService.create(snap);
        dao.updateLastSnapshotId(existingResponse.getId(), snap.getId());
        existingResponse.setLastSnapshot(snap);
        existingResponse.setLastSnapshotId(snap.getId());
        return existingResponse;
    }

    @Override
    public void delete(UUID responseId, Set<CascadeProperty> cascades) {
        dao.clearLastSnapshotId(responseId);
        List<ResponseSnapshot> snapshots = responseSnapshotService.findByResponseId(responseId);
        for (ResponseSnapshot snap : snapshots) {
            responseSnapshotService.delete(snap.getId(), cascades);
        }
        dao.delete(responseId);
    }

    public void validateResponse(Survey survey, ParticipantTask task, ResponseSnapshotDto snapshotDto) {
        if (!survey.getStableId().equals(task.getTargetStableId())) {
            throw new IllegalArgumentException("submitted form does not match assigned task");
        }
        // TODO check required fields
    }
}
