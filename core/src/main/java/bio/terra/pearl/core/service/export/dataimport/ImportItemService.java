package bio.terra.pearl.core.service.export.dataimport;

import bio.terra.pearl.core.dao.dataimport.ImportItemDao;
import bio.terra.pearl.core.model.dataimport.Import;
import bio.terra.pearl.core.model.dataimport.ImportItem;
import bio.terra.pearl.core.model.dataimport.ImportItemStatus;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.service.CrudService;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class ImportItemService extends CrudService<ImportItem, ImportItemDao> {

    private final EnrolleeService enrolleeService;

    public ImportItemService(ImportItemDao dao, EnrolleeService enrolleeService) {
        super(dao);
        this.enrolleeService = enrolleeService;
    }

    public void attachImportItems(Import dataImport) {
        List<ImportItem> items = dao.findAllByImport(dataImport.getId());
        attachEnrollees(items);
        dataImport.setImportItems(items);
    }

    private void attachEnrollees(List<ImportItem> items) {
        List<UUID> enrolleeIds = items.stream()
                .filter(item -> item.getStatus().equals(ImportItemStatus.SUCCESS))
                .map(ImportItem::getCreatedEnrolleeId)
                .filter(Objects::nonNull)
                .toList();

        if (!enrolleeIds.isEmpty()) {
            List<Enrollee> enrollees = enrolleeService.findAll(enrolleeIds);

            for (ImportItem item : items) {
                if (item.getCreatedEnrolleeId() != null) {
                    Enrollee enrollee = enrollees.stream()
                            .filter(e -> e.getId().equals(item.getCreatedEnrolleeId()))
                            .findFirst()
                            .orElse(null);
                    item.setCreatedEnrollee(enrollee);
                }
            }
        }
    }

    @Transactional
    public ImportItem updateStatus(UUID id, ImportItemStatus status) {
        ImportItem importItem = dao.find(id).orElseThrow(() -> new NotFoundException("Import Item not found for id: " + id));
        importItem.setStatus(status);
        return dao.update(importItem);
    }

    @Transactional
    public void updateStatusByImportId(UUID importId, ImportItemStatus status) {
        List<ImportItem> importItems = dao.findAllByImport(importId);
        importItems.forEach(importItem -> {
            importItem.setStatus(status);
            dao.update(importItem);
        });
    }

    @Transactional
    public void deleteEnrolleeByItemId(UUID id) {
        ImportItem importItem = dao.find(id).orElseThrow(() -> new NotFoundException("Import not found "));
        if (importItem.getCreatedEnrolleeId() != null) {
            enrolleeService.delete(importItem.getCreatedEnrolleeId(), Set.of(EnrolleeService.AllowedCascades.PARTICIPANT_USER));
        }
    }

    public void deleteByImportId(UUID importId) {
        dao.deleteByImportId(importId);
    }

}
