package bio.terra.pearl.core.dao.participant;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.participant.Enrollee;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class EnrolleeDao extends BaseJdbiDao<Enrollee> {
    public EnrolleeDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<Enrollee> getClazz() {
        return Enrollee.class;
    }

    public Optional<Enrollee> findOneByShortcode(String shortcode) {
        return findByProperty("shortcode", shortcode);
    }

    public List<Enrollee> findByStudyEnvironment(UUID studyEnvironmentId) {
        return findAllByProperty("study_environment_id", studyEnvironmentId);
    }

    public List<Enrollee> findByParticipantUserId(UUID userId) {
        return findAllByProperty("participant_user_id", userId);
    }

    public int countByStudyEnvironment(UUID studyEnvironmentId) {
        return countByProperty("study_environment_id", studyEnvironmentId);
    }
    public void deleteByStudyEnvironmentId(UUID studyEnvironmentId) {
        deleteByUuidProperty("study_environment_id", studyEnvironmentId);
    }
}
