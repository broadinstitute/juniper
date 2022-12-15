package bio.terra.pearl.core.dao.site;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.site.SiteContent;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class SiteContentDao extends BaseJdbiDao<SiteContent> {
    public SiteContentDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<SiteContent> getClazz() {
        return SiteContent.class;
    }
}
