package bio.terra.pearl.core.dao.survey;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.survey.PreEnrollmentResponse;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class PreEnrollmentResponseDao extends BaseMutableJdbiDao<PreEnrollmentResponse> {
    public PreEnrollmentResponseDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<PreEnrollmentResponse> getClazz() {
        return PreEnrollmentResponse.class;
    }
}
