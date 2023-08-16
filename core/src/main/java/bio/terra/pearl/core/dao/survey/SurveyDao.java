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
    public List<Survey> findByStableIdNoContent(String stableId) {
        List<Survey> surveys = jdbi.withHandle(handle ->
                handle.createQuery("select id, name, version, stable_id from survey where stable_id = :stableId;")
                        .bind("stableId", stableId)
                        .mapTo(clazz)
                        .list()
        );
        return surveys;
    }

    public List<Survey> findByPortalId(UUID portalId) {
        return findAllByProperty("portal_id", portalId);
    }

    public List<Survey> findByPortalIdNoContent(UUID portalId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("select id, name, version, stable_id from survey where portal_id = :portalId;")
                        .bind("portalId", portalId)
                        .mapTo(clazz)
                        .list()
        );
    }

    @Override
    protected Class<Survey> getClazz() {
        return Survey.class;
    }
}
