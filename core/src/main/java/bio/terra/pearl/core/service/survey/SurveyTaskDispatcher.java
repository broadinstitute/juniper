package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.model.survey.*;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.rule.EnrolleeContextService;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpressionParser;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.study.StudyEnvironmentSurveyService;
import bio.terra.pearl.core.service.survey.event.EnrolleeSurveyEvent;
import bio.terra.pearl.core.service.survey.event.SurveyPublishedEvent;
import bio.terra.pearl.core.service.workflow.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/** listens for events and updates enrollee survey tasks accordingly */
@Service
@Slf4j
public class SurveyTaskDispatcher extends TaskDispatcher<SurveyTaskConfigDto> {
    private final StudyEnvironmentSurveyService studyEnvironmentSurveyService;
    private final SurveyService surveyService;
    private final SurveyResponseService surveyResponseService;


    public SurveyTaskDispatcher(StudyEnvironmentSurveyService studyEnvironmentSurveyService,
                                StudyEnvironmentService studyEnvironmentService, ParticipantTaskService participantTaskService,
                                EnrolleeService enrolleeService,
                                PortalParticipantUserService portalParticipantUserService,
                                EnrolleeContextService enrolleeContextService,
                                EnrolleeSearchExpressionParser enrolleeSearchExpressionParser, SurveyService surveyService, SurveyResponseService surveyResponseService) {
        super(studyEnvironmentService, participantTaskService, enrolleeService, enrolleeSearchExpressionParser, enrolleeContextService, portalParticipantUserService);
        this.studyEnvironmentSurveyService = studyEnvironmentSurveyService;
        this.surveyService = surveyService;
        this.surveyResponseService = surveyResponseService;
    }

    @EventListener
    @Order(DispatcherOrder.SURVEY_TASK)
    public void handleSurveyPublishedEvent(SurveyPublishedEvent newEvent) {
        SurveyTaskConfigDto surveyTaskConfigDto = findTaskConfigByStableId(newEvent.getStudyEnvironmentId(),
                newEvent.getStableId(),
                newEvent.getVersion())
                .orElseThrow(() -> new IllegalStateException("Could not find new survey task config"));
        updateTasksForNewTaskConfig(surveyTaskConfigDto);
    }

    /**
     * create the survey tasks for an enrollee's initial creation
     */
    @EventListener
    @Order(DispatcherOrder.SURVEY_TASK)
    public void handleNewEnrolleeEvent(EnrolleeCreationEvent enrolleeEvent) {
        syncTasksForEnrollee(enrolleeEvent.getEnrolleeContext());
    }

    /**
     * survey responses can update what surveys a person is eligible for -- recompute as needed
     */
    @EventListener
    @Order(DispatcherOrder.SURVEY_TASK)
    public void handleSurveyEvent(EnrolleeSurveyEvent enrolleeEvent) {
        /** for now, only recompute on updates involving a completed survey.  This will
         * avoid assigning surveys based on an answer that was quickly changed, since we don't
         * yet have functions for unassigning surveys */
        if (!enrolleeEvent.getSurveyResponse().isComplete()) {
            return;
        }
        syncTasksForEnrollee(enrolleeEvent.getEnrolleeContext());
    }


    @Override
    protected List<SurveyTaskConfigDto> findTaskConfigsByStudyEnvironment(UUID studyEnvId) {
        List<StudyEnvironmentSurvey> studyEnvironmentSurveys = studyEnvironmentSurveyService.findAllByStudyEnvId(studyEnvId, true);
        // load surveys
        return studyEnvironmentSurveys.stream()
                .map(ses -> {
                    Survey survey = surveyService.find(ses.getSurveyId())
                            .orElseThrow(() -> new NotFoundException("Could not find survey"));
                    return new SurveyTaskConfigDto(ses, survey);
                })
                .toList();

    }

    @Override
    protected Optional<SurveyTaskConfigDto> findTaskConfigForAssignDto(UUID studyEnvironmentId, ParticipantTaskAssignDto participantTaskAssignDto) {
        return findTaskConfigByStableId(
                studyEnvironmentId,
                participantTaskAssignDto.targetStableId(),
                participantTaskAssignDto.targetAssignedVersion());
    }

    @Override
    protected TaskType getTaskType(SurveyTaskConfigDto taskDto) {
        return taskTypeForSurveyType.get(taskDto.getSurvey().getSurveyType());
    }

    private final Map<SurveyType, TaskType> taskTypeForSurveyType = Map.of(
            SurveyType.CONSENT, TaskType.CONSENT,
            SurveyType.RESEARCH, TaskType.SURVEY,
            SurveyType.OUTREACH, TaskType.OUTREACH,
            SurveyType.ADMIN, TaskType.ADMIN_FORM,
            SurveyType.PRE_ENROLL, TaskType.SURVEY, // pre-enroll surveys are NOT typically assigned as tasks
            SurveyType.DOCUMENT_REQUEST, TaskType.DOCUMENT_REQUEST
    );

    protected Optional<SurveyTaskConfigDto> findTaskConfigByStableId(UUID studyEnvironmentId, String stableId, Integer version) {
        Optional<Survey> surveyOpt = version != null ?
                surveyService.findActiveByStudyEnvironmentIdAndStableIdNoContent(studyEnvironmentId, stableId, version)
                : surveyService.findLatestActiveByStudyEnvironmentIdAndStableIdNoContent(studyEnvironmentId, stableId);
        if (surveyOpt.isEmpty()) {
            return Optional.empty();
        }

        Optional<StudyEnvironmentSurvey> sesOpt = studyEnvironmentSurveyService
                .findActiveBySurvey(studyEnvironmentId, surveyOpt.get().getId());
        if (sesOpt.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new SurveyTaskConfigDto(sesOpt.get(), surveyOpt.get()));
    }

    @Override
    public void copyTaskData(ParticipantTask newTask, ParticipantTask oldTask, SurveyTaskConfigDto taskDispatchConfig) {
        super.copyTaskData(newTask, oldTask, taskDispatchConfig);

        newTask.setSurveyResponseId(null);
        newTask.setCompletedAt(null);

        // if the survey is set to prepopulate, copy the answers from the old task to the new task
        // we need to also create a new survey response for the new task and attach it to that task
        if(taskDispatchConfig.getSurvey().isPrepopulate()) {
            Optional<SurveyResponse> priorResponse = surveyResponseService.findOneWithAnswers(oldTask.getSurveyResponseId());

            if (priorResponse.isPresent() && taskDispatchConfig.getSurvey().isPrepopulate()) {
                SurveyResponse createdResponse = createPrepopulatedSurveyResponse(priorResponse.get());
                newTask.setStatus(TaskStatus.NEW);
                newTask.setSurveyResponseId(createdResponse.getId());
            }
        }
    }

    // creates a new survey response with the same answers as priorResponse
    // for use with longitudinal recurring surveys
    private SurveyResponse createPrepopulatedSurveyResponse(SurveyResponse priorResponse) {
        List<Answer> answers = priorResponse.getAnswers().stream()
                .map(a -> (Answer) a.cleanForCopying())
                .collect(Collectors.toList());

        SurveyResponse newResponse = SurveyResponse.builder()
                .surveyId(priorResponse.getSurveyId())
                .enrolleeId(priorResponse.getEnrolleeId())
                .createdAt(Instant.now())
                .lastUpdatedAt(null)
                .answers(answers)
                .participantFiles(priorResponse.getParticipantFiles())
                .build();

        return surveyResponseService.create(newResponse);
    }
}
