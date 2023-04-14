package bio.terra.pearl.core.service.datarepo;

import bio.terra.pearl.core.dao.datarepo.CreateDatasetJobDao;
import bio.terra.pearl.core.model.datarepo.CreateDatasetJob;
import bio.terra.pearl.core.service.CrudService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CreateDatasetJobService extends CrudService<CreateDatasetJob, CreateDatasetJobDao> {

    public CreateDatasetJobService(CreateDatasetJobDao dao) {
        super(dao);
    }

    public void updateJobStatus(UUID id, String newJobStatus) {
        dao.updateJobStatus(id, newJobStatus);
    }

    public void deleteByStudyEnvironmentId(UUID studyEnvId) {
        dao.deleteByStudyEnvironmentId(studyEnvId);
    }
}
