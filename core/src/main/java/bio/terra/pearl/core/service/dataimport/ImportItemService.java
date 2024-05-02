package bio.terra.pearl.core.service.dataimport;

import bio.terra.pearl.core.dao.dataimport.ImportItemDao;
import bio.terra.pearl.core.model.dataimport.Import;
import bio.terra.pearl.core.model.dataimport.ImportItem;
import bio.terra.pearl.core.model.dataimport.ImportItemStatus;
import bio.terra.pearl.core.service.CrudService;
import bio.terra.pearl.core.service.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class ImportItemService extends CrudService<ImportItem, ImportItemDao> {

    public ImportItemService(ImportItemDao dao) {
        super(dao);
    }

    public void attachImportItems(Import dataImport) {
        dataImport.setImportItems(dao.findAllByImport(dataImport.getId()));
    }

    public ImportItem updateStatus(UUID id, ImportItemStatus status) {
        ImportItem importItem = dao.find(id).orElseThrow(() -> new NotFoundException("Import Item not found for id: " + id));
        importItem.setStatus(status);
        return dao.update(importItem);
    }

    public void updateStatusByImportId(UUID importId, ImportItemStatus status) {
        List<ImportItem> importItems = dao.findAllByImport(importId);
        importItems.forEach(importItem -> {
            importItem.setStatus(status);
            dao.update(importItem);
        });
    }

}
