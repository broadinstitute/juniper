package bio.terra.pearl.core.dao.consent;


import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.consent.ConsentResponse;
import java.util.List;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class ConsentResponseDao extends BaseJdbiDao<ConsentResponse> {
    public ConsentResponseDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<ConsentResponse> getClazz() {
        return ConsentResponse.class;
    }

    public List<ConsentResponse> findByEnrolleeId(UUID enrolleeId) {
        return findAllByProperty("enrollee_id", enrolleeId);
    }

    public List<ConsentResponse> findByEnrolleeId(UUID enrolleeId, UUID consentFormId) {
        return findAllByTwoProperties("enrollee_id", enrolleeId,
                "consent_form_id", consentFormId);
    }
}
