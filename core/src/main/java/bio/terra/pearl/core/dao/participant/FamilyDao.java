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

    public List<Family> findByEnrolleeId(UUID enrolleeId) {
        return jdbi.withHandle(handle -> handle.createQuery("SELECT f.* FROM family f INNER JOIN family_members fm ON fm.family_id = f.id WHERE fm.enrollee_id = :enrolleeId")
                .bind("enrolleeId", enrolleeId)
                .mapToBean(Family.class)
                .list());
    }
}
