package bio.terra.pearl.core.dao.survey;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.survey.PreregistrationResponse;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class PreregistrationResponseDao extends BaseMutableJdbiDao<PreregistrationResponse> {
    public PreregistrationResponseDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<PreregistrationResponse> getClazz() {
        return PreregistrationResponse.class;
    }
}
