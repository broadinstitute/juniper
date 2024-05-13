package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.dao.survey.AnswerMappingDao;
import bio.terra.pearl.core.model.survey.AnswerMapping;
import bio.terra.pearl.core.service.ImmutableEntityService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AnswerMappingService extends ImmutableEntityService<AnswerMapping, AnswerMappingDao> {
    public AnswerMappingService(AnswerMappingDao dao) {
        super(dao);
    }

    public List<AnswerMapping> findBySurveyId(UUID survey) {
        return dao.findBySurveyId(survey);
    }
}
