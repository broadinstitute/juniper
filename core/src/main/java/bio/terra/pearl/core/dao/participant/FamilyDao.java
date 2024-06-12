package bio.terra.pearl.core.dao.participant;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.participant.Family;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.Optional;

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
}
