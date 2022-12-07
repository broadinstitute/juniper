package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.dao.survey.SurveyResponseDao;
import bio.terra.pearl.core.model.survey.ResponseSnapshot;
import bio.terra.pearl.core.model.survey.SurveyResponse;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.CrudService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class SurveyResponseService extends CrudService<SurveyResponse, SurveyResponseDao> {
    private ResponseSnapshotService responseSnapshotService;

    public SurveyResponseService(SurveyResponseDao dao, ResponseSnapshotService responseSnapshotService) {
        super(dao);
        this.responseSnapshotService = responseSnapshotService;
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
            if (snapshot.getAdminUserId() == null && snapshot.getParticipantUserId() == null) {
                snapshot.setParticipantUserId(response.getParticipantUserId());
                snapshot.setAdminUserId(response.getAdminUserId());
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
