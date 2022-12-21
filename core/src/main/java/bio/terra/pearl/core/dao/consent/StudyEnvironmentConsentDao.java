package bio.terra.pearl.core.dao.consent;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.consent.StudyEnvironmentConsent;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class StudyEnvironmentConsentDao extends BaseMutableJdbiDao<StudyEnvironmentConsent> {
    public StudyEnvironmentConsentDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<StudyEnvironmentConsent> getClazz() {
        return StudyEnvironmentConsent.class;
    }
}
