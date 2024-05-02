package bio.terra.pearl.core.service.dataimport;

import bio.terra.pearl.core.dao.dataimport.ImportDao;
import bio.terra.pearl.core.model.dataimport.Import;
import bio.terra.pearl.core.model.dataimport.ImportItemStatus;
import bio.terra.pearl.core.model.dataimport.ImportStatus;
import bio.terra.pearl.core.service.CrudService;
import bio.terra.pearl.core.service.admin.AdminUserService;
import bio.terra.pearl.core.service.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class ImportService extends CrudService<Import, ImportDao> {

    @Autowired
    ImportItemService importItemService;

    @Autowired
    AdminUserService adminUserService;

    public ImportService(ImportDao dao) {
        super(dao);
    }

    public List<Import> findAllByStudyEnv(UUID studyEnvId) {
        List<Import> imports = dao.findAllByStudyEnv(studyEnvId);
        //load ImportItems
        imports.forEach(anImport -> importItemService.attachImportItems(anImport));
        return imports;
    }

    public Import updateStatus(UUID id, ImportStatus status) {
        Import dataImport = dao.find(id).orElseThrow(() -> new NotFoundException("Import not found "));
        dataImport.setStatus(status);
        //update all importItems
        importItemService.updateStatusByImportId(id, ImportItemStatus.DELETED);
        return dao.update(dataImport);
    }

}
