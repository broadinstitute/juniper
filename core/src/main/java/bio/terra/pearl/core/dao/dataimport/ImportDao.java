package bio.terra.pearl.core.dao.dataimport;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.dataimport.Import;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.springframework.stereotype.Component;

@Component
public class ImportDao extends BaseMutableJdbiDao<Import> {

    public ImportDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<Import> getClazz() {
        return Import.class;
    }

}
