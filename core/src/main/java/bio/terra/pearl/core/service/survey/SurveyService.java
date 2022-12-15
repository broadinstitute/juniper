package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.dao.survey.SurveyDao;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.CrudService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SurveyService extends CrudService<Survey, SurveyDao> {

    public SurveyService(SurveyDao surveyDao) {
        super(surveyDao);
    }

    public Optional<Survey> findByStableId(String stableId, int version) {
        return dao.findByStableId(stableId, version);
    }

}
