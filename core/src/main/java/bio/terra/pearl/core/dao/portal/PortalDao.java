package bio.terra.pearl.core.dao.portal;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.portal.Portal;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PortalDao extends BaseJdbiDao<Portal> {
    @Override
    protected Class<Portal> getClazz() {
        return Portal.class;
    }

    public PortalDao(Jdbi jdbi) {
        super(jdbi);
    }

    public Optional<Portal> findOneByShortcode(String shortcode) {
        return findByProperty("shortcode", shortcode);
    }

}
