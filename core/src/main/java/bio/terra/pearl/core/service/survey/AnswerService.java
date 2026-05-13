package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.dao.survey.AnswerDao;
import bio.terra.pearl.core.model.file.FileAnswer;
import bio.terra.pearl.core.model.survey.Answer;
import bio.terra.pearl.core.model.survey.AnswerFormat;
import bio.terra.pearl.core.service.CrudService;
import bio.terra.pearl.core.service.file.ParticipantFileService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AnswerService extends CrudService<Answer, AnswerDao> {
    private final ParticipantFileService participantFileService;
    private final ObjectMapper objectMapper;

    public AnswerService(AnswerDao dao, ParticipantFileService participantFileService, ObjectMapper objectMapper) {
        super(dao);
        this.participantFileService = participantFileService;
        this.objectMapper = objectMapper;
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

    @Override
    @Transactional
    public Answer create(Answer answer) {
        validateAnswer(answer);
        return super.create(answer);
    }

    @Override
    @Transactional
    public Answer update(Answer answer) {
        validateAnswer(answer);
        return super.update(answer);
    }

    private void validateAnswer(Answer answer) {
        answer.inferTypeIfMissing();
        if (answer.getFormat() == null) {
            answer.setFormat(AnswerFormat.NONE);
        }

        if (answer.getFormat().equals(AnswerFormat.FILE_NAME) && answer.getStringValue() != null) {
            List<String> fileNames = List.of(answer.getStringValue().split(","));
            fileNames.forEach(fileName -> {
                participantFileService.findByEnrolleeIdAndFileName(answer.getEnrolleeId(), fileName)
                        .orElseThrow(() -> new IllegalArgumentException("File (%s) not found for answer".formatted(fileName)));
            });
        }

        if (answer.getFormat().equals(AnswerFormat.FILE_UPLOAD) && answer.getObjectValue() != null) {
            try {
                FileAnswer[] fileAnswers = objectMapper.readValue(answer.getObjectValue(), FileAnswer[].class);
                for (FileAnswer fileAnswer : fileAnswers) {
                    if (fileAnswer.getFileName() != null) {
                        participantFileService.findByEnrolleeIdAndFileName(answer.getEnrolleeId(), fileAnswer.getFileName())
                                .orElseThrow(() -> new IllegalArgumentException(
                                        "File (%s) not found for answer".formatted(fileAnswer.getFileName())));
                    }
                }
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Invalid FILE_UPLOAD answer format", e);
            }
        }
    }
}
