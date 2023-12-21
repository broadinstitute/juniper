package bio.terra.pearl.core.dao.consent;

import bio.terra.pearl.core.dao.BaseVersionedJdbiDao;
import bio.terra.pearl.core.model.consent.ConsentForm;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import bio.terra.pearl.core.model.survey.Survey;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class ConsentFormDao extends BaseVersionedJdbiDao<ConsentForm> {
    public ConsentFormDao(Jdbi jdbi) {
        super(jdbi);
    }

    public Optional<ConsentForm> findByStableId(String stableId, int version) {
        return findByTwoProperties("stable_id", stableId, "version", version);
    }

    @Override
    protected Class<ConsentForm> getClazz() {
        return ConsentForm.class;
    }

    public List<ConsentForm> findByPortalId(UUID portalId) {
        return findAllByProperty("portal_id", portalId);
    }

    public List<ConsentForm> findByStableIdNoContent(String stableId) {
        List<ConsentForm> forms = jdbi.withHandle(handle ->
                handle.createQuery("select id, name, created_at, last_updated_at, version, stable_id, portal_id from consent_form where stable_id = :stableId;")
                        .bind("stableId", stableId)
                        .mapTo(clazz)
                        .list()
        );
        return forms;
    }

    public int getNextVersion(String stableId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("select max(version) from " + tableName + " where stable_id = :stableId")
                        .bind("stableId", stableId)
                        .mapTo(int.class)
                        .one()
        ) + 1;
    }
}
