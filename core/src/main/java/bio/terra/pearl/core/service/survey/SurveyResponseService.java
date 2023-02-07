package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.dao.survey.SurveyResponseDao;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.survey.*;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.CrudService;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.ParticipantTaskService;
import bio.terra.pearl.core.service.study.StudyEnvironmentSurveyService;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class SurveyResponseService extends CrudService<SurveyResponse, SurveyResponseDao> {
    private ResponseSnapshotService responseSnapshotService;
    private EnrolleeService enrolleeService;
    private SurveyService surveyService;
    private ParticipantTaskService participantTaskService;
    private StudyEnvironmentSurveyService studyEnvironmentSurveyService;

    public SurveyResponseService(SurveyResponseDao dao, ResponseSnapshotService responseSnapshotService,
                                 EnrolleeService enrolleeService, SurveyService surveyService,
                                 ParticipantTaskService participantTaskService,
                                 StudyEnvironmentSurveyService studyEnvironmentSurveyService) {
        super(dao);
        this.responseSnapshotService = responseSnapshotService;
        this.enrolleeService = enrolleeService;
        this.surveyService = surveyService;
        this.participantTaskService = participantTaskService;
        this.studyEnvironmentSurveyService = studyEnvironmentSurveyService;
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

        StudyEnvironmentSurvey configSurvey = studyEnvironmentSurveyService
                .findBySurvey(studyEnvId, form.getId()).get();
        configSurvey.setSurvey(form);
        return new SurveyWithResponse(
                configSurvey, lastResponse
        );
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

}
