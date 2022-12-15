package bio.terra.pearl.core.dao.site;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.site.LocalizedSiteContent;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
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

    public Optional<LocalizedSiteContent> findBySiteContent(UUID siteContentId, String language) {
        return findByTwoProperties("site_content_id", siteContentId, "language", language);
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

    public void setLandingPageId(UUID siteContentId, UUID landingPageId) {
        jdbi.withHandle(handle ->
                handle.createUpdate("update " + tableName + " set landing_page_id = :landingPageId "
                                + " where id = :id;")
                        .bind("id", siteContentId)
                        .bind("landingPageId", landingPageId)
                        .execute()
        );
    }
}
