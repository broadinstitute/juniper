package bio.terra.pearl.core.service.site;

import bio.terra.pearl.core.dao.site.NavbarItemDao;
import bio.terra.pearl.core.model.site.NavbarItem;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.ImmutableEntityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
        NavbarItem savedItem = dao.create(item);

        List<NavbarItem> savedItems = new ArrayList<>();
        if (item.getItems() != null && !item.getItems().isEmpty()) {
            for (int i = 0; i < item.getItems().size(); i++) {
                NavbarItem groupedItem = item.getItems().get(i);
                groupedItem.setParentNavbarItemId(savedItem.getId());
                groupedItem.setLocalizedSiteContentId(savedItem.getLocalizedSiteContentId());
                groupedItem.setItemOrder(i);
                groupedItem = this.create(groupedItem);
                savedItems.add(groupedItem);
            }
        }
        savedItem.setItems(savedItems);
        return savedItem;
    }

    public void deleteByLocalSiteId(UUID localSiteId, Set<CascadeProperty> cascades) {
        dao.deleteByLocalSiteId(localSiteId);
    }
}
