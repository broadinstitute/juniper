package bio.terra.pearl.core.dao.site;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.site.NavbarItem;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class NavbarItemDao extends BaseJdbiDao<NavbarItem> {
    public NavbarItemDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<NavbarItem> getClazz() {
        return NavbarItem.class;
    }

    public List<NavbarItem> findByLocalSiteId(UUID localSiteId) {
        return findAllByPropertySorted("localized_site_content_id", localSiteId,
                "item_order", "asc");
    }

    public void deleteByLocalSiteId(UUID localSiteId) {
        deleteByUuidProperty("localized_site_content_id", localSiteId);
    }
}
