package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.dao.survey.AnswerDao;
import bio.terra.pearl.core.model.survey.Answer;
import bio.terra.pearl.core.service.CrudService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AnswerService extends CrudService<Answer, AnswerDao> {
    public AnswerService(AnswerDao dao) {
        super(dao);
    }

    public Optional<Answer> findForQuestion(UUID surveyResponseId, String questionStableId) {
        return dao.findByResponseQuestion(surveyResponseId, questionStableId);
    }

    public List<Answer> findByResponse(UUID surveyResponseId) {
        return dao.findByResponse(surveyResponseId);
    }

    public List<Answer> findByResponseAndQuestions(UUID surveyResponseId, List<String> questionStableIds) {
        return dao.findByResponseAndQuestions(surveyResponseId, questionStableIds);
    }

    public List<Answer> findByEnrollee(UUID enrolleeID) {
        return dao.findByEnrollee(enrolleeID);
    }

    public List<Answer> findByEnrolleeAndSurvey(UUID enrolleeID, String surveyStableId) {
        return dao.findByEnrolleeAndSurvey(enrolleeID, surveyStableId);
    }

    public Optional<Answer> findForEnrolleeByQuestion(UUID enrolleeID, String surveyStableId, String questionStableId) {
        return dao.findForEnrolleeByQuestion(enrolleeID, surveyStableId, questionStableId);
    }

    public void deleteByResponseId(UUID responseId) {
        dao.deleteByResponseId(responseId);
    }
}
