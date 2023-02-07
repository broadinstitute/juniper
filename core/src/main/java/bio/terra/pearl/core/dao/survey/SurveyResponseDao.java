package bio.terra.pearl.core.dao.survey;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.survey.ResponseSnapshot;
import bio.terra.pearl.core.model.survey.SurveyResponse;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class SurveyResponseDao extends BaseMutableJdbiDao<SurveyResponse> {
    private ResponseSnapshotDao responseSnapshotDao;

    public SurveyResponseDao(Jdbi jdbi, ResponseSnapshotDao responseSnapshotDao) {
        super(jdbi);
        this.responseSnapshotDao = responseSnapshotDao;
    }


    @Override
    protected Class<SurveyResponse> getClazz() {
        return SurveyResponse.class;
    }

    public List<SurveyResponse> findByEnrolleeId(UUID enrolleeId) {
        return findAllByProperty("enrollee_id", enrolleeId);
    }

    public List<SurveyResponse> findByEnrolleeIdWithLastSnapshot(UUID enrolleeId) {
        List<SurveyResponse> responses = findAllByProperty("enrollee_id", enrolleeId);
        List<UUID> snapshotIds = responses.stream().map(response -> response.getLastSnapshotId()).toList();
        List<ResponseSnapshot> snapshots = responseSnapshotDao.findAll(snapshotIds);
        for (SurveyResponse response : responses) {
            response.setLastSnapshot(snapshots.stream()
                    .filter(snap -> snap.getId().equals(response.getLastSnapshotId())).findFirst().orElse(null));
        }
        return responses;
    }

    public Optional<SurveyResponse> findOneWithLastSnapshot(UUID responseId) {
        return findWithChild(responseId, "lastSnapshotId",
                "lastSnapshot", responseSnapshotDao);
    }

    public Optional<SurveyResponse> findMostRecent(UUID enrolleeId, UUID surveyId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("select * from " + tableName + " where enrollee_id = :enrolleeId"
                                + " and survey_id = :surveyId order by created_at DESC LIMIT 1")
                        .bind("enrolleeId", enrolleeId)
                        .bind("surveyId", surveyId)
                        .mapTo(clazz)
                        .findOne()
        );
    }

    /**
     * clears the lastSnapshotId.  this is necessary in some cases to enable
     * deletion, since that snapshot is bidirectionally linked to the response
     */
    public void clearLastSnapshotId(UUID responseId) {
        jdbi.withHandle(handle ->
                handle.createUpdate("update " + tableName + " set last_snapshot_id = null "
                                + " where id = :id;")
                        .bind("id", responseId)
                        .execute()
        );
    }
}
