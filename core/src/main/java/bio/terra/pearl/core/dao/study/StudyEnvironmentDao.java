package bio.terra.pearl.core.dao.study;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import org.apache.commons.lang3.StringUtils;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class StudyEnvironmentDao extends BaseJdbiDao<StudyEnvironment> {
    public StudyEnvironmentDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    public Class<StudyEnvironment> getClazz() {
        return StudyEnvironment.class;
    }

    public List<StudyEnvironment> findByStudy(UUID studyId) {
        return findAllByProperty("study_id", studyId);
    }

    public Optional<StudyEnvironment> findByStudy(String shortcode, EnvironmentName environmentName) {
        List<String> primaryCols = getQueryColumns.stream().map(col -> "a." + col)
                .collect(Collectors.toList());
        return jdbi.withHandle(handle ->
                handle.createQuery("select " + StringUtils.join(primaryCols, ", ") + " from " + tableName
                        + " a join study on study_id = study.id"
                        + " where study.shortcode = :shortcode and environment_name = :environmentName")
                        .bind("shortcode", shortcode)
                        .bind("environmentName", environmentName)
                        .mapTo(clazz)
                        .findOne()
        );
    }

    public void deleteByStudyId(UUID studyId) {
        deleteByUuidProperty("study_id", studyId);
    }
}
