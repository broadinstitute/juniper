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
                handle.createQuery("select id, name, created_at, last_updated_at, version, stable_id, portal_id from survey where stable_id = :stableId;")
                        .bind("stableId", stableId)
                        .mapTo(clazz)
                        .list()
        );
        return surveys;
    }

    public List<Survey> findByPortalId(UUID portalId) {
        return findAllByProperty("portal_id", portalId);
    }

    /** get all the surveys, but without the content populated */
    public List<Survey> findAllNoContent(List<UUID> ids) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                        select id, name, version, published_version, stable_id 
                        from survey 
                        where id IN (<ids>);""")
                        .bindList("ids", ids)
                        .mapTo(clazz)
                        .list()
        );
    }

    @Override
    protected Class<Survey> getClazz() {
        return Survey.class;
    }
}
