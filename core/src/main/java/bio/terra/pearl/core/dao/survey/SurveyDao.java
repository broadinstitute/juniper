package bio.terra.pearl.core.dao.survey;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.survey.Survey;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class SurveyDao extends BaseJdbiDao<Survey> {
    public SurveyDao(Jdbi jdbi) {
        super(jdbi);
    }

    public Optional<Survey> findByStableId(String stableId, int version) {
        return jdbi.withHandle(handle ->
                handle.createQuery("select * from " + tableName
                                + " where stable_id = :stableId and version = :version")
                        .bind("stableId", stableId)
                        .bind("version", version)
                        .mapTo(clazz)
                        .findOne()
        );
    }

    public List<Survey> findByPortalId(UUID portalId) {
        return findAllByProperty("portal_id", portalId);
    }


    @Override
    protected Class<Survey> getClazz() {
        return Survey.class;
    }
}
