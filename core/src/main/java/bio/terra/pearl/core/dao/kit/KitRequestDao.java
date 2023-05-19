package bio.terra.pearl.core.dao.kit;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.kit.KitRequest;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class KitRequestDao extends BaseMutableJdbiDao<KitRequest> {
    public KitRequestDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<KitRequest> getClazz() { return KitRequest.class; }
}
