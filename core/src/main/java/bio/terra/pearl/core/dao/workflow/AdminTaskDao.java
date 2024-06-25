package bio.terra.pearl.core.dao.workflow;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.workflow.AdminTask;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class AdminTaskDao extends BaseMutableJdbiDao<AdminTask> {
    public AdminTaskDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<AdminTask> getClazz() {
        return AdminTask.class;
    }

    public List<AdminTask> findByEnrolleeId(UUID enrolleeId) {
        return findAllByProperty("enrollee_id", enrolleeId);
    }

    public void deleteByEnrolleeId(UUID enrolleeId) {
        deleteByProperty("enrollee_id", enrolleeId);
    }

    public List<AdminTask> findByStudyEnvironmentId(UUID studyEnvId) {
        return findAllByProperty("study_environment_id", studyEnvId);
    }

    // WARNING: This method is not audited; it should only be used during study population/repopulation
    public void deleteByStudyEnvironmentId(UUID studyEnvId) {
        deleteByProperty("study_environment_id", studyEnvId);
    }
}
