package bio.terra.pearl.core.dao.participant;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.participant.Family;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class FamilyDao extends BaseMutableJdbiDao<Family> {

    public FamilyDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<Family> getClazz() {
        return Family.class;
    }

    public Optional<Family> findOneByShortcode(String shortcode) {
        return findByProperty("shortcode", shortcode);
    }

    public List<Family> findByStudyEnvironmentId(UUID studyEnvironmentId) {
        return findAllByProperty("study_environment_id", studyEnvironmentId);
    }

    public void deleteByStudyEnvironmentId(UUID studyEnvironmentId) {
        deleteByProperty("study_environment_id", studyEnvironmentId);
    }

    public List<Family> findByEnrolleeId(UUID enrolleeId) {
        return jdbi.withHandle(handle -> handle.createQuery("SELECT family.* FROM family family INNER JOIN family_enrollee family_enrollee ON family_enrollee.family_id = family.id WHERE family_enrollee.enrollee_id = :enrolleeId")
                .bind("enrolleeId", enrolleeId)
                .mapToBean(Family.class)
                .list());
    }
}
