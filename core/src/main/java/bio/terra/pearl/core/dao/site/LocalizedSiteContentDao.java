package bio.terra.pearl.core.dao.site;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.site.LocalizedSiteContent;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class LocalizedSiteContentDao  extends BaseJdbiDao<LocalizedSiteContent> {
    public LocalizedSiteContentDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<LocalizedSiteContent> getClazz() {
        return LocalizedSiteContent.class;
    }

    public List<LocalizedSiteContent> findBySiteContent(UUID siteContentId) {
        return findAllByProperty("site_content_id", siteContentId);
    }

    /**
     * clears the landing page Id from the specified site content.  this is necessary in some cases to enable
     * deletion, since localizedSiteContent can be bidirectionally linked to an htmlPage
     * @param siteContentId
     */
    public void clearLandingPageId(UUID siteContentId) {
        jdbi.withHandle(handle ->
                handle.createUpdate("update " + tableName + " set landing_page_id = null "
                                + " where id = :id;")
                        .bind("id", siteContentId)
                        .execute()
        );
    }
}
