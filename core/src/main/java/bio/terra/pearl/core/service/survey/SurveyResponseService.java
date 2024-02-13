package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.dao.survey.SurveyResponseDao;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.audit.DataChangeRecord;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.portal.PortalEnvironmentLanguage;
import bio.terra.pearl.core.model.survey.*;
import bio.terra.pearl.core.model.workflow.*;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.ImmutableEntityService;
import bio.terra.pearl.core.service.portal.PortalLanguageService;
import bio.terra.pearl.core.service.survey.event.EnrolleeSurveyEvent;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import bio.terra.pearl.core.service.study.StudyEnvironmentSurveyService;
import bio.terra.pearl.core.service.workflow.DataChangeRecordService;
import bio.terra.pearl.core.service.workflow.EventService;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class SurveyResponseService extends ImmutableEntityService<SurveyResponse, SurveyResponseDao> {
    private AnswerService answerService;
    private PortalLanguageService portalLanguageService;
    private SurveyService surveyService;
    private ParticipantTaskService participantTaskService;
    private StudyEnvironmentSurveyService studyEnvironmentSurveyService;
    private AnswerProcessingService answerProcessingService;
    private DataChangeRecordService dataChangeRecordService;
    private EventService eventService;

    public SurveyResponseService(SurveyResponseDao dao, AnswerService answerService,
                                 PortalLanguageService portalLanguageService, SurveyService surveyService,
                                 ParticipantTaskService participantTaskService,
                                 StudyEnvironmentSurveyService studyEnvironmentSurveyService,
                                 AnswerProcessingService answerProcessingService,
                                 DataChangeRecordService dataChangeRecordService, EventService eventService) {
        super(dao);
        this.answerService = answerService;
        this.portalLanguageService = portalLanguageService;
        this.surveyService = surveyService;
        this.participantTaskService = participantTaskService;
        this.studyEnvironmentSurveyService = studyEnvironmentSurveyService;
        this.answerProcessingService = answerProcessingService;
        this.dataChangeRecordService = dataChangeRecordService;
        this.eventService = eventService;
    }

    public List<SurveyResponse> findByEnrolleeId(UUID enrolleeId) {
        return dao.findByEnrolleeId(enrolleeId);
    }

    public Optional<SurveyResponse> findOneWithAnswers(UUID responseId) {
        return dao.findOneWithAnswers(responseId);
    }

    @Override
    public SurveyResponse create(SurveyResponse response) {
        SurveyResponse savedResponse = super.create(response);
        if (!response.getAnswers().isEmpty()) {
            Survey survey = surveyService.find(savedResponse.getSurveyId()).get();
            for (Answer answer : response.getAnswers()) {
                answer.setSurveyResponseId(savedResponse.getId());
                // if no explicit user is specified on the answer, it's the same as the user of the response
                if (answer.getCreatingAdminUserId() == null && answer.getCreatingParticipantUserId() == null) {
                    answer.setCreatingParticipantUserId(response.getCreatingParticipantUserId());
                    answer.setCreatingAdminUserId(response.getCreatingAdminUserId());
                }
                answer.setEnrolleeId(savedResponse.getEnrolleeId());
                answer.setSurveyStableId(survey.getStableId());
                answer.setSurveyVersion(survey.getVersion());
                Answer savedAnswer = answerService.create(answer);
                savedResponse.getAnswers().add(savedAnswer);
            }
        }
        return savedResponse;
    }

    /**
     * will load the survey and the surveyResponse associated with the task,
     * or the most recent survey response, with answers attached
     */
    public SurveyWithResponse findWithActiveResponse(UUID studyEnvId, String stableId, Integer version,
                                                     Enrollee enrollee, UUID taskId) {
        Survey form = surveyService.findByStableId(stableId, version).get();
        SurveyResponse lastResponse = null;
        if (taskId != null) {
            ParticipantTask task = participantTaskService.find(taskId).get();
            // if there is an associated task, try to find an associated response
            lastResponse = dao.findOneWithAnswers(task.getSurveyResponseId()).orElse(null);
        }

        if (lastResponse == null) {
            // if there's no response already associated with the task, grab the most recently created
            lastResponse = dao.findMostRecent(enrollee.getId(), form.getId()).orElse(null);
            if (lastResponse != null) {
                dao.attachAnswers(lastResponse);
            }
        }
        StudyEnvironmentSurvey configSurvey = studyEnvironmentSurveyService
                .findActiveBySurvey(studyEnvId, stableId).get();
        configSurvey.setSurvey(form);
        return new SurveyWithResponse(
                configSurvey, lastResponse
        );
    }

    /**
     * Creates a survey response and fires appropriate downstream events.
     */
    @Transactional
    public HubResponse<SurveyResponse> updateResponse(SurveyResponse responseDto, UUID participantUserId,
                                                      PortalParticipantUser ppUser,
                                                      Enrollee enrollee, UUID taskId) {

        ParticipantTask task = participantTaskService.authTaskToPortalParticipantUser(taskId, ppUser.getId()).get();
        Survey survey = surveyService.findByStableIdWithMappings(task.getTargetStableId(),
                task.getTargetAssignedVersion()).get();
        validateResponse(survey, task, responseDto.getAnswers());

        // find or create the SurveyResponse object to attach the snapshot
        SurveyResponse response = findOrCreateResponse(task, enrollee, participantUserId, responseDto);
        List<Answer> updatedAnswers = createOrUpdateAnswers(responseDto.getAnswers(), response, survey, ppUser);

        DataAuditInfo auditInfo = DataAuditInfo.builder()
                .responsibleUserId(participantUserId)
                .enrolleeId(enrollee.getId())
                .surveyId(survey.getId())
                .portalParticipantUserId(ppUser.getId())
                .build();

        // process any answers that need to be propagated elsewhere to the data model
        answerProcessingService.processAllAnswerMappings(responseDto.getAnswers(),
                survey.getAnswerMappings(), ppUser, auditInfo);

        // now update the task status and response id
        updateTaskToResponse(task, response, updatedAnswers, auditInfo);

        EnrolleeSurveyEvent event = eventService.publishEnrolleeSurveyEvent(enrollee, response, ppUser);
        logger.info("SurveyReponse received -- enrollee: {}, surveyStabledId: {}", enrollee.getShortcode(), survey.getStableId());
        HubResponse<SurveyResponse> hubResponse = eventService.buildHubResponse(event, response);
        return hubResponse;
    }

    /**
     * creates a new snapshot (along with a SurveyResponse container if needed) for the given task
     * This method does not do any validation or authorization -- callers should ensure the user
     * is authorized to update the given task/enrollee, and that the task corresponds to the snapshot
     */
    @Transactional
    public SurveyResponse findOrCreateResponse(ParticipantTask task, Enrollee enrollee,
                                                  UUID participantUserId, SurveyResponse responseDto) {
        UUID taskResponseId = task.getSurveyResponseId();
        Survey survey = surveyService.findByStableId(task.getTargetStableId(), task.getTargetAssignedVersion()).get();
        SurveyResponse response;
        if (taskResponseId != null) {
            response = dao.find(taskResponseId).get();
            // don't allow the response to be marked incomplete if it's already complete
            if(!response.isComplete()) {
                response.setComplete(responseDto.isComplete());
            }
            // to enable simultaneous editing with page-saving, update this to be a merge, rather than a set
            response.setResumeData(responseDto.getResumeData());
            dao.update(response);
        } else {
            SurveyResponse newResponse = SurveyResponse.builder()
                    .enrolleeId(enrollee.getId())
                    .creatingParticipantUserId(participantUserId)
                    .surveyId(survey.getId())
                    .complete(responseDto.isComplete())
                    .resumeData(responseDto.getResumeData())
                    .build();
            response = dao.create(newResponse);
        }

        return response;
    }

    protected ParticipantTask updateTaskToResponse(ParticipantTask task, SurveyResponse response,
                                                   List<Answer> updatedAnswers, DataAuditInfo auditInfo) {
        task.setSurveyResponseId(response.getId());
        if (task.getStatus() != TaskStatus.COMPLETE) { // task statuses shouldn't ever change from complete to not
            if (response.isComplete()) {
                task.setStatus(TaskStatus.COMPLETE);
            } else if (task.getStatus() == TaskStatus.NEW && updatedAnswers.size() == 0) {
                // if the task is new and no answers we submitted, this is just indicating the survey was viewed
                task.setStatus(TaskStatus.VIEWED);
            } else if (updatedAnswers.size() > 0) {
                task.setStatus(TaskStatus.IN_PROGRESS);
            }
        }
        return participantTaskService.update(task, auditInfo);
    }

    /** Creates and attaches the answers to the response. */
    @Transactional
    public List<Answer> createOrUpdateAnswers(List<Answer> answers, SurveyResponse response,
                                                 Survey survey, PortalParticipantUser ppUser) {
        List<String> updatedStableIds = answers.stream().map(Answer::getQuestionStableId).toList();
        // bulk-fetch any existingAnswers that will need to be updated
        // note that we do not use any answer ids returned by the client -- we'd have to run a query on them anyway
        // to confirm they were in fact associated with this user & response.  So it's easier to just ignore user-supplied ids and
        // use the responseId (which we have already validated) and questionStableIds to get existing answers
        List<Answer> existingAnswers = answerService.findByResponseAndQuestions(response.getId(), updatedStableIds);

        List<PortalEnvironmentLanguage> supportedLanguages = portalLanguageService.findByPortalEnvId(ppUser.getPortalEnvironmentId());
        for (Answer answer : answers) {
            if(answer.getViewedLanguage() != null) {
                Optional<PortalEnvironmentLanguage> langOpt = supportedLanguages.stream().filter(language -> language.getLanguageCode().equals(answer.getViewedLanguage().getLanguageCode())).findFirst();
                langOpt.ifPresentOrElse(
                        lang -> answer.setViewedLanguageId(lang.getId()),
                        () -> {
                            // if the language is somehow not supported, log an error and set the language to null.
                            // we don't want to error here because we don't want to lose the participants response
                            log.error("The specified language (langName: {}, langCode: {}, responseId: {}) is not supported by the portal environment",
                                    answer.getViewedLanguage().getLanguageName(),
                                    answer.getViewedLanguage().getLanguageCode(),
                                    response.getId());
                            answer.setViewedLanguageId(null);
                        }
                );
            }
        }

        // put the answers into a map by their questionStableId so we can quickly match them to the submitted answers
        Map<String, Answer> existingAnswerMap = new HashMap<>();
        for (Answer answer : existingAnswers) {
            existingAnswerMap.put(answer.getQuestionStableId(), answer);
        }
        List<DataChangeRecord> changeRecords = new ArrayList<>();
        List<Answer> updatedAnswers = answers.stream().map(answer -> {
            Answer existing = existingAnswerMap.get(answer.getQuestionStableId());
            if (existing != null) {
                return updateAnswer(existing, answer, response, survey, ppUser, changeRecords);
            }
            return createAnswer(answer, response, survey, ppUser);
        }).toList();
        dataChangeRecordService.bulkCreate(changeRecords);
        return updatedAnswers;
    }

    @Transactional
    public Answer updateAnswer(Answer existing, Answer updated, SurveyResponse response,
                                  Survey survey, PortalParticipantUser ppUser, List<DataChangeRecord> changeRecords) {
        if (existing.valuesEqual(updated)) {
            // if the values are the same, don't bother with an update
            return existing;
        }
        DataChangeRecord change = DataChangeRecord.builder()
                .surveyId(survey.getId())
                .enrolleeId(response.getEnrolleeId())
                .operationId(response.getId())
                .responsibleUserId(ppUser.getParticipantUserId())
                .portalParticipantUserId(ppUser.getId())
                .modelName(survey.getStableId())
                .fieldName(existing.getQuestionStableId())
                .oldValue(existing.valueAsString())
                .newValue(updated.valueAsString()).build();
        changeRecords.add(change);
        existing.setSurveyVersion(updated.getSurveyVersion());
        if (existing.getSurveyVersion() == 0)  {
            // if the frontend didn't specify a specific version,
            // default to the assigned version
            existing.setSurveyVersion(survey.getVersion());
        }
        existing.copyValuesFrom(updated);
        return answerService.update(existing);
    }

    private Answer createAnswer(Answer answer, SurveyResponse response,
                                  Survey survey, PortalParticipantUser ppUser) {
        answer.setCreatingParticipantUserId(ppUser.getParticipantUserId());
        answer.setSurveyResponseId(response.getId());
        answer.setSurveyStableId(survey.getStableId());
        if(answer.getViewedLanguage() != null) {
            answer.setViewedLanguageId(answer.getViewedLanguage().getId());
        }
        if (answer.getSurveyVersion() == 0) {
            // if the frontend didn't specify a specific version,
            // default to the assigned version
            answer.setSurveyVersion(survey.getVersion());
        }
        answer.setEnrolleeId(response.getEnrolleeId());
        return answerService.create(answer);
    }

    @Override
    public void delete(UUID responseId, Set<CascadeProperty> cascades) {
        answerService.deleteByResponseId(responseId);
        dao.delete(responseId);
    }

    public void validateResponse(Survey survey, ParticipantTask task, List<Answer> answers) {
        if (!survey.getStableId().equals(task.getTargetStableId())) {
            throw new IllegalArgumentException("submitted form does not match assigned task");
        }
        // This is where we'd want to check required fields if we want server-side validation of that
    }
}
