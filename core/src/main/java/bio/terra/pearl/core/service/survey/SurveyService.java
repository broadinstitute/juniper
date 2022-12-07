package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.dao.survey.SurveyDao;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.CrudService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SurveyService extends CrudService<Survey, SurveyDao> {

    public SurveyService(SurveyDao surveyDao) {
        super(surveyDao);
    }

    public Optional<Survey> findByStableId(String stableId, int version) {
        return dao.findByStableId(stableId, version);
    }

    @Transactional
    public void deleteByPortalId(UUID portalId) {
        List<Survey> surveys = dao.findByPortalId(portalId);
        for (Survey survey : surveys) {
            delete(survey.getId(), new HashSet<>());
        }
    }

}
