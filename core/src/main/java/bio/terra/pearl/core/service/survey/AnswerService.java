package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.dao.survey.AnswerDao;
import bio.terra.pearl.core.model.survey.Answer;
import bio.terra.pearl.core.service.CrudService;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AnswerService extends CrudService<Answer, AnswerDao> {
    public AnswerService(AnswerDao dao) {
        super(dao);
    }

    public void deleteByResponseId(UUID responseId) {
        dao.deleteByResponseId(responseId);
    }
}
