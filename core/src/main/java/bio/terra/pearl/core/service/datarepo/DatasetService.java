package bio.terra.pearl.core.service.datarepo;

import bio.terra.datarepo.client.ApiException;
import bio.terra.datarepo.model.JobModel;
import bio.terra.pearl.core.dao.export.datarepo.DatasetDao;
import bio.terra.pearl.core.model.export.datarepo.Dataset;
import bio.terra.pearl.core.model.export.datarepo.DatasetStatus;
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

    //This deletes the dataset from the Juniper database and from TDR.
    //Note that this is only used when deleting datasets as part of study environment re-population.
    //For ease of use, this does not poll for the result of the deletion job.
    //It's okay if there are stray datasets in TDR dev, we will periodically clean them.
    //For production code, it's better to call DataRepoExportService.deleteDataset, which is
    //equipped to handle the asynchronous nature of TDR deletions.
    public void deleteFull(Dataset dataset) {
        dao.delete(dataset.getId());
        try {
            JobModel jobModel = dataRepoClient.deleteDataset(dataset.getTdrDatasetId());
            logger.info("Deleted dataset: {} (TDR job ID {})", dataset.getId(), jobModel.getId());
        } catch (ApiException e) {
            logger.error("Unable to delete dataset {}. Error: {}", dataset.getId(), e.getMessage());
        }
    }

    public void delete(Dataset dataset) { dao.delete(dataset.getId()); }

    public Optional<Dataset> findById(UUID datasetId) {
        return dao.find(datasetId);
    }

    public void updateLastExported(UUID id, Instant lastExported) {
        dao.updateLastExported(id, lastExported);
    }

    public void setTdrDatasetId(UUID datasetId, UUID tdrDatasetId) {
        dao.setTdrDatasetId(datasetId, tdrDatasetId);
    }

    public void updateStatus(UUID id, DatasetStatus newStatus) {
        dao.updateStatus(id, newStatus);
    }

    public void deleteByStudyEnvironmentId(UUID studyEnvId) {
        List<Dataset> datasets = dao.findByStudyEnvironmentId(studyEnvId);
        for (Dataset dataset : datasets) {
            deleteFull(dataset);
        }
    }

}
