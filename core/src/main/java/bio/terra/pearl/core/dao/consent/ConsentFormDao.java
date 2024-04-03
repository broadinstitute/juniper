package bio.terra.pearl.core.dao.consent;

import bio.terra.pearl.core.dao.BaseVersionedJdbiDao;
import bio.terra.pearl.core.model.consent.ConsentForm;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ConsentFormDao extends BaseVersionedJdbiDao<ConsentForm> {
    public ConsentFormDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<ConsentForm> getClazz() {
        return ConsentForm.class;
    }

    public List<ConsentForm> findByPortalId(UUID portalId) {
        return findAllByProperty("portal_id", portalId);
    }

    public List<ConsentForm> findByStableIdNoContent(String stableId, UUID portalId) {
        List<ConsentForm> forms = jdbi.withHandle(handle ->
                handle.createQuery("select id, name, created_at, last_updated_at, version, stable_id, portal_id from consent_form where stable_id = :stableId and portal_id = :portalId;")
                        .bind("stableId", stableId)
                        .bind("portalId", portalId)
                        .mapTo(clazz)
                        .list()
        );
        return forms;
    }

    public int getNextFormVersion(String stableId, UUID portalId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("select max(version) from " + tableName + " where stable_id = :stableId and portal_id = :portalId;")
                        .bind("stableId", stableId)
                        .bind("portalId", portalId)
                        .mapTo(int.class)
                        .one()
        ) + 1;
    }
}
