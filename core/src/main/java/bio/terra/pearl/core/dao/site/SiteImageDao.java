package bio.terra.pearl.core.dao.site;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.site.SiteImage;
import java.util.Optional;
import java.util.UUID;
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

    public Optional<SiteImage> findOne(String shortcode) {
        return findByProperty("shortcode", shortcode);
    }

    public void deleteBySiteContentId(UUID siteContentId) {
        deleteByUuidProperty("site_content_id", siteContentId);
    }
}
