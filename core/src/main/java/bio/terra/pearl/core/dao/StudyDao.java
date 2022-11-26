package bio.terra.pearl.core.dao;

import bio.terra.pearl.core.model.Study;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class StudyDao  extends BaseJdbiDao<Study> {
    @Override
    protected Class<Study> getClazz() {
        return Study.class;
    }

    public StudyDao(Jdbi jdbi) {
        super(jdbi);
    }

    public Optional<Study> findOneByShortcode(String shortcode) {
        return findByProperty("shortcode", shortcode);
    }
}
