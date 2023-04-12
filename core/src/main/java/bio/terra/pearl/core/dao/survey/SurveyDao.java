package bio.terra.pearl.core.dao.survey;

import bio.terra.pearl.core.dao.BaseVersionedJdbiDao;
import bio.terra.pearl.core.model.survey.Survey;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class SurveyDao extends BaseVersionedJdbiDao<Survey> {
    private AnswerMappingDao answerMappingDao;
    public SurveyDao(Jdbi jdbi, AnswerMappingDao answerMappingDao) {
        super(jdbi);
        this.answerMappingDao = answerMappingDao;
    }

    public Optional<Survey> findByStableIdWithMappings(String stableId, int version) {
        Optional<Survey> surveyOpt = findByTwoProperties("stable_id", stableId, "version", version);
        surveyOpt.ifPresent(survey -> {
            survey.setAnswerMappings(answerMappingDao.findBySurveyId(survey.getId()));
        });
        return surveyOpt;
    }

    public List<Survey> findByPortalId(UUID portalId) {
        return findAllByProperty("portal_id", portalId);
    }

    @Override
    protected Class<Survey> getClazz() {
        return Survey.class;
    }
}
