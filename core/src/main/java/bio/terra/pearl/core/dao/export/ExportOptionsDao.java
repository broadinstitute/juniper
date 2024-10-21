package bio.terra.pearl.core.dao.export;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.export.ExportOptions;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExportOptionsDao extends BaseMutableJdbiDao<ExportOptions> {
    public ExportOptionsDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<ExportOptions> getClazz() {
        return ExportOptions.class;
    }
}
