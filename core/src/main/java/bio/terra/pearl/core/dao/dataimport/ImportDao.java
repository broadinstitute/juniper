package bio.terra.pearl.core.dao.dataimport;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.dao.StudyEnvAttachedDao;
import bio.terra.pearl.core.model.dataimport.Import;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class ImportDao extends BaseMutableJdbiDao<Import> implements StudyEnvAttachedDao<Import> {

    public ImportDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<Import> getClazz() {
        return Import.class;
    }
}
