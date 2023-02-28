package bio.terra.pearl.core.dao.workflow;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.workflow.DataChangeRecord;
import java.util.List;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class DataChangeRecordDao extends BaseJdbiDao<DataChangeRecord> {

    public DataChangeRecordDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<DataChangeRecord> getClazz() {
        return DataChangeRecord.class;
    }

    public List<DataChangeRecord> findByEnrolleeId(UUID enrolleeId) {
        return findAllByProperty("enrollee_id", enrolleeId);
    }
}
