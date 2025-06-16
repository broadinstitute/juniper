package bio.terra.pearl.core.service.study;

import bio.terra.pearl.core.dao.study.StudyEnvironmentConfigDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.publishing.ConfigChange;
import bio.terra.pearl.core.model.publishing.StudyEnvironmentChange;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import bio.terra.pearl.core.service.CrudService;
import bio.terra.pearl.core.service.exception.internal.InternalServerException;
import bio.terra.pearl.core.service.migration.AuthMigrationConfigService;
import bio.terra.pearl.core.service.publishing.StudyEnvPublishable;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class StudyEnvironmentConfigService extends CrudService<StudyEnvironmentConfig, StudyEnvironmentConfigDao> implements StudyEnvPublishable {
    private final StudyEnvironmentService studyEnvironmentService;
    private final AuthMigrationConfigService authMigrationConfigService;

    public StudyEnvironmentConfigService(StudyEnvironmentConfigDao dao, @Lazy StudyEnvironmentService studyEnvironmentService, AuthMigrationConfigService authMigrationConfigService) {
        super(dao);
        this.studyEnvironmentService = studyEnvironmentService;
        this.authMigrationConfigService = authMigrationConfigService;
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

    @Transactional
    public void attachAuthMigrationConfig(StudyEnvironmentConfig studyEnvironmentConfig) {
        authMigrationConfigService
                .findByStudyEnvConfigId(studyEnvironmentConfig.getId())
                .ifPresent(studyEnvironmentConfig::setAuthMigrationConfig);
    }


    @Override
    public void loadForPublishing(StudyEnvironment studyEnv) {
        StudyEnvironmentConfig config = findByStudyEnvironmentId(studyEnv.getId());
        attachAuthMigrationConfig(config);
        studyEnv.setStudyEnvironmentConfig(config);
    }

    @Override
    public void updateDiff(StudyEnvironmentChange change, StudyEnvironment sourceEnv, StudyEnvironment destEnv) {
        List<ConfigChange> envConfigChanges = ConfigChange.allChanges(
                sourceEnv.getStudyEnvironmentConfig(),
                destEnv.getStudyEnvironmentConfig(),
                getPublishIgnoreProps());
        change.setConfigChanges(envConfigChanges);
    }


    @Override
    @Transactional
    public void applyDiff(StudyEnvironmentChange change, StudyEnvironment destEnv, PortalEnvironment destPortalEnv) {
        List<ConfigChange> configChanges = change.getConfigChanges();
        if (!configChanges.isEmpty()) {
            try {
                for (ConfigChange configChange : configChanges) {
                    PropertyUtils.setProperty(destEnv.getStudyEnvironmentConfig(), configChange.propertyName(), configChange.newValue());
                }
                update(destEnv.getStudyEnvironmentConfig());
            } catch (Exception e) {
                throw new InternalServerException("Error setting property during publish", e);
            }
        }

    }
}
