package bio.terra.pearl.core.service.study;

import bio.terra.pearl.core.dao.study.StudyEnvironmentConfigDao;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import bio.terra.pearl.core.service.CrudService;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudyEnvironmentConfigService extends CrudService<StudyEnvironmentConfig, StudyEnvironmentConfigDao> {
    public StudyEnvironmentConfigService(StudyEnvironmentConfigDao dao) {
        super(dao);
    }

    @Transactional
    public void deleteByStudyEnvironmentId(UUID studyEnvId) {
        dao.deleteByStudyEnvironmentId(studyEnvId);
    }

    @Transactional
    public void delete(UUID configId) {
        dao.delete(configId);
    }
}
