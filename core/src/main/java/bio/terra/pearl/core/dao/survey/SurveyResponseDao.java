package bio.terra.pearl.core.dao.survey;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
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

    public Optional<SurveyResponse> findOneWithLastSnapshot(UUID responseId) {
        return findWithChild(responseId, "lastSnapshotId",
                "lastSnapshot", responseSnapshotDao);
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
