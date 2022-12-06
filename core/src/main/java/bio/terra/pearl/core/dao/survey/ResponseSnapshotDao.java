package bio.terra.pearl.core.dao.survey;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.survey.ResponseSnapshot;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class ResponseSnapshotDao extends BaseJdbiDao<ResponseSnapshot> {
    public ResponseSnapshotDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<ResponseSnapshot> getClazz() {
        return ResponseSnapshot.class;
    }
}
