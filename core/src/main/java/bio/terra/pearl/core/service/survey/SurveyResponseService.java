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
import bio.terra.pearl.core.service.ImmutableEntityService;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.ParticipantTaskService;
import bio.terra.pearl.core.service.study.StudyEnvironmentSurveyService;
import bio.terra.pearl.core.service.workflow.EventService;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SurveyResponseService extends ImmutableEntityService<SurveyResponse, SurveyResponseDao> {
    private ResponseSnapshotService responseSnapshotService;
    private EnrolleeService enrolleeService;
    private SurveyService surveyService;
    private ParticipantTaskService participantTaskService;
    private StudyEnvironmentSurveyService studyEnvironmentSurveyService;
    private SnapshotProcessingService snapshotProcessingService;
    private EventService eventService;

    public SurveyResponseService(SurveyResponseDao dao, ResponseSnapshotService responseSnapshotService,
                                 EnrolleeService enrolleeService, SurveyService surveyService,
                                 ParticipantTaskService participantTaskService,
                                 StudyEnvironmentSurveyService studyEnvironmentSurveyService,
                                 SnapshotProcessingService snapshotProcessingService, EventService eventService) {
        super(dao);
        this.responseSnapshotService = responseSnapshotService;
        this.enrolleeService = enrolleeService;
        this.surveyService = surveyService;
        this.participantTaskService = participantTaskService;
        this.studyEnvironmentSurveyService = studyEnvironmentSurveyService;
        this.snapshotProcessingService = snapshotProcessingService;
        this.eventService = eventService;
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
     */
    public SurveyWithResponse findWithActiveResponse(UUID studyEnvId, String stableId, Integer version,
                                                     Enrollee enrollee, UUID taskId) {
        Survey form = surveyService.findByStableId(stableId, version).get();
        SurveyResponse lastResponse = null;
        if (taskId != null) {
            ParticipantTask task = participantTaskService.find(taskId).get();
            // if there is an associated task, try to find an associated response
            lastResponse = dao.findOneWithLastSnapshot(task.getSurveyResponseId()).orElse(null);
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
     * Creates a survey response and fires appropriate downstream events. 
     */
    @Transactional
    public HubResponse<ConsentResponse> submitResponse(UUID participantUserId, PortalParticipantUser ppUser,
                                                       Enrollee enrollee, UUID taskId,
                                                       ResponseSnapshotDto snapDto) {

        ParticipantTask task = participantTaskService.authTaskToPortalParticipantUser(taskId, ppUser.getId()).get();
        Survey survey = surveyService.findByStableIdWithMappings(task.getTargetStableId(),
                task.getTargetAssignedVersion()).get();
        validateResponse(survey, task, snapDto);

        // find or create the SurveyResponse object to attach the snapshot
        SurveyResponse response = findOrCreateResponse(task, enrollee, participantUserId);
        attachSnapshot(response, snapDto, participantUserId);

        // process any answers that need to be propagated elsewhere to the data model
        snapshotProcessingService.processAllAnswerMappings(snapDto.getParsedData(),
                survey.getAnswerMappings(), ppUser, participantUserId, enrollee.getId(), survey.getId());

        // now update the task status and response id
        task.setStatus(snapDto.isComplete() ? TaskStatus.COMPLETE : TaskStatus.IN_PROGRESS);
        task.setSurveyResponseId(response.getId());
        participantTaskService.update(task);

        EnrolleeSurveyEvent event = eventService.publishEnrolleeSurveyEvent(enrollee, response, ppUser);
        logger.info("SurveyReponse received -- enrollee: {}, surveyStabledId: {}", enrollee.getShortcode(), survey.getStableId());
        HubResponse hubResponse = eventService.buildHubResponse(event, response);
        return hubResponse;
    }

    /**
     * creates a new snapshot (along with a SurveyResponse container if needed) for the given task
     * This method does not do any validation or authorization -- callers should ensure the user
     * is authorized to update the given task/enrollee, and that the task corresponds to the snapshot
     */
    @Transactional
    protected SurveyResponse findOrCreateResponse(ParticipantTask task, Enrollee enrollee, UUID participantUserId) {
        UUID taskResponseId = task.getSurveyResponseId();
        Survey survey = surveyService.findByStableId(task.getTargetStableId(), task.getTargetAssignedVersion()).get();
        SurveyResponse response = null;
        if (taskResponseId != null) {
            response = dao.find(taskResponseId).get();
        } else {
            SurveyResponse newResponse = SurveyResponse.builder()
                    .enrolleeId(enrollee.getId())
                    .creatingParticipantUserId(participantUserId)
                    .surveyId(survey.getId())
                    .complete(false)
                    .build();
            response = dao.create(newResponse);
        }
        return response;
    }

    /** Creates and attaches the snapshot to the response.  Updates the Response with the snapshot and completion */
    @Transactional
    protected ResponseSnapshot attachSnapshot(SurveyResponse response, ResponseSnapshotDto snapDto, UUID participantUserId) {
        ResponseSnapshot snap = ResponseSnapshot.builder()
                .surveyResponseId(response.getId())
                .creatingParticipantUserId(participantUserId)
                .fullData(snapDto.getFullData())
                .resumeData(snapDto.getResumeData())
                .build();
        snap = responseSnapshotService.create(snap);
        dao.updateLastSnapshotId(response.getId(), snap.getId());
        response.setLastSnapshot(snap);
        response.setLastSnapshotId(snap.getId());
        response.setComplete(snapDto.isComplete());
        dao.update(response);
        return snap;
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
