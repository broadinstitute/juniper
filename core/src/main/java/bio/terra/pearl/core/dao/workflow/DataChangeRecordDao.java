package bio.terra.pearl.core.dao.workflow;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.workflow.DataChangeRecord;
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
}
