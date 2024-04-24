package bio.terra.pearl.core.dao.dataimport;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.dataimport.ImportItem;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ImportItemDao extends BaseMutableJdbiDao<ImportItem> {

    public ImportItemDao(Jdbi jdbi) {
        super(jdbi);
        jdbi.registerRowMapper(ImportItem.class, BeanMapper.of(ImportItem.class));
    }

    @Override
    protected Class<ImportItem> getClazz() {
        return ImportItem.class;
    }

    public List<ImportItem> findByItem(UUID itemId) {
        return super.findAllByProperty("import_id", itemId);
    }


}
