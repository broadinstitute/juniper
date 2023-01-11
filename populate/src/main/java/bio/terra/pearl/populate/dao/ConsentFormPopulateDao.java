package bio.terra.pearl.populate.dao;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.consent.ConsentForm;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

/**
 * This should NEVER be used outside of populating contexts, as it changes entities our application otherwise
 * assumes are immutable.
 */
@Component
public class ConsentFormPopulateDao extends BaseMutableJdbiDao<ConsentForm> {
    public ConsentFormPopulateDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<ConsentForm> getClazz() {
        return ConsentForm.class;
    }
}
