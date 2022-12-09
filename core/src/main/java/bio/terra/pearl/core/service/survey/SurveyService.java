package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.dao.survey.SurveyDao;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.CascadeProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class SurveyService {
    private SurveyDao surveyDao;

    public SurveyService(SurveyDao surveyDao) {
        this.surveyDao = surveyDao;
    }

    public Optional<Survey> findByStableId(String stableId, int version) {
        return surveyDao.findByStableId(stableId, version);
    }

    @Transactional
    public Survey create(Survey survey) {
        return surveyDao.create(survey);
    }

    @Transactional
    public void delete(UUID surveyId, Set<CascadeProperty> cascades) {
        surveyDao.delete(surveyId);
    }


}
