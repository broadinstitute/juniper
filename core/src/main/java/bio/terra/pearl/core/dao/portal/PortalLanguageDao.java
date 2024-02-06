package bio.terra.pearl.core.dao.portal;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.portal.PortalLanguage;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class PortalLanguageDao extends BaseJdbiDao<PortalLanguage> {

    @Override
    public Class<PortalLanguage> getClazz() {
        return PortalLanguage.class;
    }

    public PortalLanguageDao(Jdbi jdbi) {
        super(jdbi);
    }
}
