package bio.terra.pearl.core.service.site;

import bio.terra.pearl.core.dao.site.NavbarItemDao;
import bio.terra.pearl.core.model.site.NavbarItem;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.ImmutableEntityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class NavbarItemService extends ImmutableEntityService<NavbarItem, NavbarItemDao> {
    public NavbarItemService(NavbarItemDao dao) {
        super(dao);
    }

    @Transactional
    @Override
    public NavbarItem create(NavbarItem item) {
        List<NavbarItem> groupedItems = item.getItems();
        if (groupedItems != null && !groupedItems.isEmpty()) {
            for (int i = 0; i < groupedItems.size(); i++) {
                NavbarItem groupedItem = groupedItems.get(i);
                groupedItem.setParentNavbarItemId(item.getId());
                groupedItem.setItemOrder(i);
                groupedItem = this.create(groupedItem);
                groupedItems.set(i, groupedItem);
            }
        }
        item = dao.create(item);
        return item;
    }

    public void deleteByLocalSiteId(UUID localSiteId, Set<CascadeProperty> cascades) {
        dao.deleteByLocalSiteId(localSiteId);
    }
}
