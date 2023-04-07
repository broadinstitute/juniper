package bio.terra.pearl.core.dao.site;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.site.SiteImage;
import java.util.Optional;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class SiteImageDao extends BaseJdbiDao<SiteImage> {
    public SiteImageDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<SiteImage> getClazz() {
        return SiteImage.class;
    }

    public Optional<SiteImage> findOne(String portalShortcode, String cleanFileName, int version) {
        return jdbi.withHandle(handle ->
                handle.createQuery("select * from " + tableName + " where portal_shortcode = :portalShortcode"
                                + " and clean_file_name = :cleanFileName and version = :version;")
                        .bind("portalShortcode", portalShortcode)
                        .bind("cleanFileName", cleanFileName)
                        .bind("version", version)
                        .mapTo(clazz)
                        .findOne()
        );
    }

    public Optional<SiteImage> findOneLatestVersion(String portalShortcode, String cleanFileName) {
        return jdbi.withHandle(handle ->
                handle.createQuery("select * from " + tableName + " where portal_shortcode = :portalShortcode"
                                + " and clean_file_name = :cleanFileName order by version desc limit 1;")
                        .bind("portalShortcode", portalShortcode)
                        .bind("cleanFileName", cleanFileName)
                        .mapTo(clazz)
                        .findOne()
        );
    }

    public void deleteByPortalShortcode(String portalShortcode) {
        deleteByProperty("portal_shortcode", portalShortcode);
    }
}
