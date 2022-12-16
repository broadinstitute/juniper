package bio.terra.pearl.core.dao.study;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class StudyDao  extends BaseJdbiDao<Study> {
    private StudyEnvironmentDao studyEnvironmentDao;
    private StudyEnvironmentConfigDao studyEnvironmentConfigDao;

    @Override
    protected Class<Study> getClazz() {
        return Study.class;
    }

    public StudyDao(Jdbi jdbi, StudyEnvironmentDao studyEnvironmentDao,
                    StudyEnvironmentConfigDao studyEnvironmentConfigDao) {
        super(jdbi);
        this.studyEnvironmentDao = studyEnvironmentDao;
        this.studyEnvironmentConfigDao = studyEnvironmentConfigDao;
    }

    public Optional<Study> findOneByShortcode(String shortcode) {
        return findByProperty("shortcode", shortcode);
    }

    public Optional<Study> findOneFullLoad(UUID id) {
        Optional<Study> studyOpt = find(id);
        studyOpt.ifPresent(study -> {
            List<StudyEnvironment> studyEnvs = studyEnvironmentDao.findByStudy(id);
            List<UUID> configIds = studyEnvs.stream().map(env -> env.getStudyEnvironmentConfigId())
                    .collect(Collectors.toList());
            List<StudyEnvironmentConfig> configs = studyEnvironmentConfigDao.findAll(configIds);
            for (StudyEnvironment studyEnv : studyEnvs) {
                study.getStudyEnvironments().add(studyEnv);
                studyEnv.setStudyEnvironmentConfig(configs.stream()
                        .filter(config -> config.getId().equals(studyEnv.getStudyEnvironmentConfigId()))
                        .findFirst().get());
            }
        });
        return studyOpt;
    }
}
