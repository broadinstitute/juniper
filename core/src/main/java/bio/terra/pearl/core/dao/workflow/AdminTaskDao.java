package bio.terra.pearl.core.dao.workflow;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.workflow.AdminTask;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.model.workflow.TaskType;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

    public List<AdminTask> findByAssignee(UUID adminUserId) {
        return findAllByProperty("assigned_admin_user_id", adminUserId);
    }

    public List<AdminTask> findByStudyEnvironmentId(UUID studyEnvId) {
        return findAllByProperty("study_environment_id", studyEnvId);
    }

    public void deleteByStudyEnvironmentId(UUID studyEnvId) {
        deleteByProperty("study_environment_id", studyEnvId);
    }
}
