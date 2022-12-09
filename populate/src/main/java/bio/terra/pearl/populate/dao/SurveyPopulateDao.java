package bio.terra.pearl.populate.dao;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.survey.Survey;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

/**
 * This should NEVER be used outside of populating contexts, as it changes entities our application otherwise
 * assumes are immutable.
 */
@Component
public class SurveyPopulateDao extends BaseMutableJdbiDao<Survey> {
    public SurveyPopulateDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<Survey> getClazz() {
        return Survey.class;
    }
}
