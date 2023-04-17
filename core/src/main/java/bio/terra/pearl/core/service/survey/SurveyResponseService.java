package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.dao.survey.SurveyResponseDao;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.survey.*;
import bio.terra.pearl.core.model.workflow.HubResponse;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.ImmutableEntityService;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.ParticipantTaskService;
import bio.terra.pearl.core.service.study.StudyEnvironmentSurveyService;
import bio.terra.pearl.core.service.workflow.EventService;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SurveyResponseService extends ImmutableEntityService<SurveyResponse, SurveyResponseDao> {
    private AnswerService answerService;
    private EnrolleeService enrolleeService;
    private SurveyService surveyService;
    private ParticipantTaskService participantTaskService;
    private StudyEnvironmentSurveyService studyEnvironmentSurveyService;
    private AnswerProcessingService answerProcessingService;
    private EventService eventService;

    public SurveyResponseService(SurveyResponseDao dao, AnswerService answerService,
                                 EnrolleeService enrolleeService, SurveyService surveyService,
                                 ParticipantTaskService participantTaskService,
                                 StudyEnvironmentSurveyService studyEnvironmentSurveyService,
                                 AnswerProcessingService answerProcessingService, EventService eventService) {
        super(dao);
        this.answerService = answerService;
        this.enrolleeService = enrolleeService;
        this.surveyService = surveyService;
        this.participantTaskService = participantTaskService;
        this.studyEnvironmentSurveyService = studyEnvironmentSurveyService;
        this.answerProcessingService = answerProcessingService;
        this.eventService = eventService;
    }

    public List<SurveyResponse> findByEnrolleeId(UUID enrolleeId) {
        return dao.findByEnrolleeId(enrolleeId);
    }

    public Optional<SurveyResponse> findOneWithLastSnapshot(UUID responseId) {
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
        // TODO -- this lookup should be by stableId, not formId
        StudyEnvironmentSurvey configSurvey = studyEnvironmentSurveyService
                .findBySurvey(studyEnvId, form.getId()).get();
        configSurvey.setSurvey(form);
        return new SurveyWithResponse(
                configSurvey, lastResponse
        );
    }

    /**
     * Creates a survey response and fires appropriate downstream events. 
     */
    @Transactional
    public HubResponse<SurveyResponse> submitResponse(SurveyResponse responseDto, UUID participantUserId,
                                                       PortalParticipantUser ppUser,
                                                       Enrollee enrollee, UUID taskId) {

        ParticipantTask task = participantTaskService.authTaskToPortalParticipantUser(taskId, ppUser.getId()).get();
        Survey survey = surveyService.findByStableIdWithMappings(task.getTargetStableId(),
                task.getTargetAssignedVersion()).get();
        validateResponse(survey, task, responseDto.getAnswers());

        // find or create the SurveyResponse object to attach the snapshot
        SurveyResponse response = findOrCreateResponse(task, enrollee, participantUserId, responseDto);
        createAnswers(response, responseDto.getAnswers(), survey, enrollee.getId(), participantUserId);

        // process any answers that need to be propagated elsewhere to the data model
        answerProcessingService.processAllAnswerMappings(responseDto.getAnswers(),
                survey.getAnswerMappings(), ppUser, participantUserId, enrollee.getId(), survey.getId());

        // now update the task status and response id
        task.setStatus(responseDto.isComplete() ? TaskStatus.COMPLETE : TaskStatus.IN_PROGRESS);
        task.setSurveyResponseId(response.getId());
        participantTaskService.update(task);

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
    protected SurveyResponse findOrCreateResponse(ParticipantTask task, Enrollee enrollee,
                                                  UUID participantUserId, SurveyResponse responseDto) {
        UUID taskResponseId = task.getSurveyResponseId();
        Survey survey = surveyService.findByStableId(task.getTargetStableId(), task.getTargetAssignedVersion()).get();
        SurveyResponse response = null;
        if (taskResponseId != null) {
            response = dao.find(taskResponseId).get();
            if (response.isComplete() != response.isComplete()) {
                response.setComplete(responseDto.isComplete());
                // we will need to merge, rather than set, resumeData to enable simultaneous editing with page-saving
                response.setResumeData(responseDto.getResumeData());
                dao.update(response);
            }
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

    /** Creates and attaches the answers to the response. */
    @Transactional
    protected List<Answer> createAnswers(SurveyResponse response, List<Answer> answers,
                                         Survey survey, UUID enrolleeId, UUID participantUserId) {
        List<Answer> newAnswers = new ArrayList<>();
        for (Answer answer : answers) {
            answer.setCreatingParticipantUserId(participantUserId);
            answer.setSurveyResponseId(response.getId());
            answer.setSurveyStableId(survey.getStableId());
            answer.setSurveyVersion(survey.getVersion());
            answer.setEnrolleeId(enrolleeId);
            Answer savedAnswer = answerService.create(answer);
            newAnswers.add(savedAnswer);
        }
        response.getAnswers().addAll(newAnswers);
        return newAnswers;
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
        // TODO check required fields
    }
}
