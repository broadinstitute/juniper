package bio.terra.pearl.core.dao.site;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.site.HtmlPage;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class HtmlPageDao extends BaseJdbiDao<HtmlPage> {
    public HtmlPageDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<HtmlPage> getClazz() {
        return HtmlPage.class;
    }

    public List<HtmlPage> findByLocalSite(UUID localSiteId) {
        return findAllByProperty("localized_site_content_id", localSiteId);
    }
}
