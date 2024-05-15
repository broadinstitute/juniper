package bio.terra.pearl.core.service.study;

import bio.terra.pearl.core.dao.study.StudyEnvironmentConfigDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import bio.terra.pearl.core.service.CrudService;
import java.util.UUID;

import bio.terra.pearl.core.service.exception.NotFoundException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudyEnvironmentConfigService extends CrudService<StudyEnvironmentConfig, StudyEnvironmentConfigDao> {
    private final StudyEnvironmentService studyEnvironmentService;

    public StudyEnvironmentConfigService(StudyEnvironmentConfigDao dao, @Lazy StudyEnvironmentService studyEnvironmentService) {
        super(dao);
        this.studyEnvironmentService = studyEnvironmentService;
    }

    /** assumes the shortcode has already been confirmed to be valid -- throws an error if the config/study isn't found */
    public StudyEnvironmentConfig findByStudyShortcode(String studyShortcode, EnvironmentName environmentName) {
        StudyEnvironment studyEnv = studyEnvironmentService.findByStudy(studyShortcode, environmentName).orElseThrow();
        return dao.find(studyEnv.getStudyEnvironmentConfigId()).orElseThrow(IllegalStateException::new);
    }

    public StudyEnvironmentConfig findByStudyEnvironmentId(UUID studyEnvironmentId) {
        StudyEnvironment studyEnv = studyEnvironmentService.find(studyEnvironmentId).orElseThrow();
        return dao.find(studyEnv.getStudyEnvironmentConfigId()).orElseThrow(IllegalStateException::new);
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
