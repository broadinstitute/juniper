package bio.terra.pearl.core.service.datarepo;

import bio.terra.pearl.core.dao.datarepo.DatasetDao;
import bio.terra.pearl.core.model.datarepo.Dataset;
import bio.terra.pearl.core.service.CrudService;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class DatasetService extends CrudService<Dataset, DatasetDao> {

    public DatasetService(DatasetDao dao) {
        super(dao);
    }

    public void deleteByStudyEnvironmentId(UUID studyEnvId) {
        // TODO clean up the TDR dataset itself
        dao.deleteByStudyEnvironmentId(studyEnvId);
    }
}
