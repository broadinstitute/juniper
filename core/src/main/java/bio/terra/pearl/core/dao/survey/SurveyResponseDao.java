package bio.terra.pearl.core.dao.survey;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.dao.file.ParticipantFileDao;
import bio.terra.pearl.core.model.file.ParticipantFile;
import bio.terra.pearl.core.model.survey.Answer;
import bio.terra.pearl.core.model.survey.SurveyResponse;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class SurveyResponseDao extends BaseMutableJdbiDao<SurveyResponse> {
    private final AnswerDao answerDao;
    private final ParticipantFileDao participantFileDao;

    public SurveyResponseDao(Jdbi jdbi, AnswerDao answerDao, ParticipantFileDao participantFileDao) {
        super(jdbi);
        this.answerDao = answerDao;
        this.participantFileDao = participantFileDao;
    }


    @Override
    protected Class<SurveyResponse> getClazz() {
        return SurveyResponse.class;
    }

    public List<SurveyResponse> findByEnrolleeId(UUID enrolleeId) {
        return findAllByProperty("enrollee_id", enrolleeId);
    }

    /** excludes responses that are associated with removed tasks */
    public Map<UUID, List<SurveyResponse>> findByEnrolleeIdsNotRemoved(List<UUID> enrolleeIds) {
        if (enrolleeIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return jdbi.withHandle(handle ->
                // left join to include responses that have no associated task (e.g. pre-enrollment)
                // "is distinct from" instead of != because != doesn't handle nulls, and we want to include nulls
                handle.createQuery("""
                                select sr.* from %s sr
                                left join participant_task task on task.survey_response_id = sr.id
                                where sr.enrollee_id in (<enrolleeIds>)
                                and task.status is distinct from 'REMOVED'
                                """.formatted(tableName))
                        .bindList("enrolleeIds", enrolleeIds)
                        .mapTo(clazz)
                        .stream().collect(Collectors.groupingBy(SurveyResponse::getEnrolleeId, Collectors.toList()))
        );
    }

    /**
     * this avoids N+1 querying, but is otherwise unoptimized. It grabs all the responses, then all the answers
     */
    public List<SurveyResponse> findByEnrolleeIdWithAnswers(UUID enrolleeId) {
        List<SurveyResponse> responses = findByEnrolleeId(enrolleeId);
        List<Answer> answers = answerDao.findByEnrolleeId(enrolleeId);
        // build a map of id -> response for more efficient assignment of answers
        Map<UUID, SurveyResponse> responseById = new HashMap<>();
        for (SurveyResponse response : responses) {
            responseById.put(response.getId(), response);
            attachParticipantFiles(response);
        }
        for (Answer answer : answers) {
            responseById.get(answer.getSurveyResponseId()).getAnswers().add(answer);
        }
        return responses;
    }

    public Optional<SurveyResponse> findOneWithAnswers(UUID responseId) {
        Optional<SurveyResponse> responseOpt = find(responseId);
        responseOpt.ifPresent(response -> {
            attachAnswers(response);
            attachParticipantFiles(response);
        });
        return responseOpt;
    }

    /** attaches answers to the passed-in response, and then returns it */
    public SurveyResponse attachAnswers(SurveyResponse response) {
        List<Answer> answers = answerDao.findByResponse(response.getId());
        response.setAnswers(answers);
        return response;
    }

    public SurveyResponse attachParticipantFiles(SurveyResponse response) {
        List<ParticipantFile> participantFiles = participantFileDao.findBySurveyResponseId(response.getId());
        response.setParticipantFiles(participantFiles);
        return response;
    }

    public Optional<SurveyResponse> findMostRecent(UUID enrolleeId, UUID surveyId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("select * from " + tableName + " where enrollee_id = :enrolleeId"
                                + " and survey_id = :surveyId order by created_at DESC LIMIT 1")
                        .bind("enrolleeId", enrolleeId)
                        .bind("surveyId", surveyId)
                        .mapTo(clazz)
                        .findOne()
        );
    }

    public List<SurveyResponse> findAllByEnrolleeAndSurveyId(
            UUID enrolleeId, UUID surveyId) {
        return findAllByTwoProperties(
                "enrollee_id", enrolleeId,
                "survey_id", surveyId
        );

    }
}
