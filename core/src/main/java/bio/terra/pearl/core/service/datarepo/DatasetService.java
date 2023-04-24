package bio.terra.pearl.core.service.datarepo;

import bio.terra.datarepo.client.ApiException;
import bio.terra.datarepo.model.JobModel;
import bio.terra.pearl.core.dao.datarepo.DatasetDao;
import bio.terra.pearl.core.model.datarepo.Dataset;
import bio.terra.pearl.core.service.CrudService;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class DatasetService extends CrudService<Dataset, DatasetDao> {

    private DataRepoClient dataRepoClient;

    public DatasetService(DatasetDao dao, DataRepoClient dataRepoClient) {
        super(dao);
        this.dataRepoClient = dataRepoClient;
    }

    public void delete(Dataset dataset) {
        dao.delete(dataset.getId());
        try {
            JobModel jobModel = dataRepoClient.deleteDataset(dataset.getDatasetId());
            logger.info("Deleted dataset: {} (TDR job ID {})", dataset.getDatasetId(), jobModel.getId());
        } catch (ApiException e) {
            logger.error("Unable to delete dataset {}. Error: {}", dataset.getDatasetId(), e.getMessage());
        }
    }

    public void updateLastExported(UUID id, Instant lastExported) {
        dao.updateLastExported(id, lastExported);
    }

    public Optional<Dataset> findByDataRepoId(UUID dataRepoId) {
        return dao.findByDataRepoId(dataRepoId);
    }

    public void deleteByStudyEnvironmentId(UUID studyEnvId) {
        List<Dataset> datasets = dao.findByStudyEnvironmentId(studyEnvId);
        for (Dataset dataset : datasets) {
            delete(dataset);
        }
    }

}
