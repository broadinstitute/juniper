package bio.terra.pearl.core.service.datarepo;

import bio.terra.pearl.core.dao.export.datarepo.DataRepoJobDao;
import bio.terra.pearl.core.model.export.datarepo.DataRepoJob;
import bio.terra.pearl.core.service.CrudService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DataRepoJobService extends CrudService<DataRepoJob, DataRepoJobDao> {

    public DataRepoJobService(DataRepoJobDao dao) {
        super(dao);
    }

    public void updateJobStatus(UUID id, String newJobStatus) {
        dao.updateJobStatus(id, newJobStatus);
    }

    public void deleteByStudyEnvironmentId(UUID studyEnvId) {
        dao.deleteByStudyEnvironmentId(studyEnvId);
    }

    public void deleteByDatasetId(UUID datasetId) { dao.deleteByDatasetId(datasetId); }
}
