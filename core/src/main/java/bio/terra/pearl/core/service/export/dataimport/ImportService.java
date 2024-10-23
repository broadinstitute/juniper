package bio.terra.pearl.core.service.export.dataimport;

import bio.terra.pearl.core.dao.dataimport.ImportDao;
import bio.terra.pearl.core.model.dataimport.Import;
import bio.terra.pearl.core.model.dataimport.ImportItemStatus;
import bio.terra.pearl.core.model.dataimport.ImportStatus;
import bio.terra.pearl.core.service.CrudService;
import bio.terra.pearl.core.service.admin.AdminUserService;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class ImportService extends CrudService<Import, ImportDao> {

    @Autowired
    ImportItemService importItemService;

    @Autowired
    AdminUserService adminUserService;

    @Autowired
    EnrolleeService enrolleeService;

    public ImportService(ImportDao dao) {
        super(dao);
    }

    public List<Import> findByStudyEnvWithItems(UUID studyEnvId) {
        List<Import> imports = dao.findByStudyEnvironmentId(studyEnvId);
        //load ImportItems
        imports.forEach(anImport -> importItemService.attachImportItems(anImport));
        return imports;
    }

    @Transactional
    public Import updateStatus(UUID id, ImportStatus status) {
        Import dataImport = dao.find(id).orElseThrow(() -> new NotFoundException("Import not found "));
        dataImport.setStatus(status);
        //update all importItems
        importItemService.updateStatusByImportId(id, ImportItemStatus.DELETED);
        return dao.update(dataImport);
    }

    @Transactional
    public void deleteEnrolleesByImportId(UUID id) {
        Import dataImport = dao.find(id).orElseThrow(() -> new NotFoundException("Import not found "));
        //try to load
        importItemService.attachImportItems(dataImport);
        dataImport.getImportItems().forEach(importItem -> {
            if (importItem.getCreatedEnrolleeId() != null) {
                enrolleeService.delete(importItem.getCreatedEnrolleeId(), Set.of(EnrolleeService.AllowedCascades.PARTICIPANT_USER));
            }
        });
    }

    @Transactional
    public void deleteByStudyEnvId(UUID studyEnvironmentId) {
        List<Import> imports = dao.findByStudyEnvironmentId(studyEnvironmentId);
        imports.forEach(dataImport -> importItemService.deleteByImportId(dataImport.getId()));
        dao.deleteByStudyEnvironmentId(studyEnvironmentId);
    }

}
