package bio.terra.pearl.core.dao.kit;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.kit.KitRequest;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class KitRequestDao extends BaseMutableJdbiDao<KitRequest> {
    public KitRequestDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<KitRequest> getClazz() { return KitRequest.class; }

    public List<KitRequest> findByEnrollee(UUID enrolleeId) {
        return super.findAllByProperty("enrollee_id", enrolleeId);
    }
}
