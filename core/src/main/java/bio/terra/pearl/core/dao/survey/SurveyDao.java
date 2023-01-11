package bio.terra.pearl.core.dao.survey;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.survey.Survey;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class SurveyDao extends BaseJdbiDao<Survey> {
    public SurveyDao(Jdbi jdbi) {
        super(jdbi);
    }

    public Optional<Survey> findByStableId(String stableId, int version) {
        return findByTwoProperties("stable_id", stableId, "version", version);
    }

    public List<Survey> findByPortalId(UUID portalId) {
        return findAllByProperty("portal_id", portalId);
    }

    public List<Survey> findAllById(Collection<UUID> ids) {
        return findAllByPropertyCollection("id", ids);
    }

    public int getNextVersion(String stableId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("select max(version) from " + tableName + " where stable_id = :stableId")
                        .bind("stableId", stableId)
                        .mapTo(int.class)
                        .one()
        ) + 1;
    }

    @Override
    protected Class<Survey> getClazz() {
        return Survey.class;
    }
}
