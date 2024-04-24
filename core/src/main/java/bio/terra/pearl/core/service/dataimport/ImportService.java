package bio.terra.pearl.core.service.dataimport;

import bio.terra.pearl.core.dao.dataimport.ImportDao;
import bio.terra.pearl.core.model.dataimport.Import;
import bio.terra.pearl.core.service.CrudService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ImportService extends CrudService<Import, ImportDao> {

    private final ImportDao importDao;

    public ImportService(ImportDao dao, ImportDao importDao) {
        super(dao);
        this.importDao = importDao;
    }

}
