package bio.terra.pearl.core.dao.file;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.dao.survey.AnswerDao;
import bio.terra.pearl.core.model.file.ParticipantFile;
import bio.terra.pearl.core.model.survey.Answer;
import bio.terra.pearl.core.model.survey.AnswerFormat;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ParticipantFileDao extends BaseJdbiDao<ParticipantFile> {
    private final AnswerDao answerDao;

    public ParticipantFileDao(Jdbi jdbi, AnswerDao answerDao) {
        super(jdbi);
        this.answerDao = answerDao;
    }

    @Override
    protected Class<ParticipantFile> getClazz() {
        return ParticipantFile.class;
    }

    public List<ParticipantFile> findBySurveyResponseId(UUID surveyResponseId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                                select file.* from %s file
                                inner join answer a on file.file_name = a.string_value and a.format = 'FILE_NAME'
                                where a.survey_response_id = :surveyResponseId
                                """.formatted(tableName))
                        .bind("surveyResponseId", surveyResponseId)
                        .mapTo(clazz)
                        .stream()
                        .toList()
        );
    }

    public List<ParticipantFile> findByEnrolleeIdWithAnswers(UUID enrolleeId) {
        List<ParticipantFile> participantFiles = findByEnrolleeId(enrolleeId);
        List<Answer> answers = answerDao.findByEnrolleeIdAndAnswerFormat(enrolleeId, AnswerFormat.FILE_NAME);

        Map<String, List<Answer>> answersByFileName = answers.stream().collect(Collectors.groupingBy(Answer::getStringValue));

        for (ParticipantFile file : participantFiles) {
            List<Answer> fileAnswers = answersByFileName.getOrDefault(file.getFileName(), new ArrayList<>());
            file.setAssociatedAnswers(fileAnswers);
        }

        return participantFiles;
    }

    public Optional<ParticipantFile> findWithAnswers(UUID id) {
        Optional<ParticipantFile> participantFile = find(id);
        participantFile.ifPresent(file -> {
            List<Answer> answers = answerDao.findByEnrolleeIdAndAnswerFormat(file.getEnrolleeId(), AnswerFormat.FILE_NAME);
            Map<String, List<Answer>> answersByFileName = answers.stream().collect(Collectors.groupingBy(Answer::getStringValue));
            List<Answer> answersForFile = answersByFileName.getOrDefault(file.getFileName(), new ArrayList<>());
            file.setAssociatedAnswers(answersForFile);
        });
        return participantFile;
    }

    public List<ParticipantFile> findByEnrolleeId(UUID enrolleeId) {
        return findAllByProperty("enrollee_id", enrolleeId);
    }

    public void deleteByEnrolleeId(UUID enrolleeId) {
        deleteByProperty("enrollee_id", enrolleeId);
    }

    public Optional<ParticipantFile> findByEnrolleeIdAndFileName(UUID enrolleeId, String fileName) {
        return findByTwoProperties("enrollee_id", enrolleeId, "file_name", fileName);
    }
}
