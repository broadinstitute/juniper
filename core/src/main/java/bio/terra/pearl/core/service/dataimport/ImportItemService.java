package bio.terra.pearl.core.service.dataimport;

import bio.terra.pearl.core.dao.dataimport.ImportItemDao;
import bio.terra.pearl.core.model.dataimport.Import;
import bio.terra.pearl.core.model.dataimport.ImportItem;
import bio.terra.pearl.core.service.CrudService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ImportItemService extends CrudService<ImportItem, ImportItemDao> {

    public ImportItemService(ImportItemDao dao) {
        super(dao);
    }

    public void attachImportItems(Import dataImport) {
        dataImport.setImportItems(dao.findByItem(dataImport.getId()));
    }

}
