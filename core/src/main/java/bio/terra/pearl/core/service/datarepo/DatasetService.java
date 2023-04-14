package bio.terra.pearl.core.service.datarepo;

import bio.terra.pearl.core.dao.datarepo.DatasetDao;
import bio.terra.pearl.core.model.datarepo.Dataset;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.CrudService;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class DatasetService extends CrudService<Dataset, DatasetDao> {

    public DatasetService(DatasetDao dao) {
        super(dao);
    }

    @Override
    public void delete(UUID id, Set<CascadeProperty> cascades) {
        // TODO clean up the TDR dataset itself
        dao.delete(id);
    }

    public void deleteByStudyEnvironmentId(UUID studyEnvId) {
        List<Dataset> datasets = dao.findByStudyEnvironmentId(studyEnvId);
        for (Dataset dataset : datasets) {
            delete(dataset.getId(), CascadeProperty.EMPTY_SET);
        }
    }
}
