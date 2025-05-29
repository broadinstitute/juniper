package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.dao.survey.SurveyResponseDao;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.audit.ParticipantDataChange;
import bio.terra.pearl.core.model.audit.ResponsibleEntity;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.survey.*;
import bio.terra.pearl.core.model.workflow.*;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.CrudService;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.rule.EnrolleeContext;
import bio.terra.pearl.core.service.rule.EnrolleeContextService;
import bio.terra.pearl.core.service.study.StudyEnvironmentSurveyService;
import bio.terra.pearl.core.service.survey.event.EnrolleeSurveyEvent;
import bio.terra.pearl.core.service.workflow.EventService;
import bio.terra.pearl.core.service.workflow.ParticipantDataChangeService;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

@Service
public class SurveyResponseService extends CrudService<SurveyResponse, SurveyResponseDao> {
    private final AnswerService answerService;
    private final SurveyService surveyService;
    private final ParticipantTaskService participantTaskService;
    private final StudyEnvironmentSurveyService studyEnvironmentSurveyService;
    private final AnswerProcessingService answerProcessingService;
    private final ParticipantDataChangeService participantDataChangeService;
    private final SurveyTaskDispatcher surveyTaskDispatcher;
    private final EventService eventService;
    private final EnrolleeContextService enrolleeContextService;
    public static final String CONSENTED_ANSWER_STABLE_ID = "consented";

    public SurveyResponseService(SurveyResponseDao dao,
                                 AnswerService answerService,
                                 SurveyService surveyService,
                                 ParticipantTaskService participantTaskService,
                                 StudyEnvironmentSurveyService studyEnvironmentSurveyService,
                                 AnswerProcessingService answerProcessingService,
                                 EnrolleeContextService enrolleeContextService,
                                 ParticipantDataChangeService participantDataChangeService, @Lazy SurveyTaskDispatcher surveyTaskDispatcher, EventService eventService) {
        super(dao);
        this.answerService = answerService;
        this.surveyService = surveyService;
        this.participantTaskService = participantTaskService;
        this.studyEnvironmentSurveyService = studyEnvironmentSurveyService;
        this.answerProcessingService = answerProcessingService;
        this.participantDataChangeService = participantDataChangeService;
        this.surveyTaskDispatcher = surveyTaskDispatcher;
        this.eventService = eventService;
        this.enrolleeContextService = enrolleeContextService;
    }

    public List<SurveyResponse> findByEnrolleeId(UUID enrolleeId) {
        return dao.findByEnrolleeId(enrolleeId);
    }

    public Map<UUID, List<SurveyResponse>> findByEnrolleeIdsNotRemoved(List<UUID> enrolleeIds, boolean onlyComplete) {
        return dao.findByEnrolleeIdsNotRemoved(enrolleeIds, onlyComplete);
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
     * if no task is specified, will return the survey with no response
     */
    public SurveyWithResponse findWithActiveResponse(UUID studyEnvId, UUID portalId, String stableId, Integer version,
                                                     Enrollee enrollee, UUID taskId) {

        Survey form = surveyService.findByStableId(stableId, version, portalId).get();
        SurveyResponse lastResponse = null;
        if (taskId != null) {
            ParticipantTask task = participantTaskService.find(taskId).get();
            // if there is an associated task, try to find an associated response
            lastResponse = dao.findOneWithAnswers(task.getSurveyResponseId()).orElse(null);
        } else {
            // if there's no task specified, grab the most recently created response
            lastResponse = dao.findMostRecent(enrollee.getId(), form.getId()).orElse(null);
            if (lastResponse != null) {
                dao.attachAnswers(lastResponse);
                dao.attachParticipantFiles(lastResponse);
            }
        }

        StudyEnvironmentSurvey configSurvey = studyEnvironmentSurveyService
                .findActiveBySurvey(studyEnvId, stableId)
                .stream().findFirst().orElseThrow(() -> new NotFoundException("no active survey found"));
        configSurvey.setSurvey(form);
        return new SurveyWithResponse(
                configSurvey, lastResponse, getReferencedAnswers(enrollee, form)
        );
    }

    private List<Answer> getReferencedAnswers(Enrollee enrollee, Survey survey) {
        List<Answer> answers = new ArrayList<>();

        List<SurveyParseUtils.QuestionReference> referencedQuestions = survey.getReferencedQuestions().stream().map(SurveyParseUtils.QuestionReference::fromString).toList();

        for (SurveyParseUtils.QuestionReference referencedQuestion : referencedQuestions) {
            answerService
                    .findForEnrolleeByQuestion(enrollee.getId(), referencedQuestion.surveyStableId(), referencedQuestion.questionStableId())
                    .ifPresent(answers::add);
        }

        return answers;
    }

    //if the cutoff time has passed, create a new task and response
    private boolean shouldCreateNewLongitudinalTaskAndResponse(Integer createNewResponseAfterDays, Instant surveyResponseLastUpdatedAt) {
        if(createNewResponseAfterDays == null) {
            return false;
        }
        Instant cutoffTime = ZonedDateTime.now(ZoneOffset.UTC)
                .minusDays(createNewResponseAfterDays).toInstant();
        return surveyResponseLastUpdatedAt.isBefore(cutoffTime);
    }

    /**
     * Longitudinal tasks are editable by the participant if (and only if) the task is the most recent task for the survey
     */
    private boolean isLongitudinalTaskEditable(Enrollee enrollee, SurveyResponse surveyResponse, String surveyStableId) {

        List<ParticipantTask> tasks = participantTaskService.findAllTasksForActivityByEnrollee(enrollee.getId(), surveyStableId);

        Optional<ParticipantTask> taskOpt = tasks.stream().filter(t -> surveyResponse.getId().equals(t.getSurveyResponseId())).findFirst();
        if (taskOpt.isEmpty()) {
            return false; // task not found - shouldn't happen
        }
        ParticipantTask task = taskOpt.get();

        if (task.getStatus() == TaskStatus.REJECTED || task.getStatus() == TaskStatus.REMOVED) {
            return false; // cannot edit removed/rejected tasks
        }

        // with a completed task, we can only edit if it's the
        // latest task for the survey
        List<ParticipantTask> nonRemovedTasks = tasks
                .stream()
                .filter(t -> t.getStatus() != TaskStatus.REMOVED && t.getStatus() != TaskStatus.REJECTED)
                .sorted(Comparator.comparing(ParticipantTask::getCreatedAt).reversed())
                .toList();

        if (nonRemovedTasks.isEmpty()) {
            return false;
        }

        // can only edit latest longitudinal - regardless if task status is new, inprogress, completed,
        // if it's old it's baked into the "history".
        return nonRemovedTasks.getFirst().getSurveyResponseId().equals(surveyResponse.getId());
    }

    /**
     * Creates a survey response and fires appropriate downstream events.
     */
    @Transactional
    public HubResponse<SurveyResponse> updateResponse(SurveyResponse responseDto, ResponsibleEntity operator,
                                                      String justification,
                                                      PortalParticipantUser ppUser,
                                                      Enrollee enrollee, UUID taskId, UUID portalId) {


        ParticipantTask task = participantTaskService.authTaskToEnrolleeId(taskId, enrollee.getId()).orElseThrow(() -> new NotFoundException("Task not found or not authorized for enrollee %s and task %s".formatted(enrollee.getId(), taskId)));

        Survey survey = surveyService.findByStableIdWithMappings(task.getTargetStableId(),
                task.getTargetAssignedVersion(), portalId).get();
        validateResponse(survey, task, responseDto.getAnswers());


        SurveyResponse priorResponse = dao.findOneWithAnswers(task.getSurveyResponseId()).orElse(null);
        SurveyResponse response;

        if (survey.getRecurrenceType() == RecurrenceType.LONGITUDINAL
                && priorResponse != null
                && !isLongitudinalTaskEditable(enrollee, priorResponse, survey.getStableId())
                // admins should be able to update old responses
                && operator.getParticipantUser() != null) {
            throw new IllegalArgumentException("Cannot update previous responses for longitudinal surveys");
        }

        //if the survey is longitudinal and we're past the cutoff point for updating an existing response, we need to create a new response and task
        if (survey.getRecurrenceType() == RecurrenceType.LONGITUDINAL
                && priorResponse != null
                && task.getStatus() == TaskStatus.COMPLETE
                && shouldCreateNewLongitudinalTaskAndResponse(survey.getCreateNewResponseAfterDays(), priorResponse.getLastUpdatedAt())
                // admin saving update should never trigger a new response;
                // they can re-assign if they want a fresh response
                && operator.getParticipantUser() != null
        ) {
            ParticipantTask newTask = participantTaskService.cleanForCopying(task);
            newTask.setStatus(TaskStatus.IN_PROGRESS);
            newTask.setCompletedAt(null);
            priorResponse.getAnswers().forEach(a -> a.setSurveyResponseId(null));
            response = surveyTaskDispatcher.createPrepopulatedSurveyResponse(priorResponse);
            newTask.setSurveyResponseId(response.getId());
            task = participantTaskService.create(newTask, null);
        } else {
            // find or create the SurveyResponse object to attach the snapshot
            response = findOrCreateResponse(task, enrollee, enrollee.getParticipantUserId(), responseDto, portalId, operator);
        }

        List<Answer> updatedAnswers = createOrUpdateAnswers(responseDto.getAnswers(), response, justification, survey, ppUser, operator);
        List<Answer> allAnswers = new ArrayList<>(response.getAnswers());
        List<Answer> existingAnswers = answerService.findByResponse(response.getId());
        allAnswers.addAll(existingAnswers);
        response.setAnswers(allAnswers);

        DataAuditInfo auditInfo = DataAuditInfo.builder()
                .enrolleeId(enrollee.getId())
                .surveyId(survey.getId())
                .portalParticipantUserId(ppUser.getId())
                .justification(justification)
                .build();
        auditInfo.setResponsibleEntity(operator);

        // process any answers that need to be propagated elsewhere to the data model
        answerProcessingService.processAllAnswerMappings(
                enrollee,
                responseDto.getAnswers(),
                survey.getAnswerMappings(),
                ppUser,
                operator,
                auditInfo);

        // now update the task status and response id
        task = updateTaskToResponse(task, response, updatedAnswers, auditInfo);

        // we only want to publish the event if the survey is complete
        // eventually, we may add other types of events like SURVEY_RESPONSE_UPDATED, or something
        // but for now we only care about eventing on completions or re-completions
        EnrolleeContext enrolleeContext;
        if(response.isComplete()) {
            EnrolleeSurveyEvent event = eventService.publishEnrolleeSurveyEvent(enrollee, response, ppUser, task);
            enrolleeContext = event.getEnrolleeContext();
        } else {
            enrollee.getParticipantTasks().clear();
            enrollee.getParticipantTasks().addAll(participantTaskService.findByEnrolleeId(enrollee.getId()));
            enrolleeContext = enrolleeContextService.fetchData(enrollee);
        }

        logger.info("SurveyResponse received -- enrollee: {}, surveyStabledId: {}", enrollee.getShortcode(), survey.getStableId());
        HubResponse<SurveyResponse> hubResponse = eventService.buildHubResponse(enrollee, enrolleeContext, response);
        return hubResponse;
    }

    /**
     * creates a new snapshot (along with a SurveyResponse container if needed) for the given task
     * This method does not do any validation or authorization -- callers should ensure the user
     * is authorized to update the given task/enrollee, and that the task corresponds to the snapshot
     */
    protected SurveyResponse findOrCreateResponse(ParticipantTask task, Enrollee enrollee,
                                                  UUID participantUserId, SurveyResponse responseDto,
                                                  UUID portalId, ResponsibleEntity operator) {
        UUID taskResponseId = task.getSurveyResponseId();
        Survey survey = surveyService.findByStableId(task.getTargetStableId(), task.getTargetAssignedVersion(), portalId).get();
        SurveyResponse response;
        if (taskResponseId != null) {
            response = dao.find(taskResponseId).get();
            // don't allow the response to be marked incomplete if it's already complete
            if (!response.isComplete()) {
                response.setComplete(responseDto.isComplete());
            }
            // to enable simultaneous editing with page-saving, update this to be a merge, rather than a set
            response.setResumeData(responseDto.getResumeData());
            dao.update(response);
        } else {
            SurveyResponse newResponse = SurveyResponse.builder()
                    .enrolleeId(enrollee.getId())
                    .surveyId(survey.getId())
                    .complete(responseDto.isComplete())
                    .resumeData(responseDto.getResumeData())
                    .createdAt(responseDto.getCreatedAt())
                    .lastUpdatedAt(responseDto.getLastUpdatedAt())
                    .build();
            newResponse.setResponsibleUser(operator);
            response = dao.create(newResponse);
        }

        return response;
    }

    protected ParticipantTask updateTaskToResponse(ParticipantTask task, SurveyResponse response,
                                                   List<Answer> updatedAnswers, DataAuditInfo auditInfo) {
        task.setSurveyResponseId(response.getId());
        TaskStatus updatedStatus = computeNewStatus(task, response, updatedAnswers);
        if (task.getStatus() != updatedStatus || task.getSurveyResponseId() != response.getId()) {
            task.setStatus(updatedStatus);
            task.setSurveyResponseId(response.getId());
            task = participantTaskService.update(task, auditInfo);
        }
        return task;
    }

    protected static TaskStatus computeNewStatus(ParticipantTask task, SurveyResponse response, List<Answer> updatedAnswers) {
        if (task.getStatus() == TaskStatus.COMPLETE) {
            // task statuses shouldn't ever change from complete to not
            return TaskStatus.COMPLETE;
        }

        if (response.isComplete()) {
            if (task.getTaskType().equals(TaskType.CONSENT)) {
                // consent tasks are only marked as complete if they are completed and consented
                boolean isConsented = isConsented(response, updatedAnswers);
                return isConsented ? TaskStatus.COMPLETE : TaskStatus.REJECTED;
            } else {
                return TaskStatus.COMPLETE;
            }
        } else if (task.getStatus() == TaskStatus.NEW && updatedAnswers.size() == 0) {
            // if the task is new and no answers we submitted, this is just indicating the survey was viewed
            return TaskStatus.VIEWED;
        } else if (updatedAnswers.size() > 0) {
            return TaskStatus.IN_PROGRESS;
        }
        // nothing has changed, so keep the status the same
        return task.getStatus();
    }

    /**
     * Creates and attaches the answers to the response.
     */
    @Transactional
    public List<Answer> createOrUpdateAnswers(List<Answer> answers, SurveyResponse response, String justification,
                                              Survey survey, PortalParticipantUser ppUser, ResponsibleEntity operator) {
        List<String> updatedStableIds = answers.stream().map(Answer::getQuestionStableId).toList();
        // bulk-fetch any existingAnswers that will need to be updated
        // note that we do not use any answer ids returned by the client -- we'd have to run a query on them anyway
        // to confirm they were in fact associated with this user & response.  So it's easier to just ignore user-supplied ids and
        // use the responseId (which we have already validated) and questionStableIds to get existing answers
        List<Answer> existingAnswers = answerService.findByResponseAndQuestions(response.getId(), updatedStableIds);

        // put the answers into a map by their questionStableId so we can quickly match them to the submitted answers
        Map<String, Answer> existingAnswerMap = new HashMap<>();
        for (Answer answer : existingAnswers) {
            existingAnswerMap.put(answer.getQuestionStableId(), answer);
        }
        List<ParticipantDataChange> changeRecords = new ArrayList<>();
        List<Answer> updatedAnswers = answers.stream().map(answer -> {
            Answer existing = existingAnswerMap.get(answer.getQuestionStableId());
            if (existing != null) {
                return updateAnswer(existing, answer, response, justification, survey, ppUser, changeRecords, operator);
            }
            return createAnswer(answer, response, survey, ppUser, operator);
        }).toList();
        participantDataChangeService.bulkCreate(changeRecords);
        return updatedAnswers;
    }

    @Transactional
    public Answer updateAnswer(Answer existing, Answer updated, SurveyResponse response, String justification,
                               Survey survey, PortalParticipantUser ppUser, List<ParticipantDataChange> changeRecords,
                               ResponsibleEntity operator) {
        if (existing.valuesEqual(updated)) {
            // if the values are the same, don't bother with an update
            return existing;
        }

        DataAuditInfo auditInfo = DataAuditInfo.builder()
                .enrolleeId(response.getEnrolleeId())
                .surveyId(survey.getId())
                .portalParticipantUserId(ppUser.getId())
                .justification(justification)
                .build();
        auditInfo.setResponsibleEntity(operator);

        ParticipantDataChange change = ParticipantDataChange.fromAuditInfo(auditInfo)
                .operationId(response.getId())
                .modelName(survey.getStableId())
                .modelId(response.getId())
                .fieldName(existing.getQuestionStableId())
                .oldValue(existing.valueAsString())
                .newValue(updated.valueAsString())
                .build();

        changeRecords.add(change);
        existing.inferTypeIfMissing();
        existing.setSurveyVersion(updated.getSurveyVersion());
        if (existing.getSurveyVersion() == 0) {
            // if the frontend didn't specify a specific version,
            // default to the assigned version
            existing.setSurveyVersion(survey.getVersion());
        }
        existing.copyValuesFrom(updated);
        return answerService.update(existing);
    }

    private Answer createAnswer(Answer answer, SurveyResponse response,
                                Survey survey, PortalParticipantUser ppUser, ResponsibleEntity operator) {
        if(operator.getParticipantUser() != null || operator.getAdminUser() != null) {
            answer.setCreatingEntity(operator);
        } else {
            //Answers created by a system process (i.e. enrollee import) should be associated with the participant user
            answer.setCreatingParticipantUserId(ppUser.getParticipantUserId());
        }
        answer.setSurveyResponseId(response.getId());
        answer.setSurveyStableId(survey.getStableId());
        if (answer.getSurveyVersion() == 0) {
            // if the frontend didn't specify a specific version,
            // default to the assigned version
            answer.setSurveyVersion(survey.getVersion());
        }
        answer.setEnrolleeId(response.getEnrolleeId());
        answer.inferTypeIfMissing();
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


    /**
     * for surveys of type CONSENT, if the survey has an explicit computed property "consented", that determines
     * consent.  Otherwise, the form is consented if it is complete
     */
    public static boolean isConsented(SurveyResponse response, List<Answer> responseAnswers) {
        return responseAnswers.stream().filter(answer -> answer.getQuestionStableId().equals(CONSENTED_ANSWER_STABLE_ID))
                .findFirst()
                .map(Answer::getBooleanValue)
                .orElse(response.isComplete());

    }
}
