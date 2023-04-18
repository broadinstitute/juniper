package bio.terra.pearl.core.service.datarepo;

import bio.terra.datarepo.client.ApiException;
import bio.terra.pearl.core.dao.datarepo.DatasetDao;
import bio.terra.pearl.core.model.datarepo.Dataset;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.CrudService;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import bio.terra.pearl.core.service.exception.datarepo.DatasetDeletionException;
import org.springframework.stereotype.Service;

@Service
public class DatasetService extends CrudService<Dataset, DatasetDao> {

    private DataRepoClient dataRepoClient;

    public DatasetService(DatasetDao dao, DataRepoClient dataRepoClient) {
        super(dao);
        this.dataRepoClient = dataRepoClient;
    }

    @Override
    public void delete(UUID id, Set<CascadeProperty> cascades) {
        dao.delete(id);
        try {
            dataRepoClient.deleteDataset(id);
        } catch (ApiException e) {
            throw new DatasetDeletionException(e.getMessage());
        }
    }

    public void deleteByStudyEnvironmentId(UUID studyEnvId) {
        List<Dataset> datasets = dao.findByStudyEnvironmentId(studyEnvId);
        for (Dataset dataset : datasets) {
            delete(dataset.getId(), CascadeProperty.EMPTY_SET);
        }
    }

    public List<UUID> findOrphanedDatasets() {

    }
}
