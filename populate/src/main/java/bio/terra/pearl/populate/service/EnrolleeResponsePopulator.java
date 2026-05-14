package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.dao.admin.AdminUserDao;
import bio.terra.pearl.core.dao.dataimport.TimeShiftDao;
import bio.terra.pearl.core.dao.survey.PreEnrollmentResponseDao;
import bio.terra.pearl.core.dao.survey.SurveyQuestionDefinitionDao;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.audit.ResponsibleEntity;
import bio.terra.pearl.core.model.file.ParticipantFile;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.*;
import bio.terra.pearl.core.model.workflow.HubResponse;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.exception.internal.InternalServerException;
import bio.terra.pearl.core.service.file.ParticipantFileService;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.survey.SurveyResponseService;
import bio.terra.pearl.core.service.survey.SurveyService;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import bio.terra.pearl.populate.dto.participant.EnrolleePopDto;
import bio.terra.pearl.populate.dto.survey.AnswerPopDto;
import bio.terra.pearl.populate.dto.survey.PreEnrollmentResponsePopDto;
import bio.terra.pearl.populate.dto.survey.SurveyResponsePopDto;
import bio.terra.pearl.populate.service.contexts.StudyPopulateContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class EnrolleeResponsePopulator {


    public EnrolleeResponsePopulator(PreEnrollmentResponseDao preEnrollmentResponseDao, ObjectMapper objectMapper, SurveyService surveyService, SurveyResponseService surveyResponseService, ParticipantTaskService participantTaskService, TimeShiftDao timeShiftDao, AdminUserDao adminUserDao, PortalService portalService, SurveyQuestionDefinitionDao surveyQuestionDefinitionDao, ParticipantFileService participantFileService) {
        this.preEnrollmentResponseDao = preEnrollmentResponseDao;
        this.objectMapper = objectMapper;
        this.surveyService = surveyService;
        this.surveyResponseService = surveyResponseService;
        this.participantTaskService = participantTaskService;
        this.timeShiftDao = timeShiftDao;
        this.adminUserDao = adminUserDao;
        this.portalService = portalService;
        this.surveyQuestionDefinitionDao = surveyQuestionDefinitionDao;
        this.participantFileService = participantFileService;
    }

    public void populateResponse(Enrollee enrollee,
                                 SurveyResponsePopDto responsePopDto,
                                 PortalParticipantUser ppUser,
                                 boolean simulateSubmissions, StudyPopulateContext context,
                                 ParticipantUser pUser)
            throws JsonProcessingException {
        ResponsibleEntity responsibleUser;
        if (responsePopDto.getCreatingAdminUsername() != null) {
            responsibleUser = new ResponsibleEntity(adminUserDao.findByUsername(responsePopDto.getCreatingAdminUsername()).get());
        } else {
            responsibleUser = new ResponsibleEntity(pUser);
        }
        Survey survey = surveyService.findByStableIdAndPortalShortcodeWithMappings(responsePopDto.getSurveyStableId(),
                responsePopDto.getSurveyVersion(), context.getPortalShortcode()).orElseThrow(() -> new NotFoundException("Survey not found " + context.applyShortcodeOverride(responsePopDto.getSurveyStableId())));

        SurveyResponseWithJustification response = SurveyResponseWithJustification.builder()
                .surveyId(survey.getId())
                .enrolleeId(enrollee.getId())
                .complete(responsePopDto.isComplete())
                .justification(responsePopDto.getJustification())
                .creatingParticipantUserId(enrollee.getParticipantUserId())
                .resumeData(makeResumeData(responsePopDto.getCurrentPageNo(), enrollee.getParticipantUserId()))
                .build();

        for (AnswerPopDto answerPopDto : responsePopDto.getAnswerPopDtos()) {
            Answer answer = convertAnswerPopDto(answerPopDto, enrollee.getId());
            response.getAnswers().add(answer);
        }
        DataAuditInfo auditInfo = DataAuditInfo.builder()
                .enrolleeId(enrollee.getId())
                .portalParticipantUserId(ppUser.getId()).build();

        auditInfo.setResponsibleEntity(responsibleUser);

        if (responsePopDto.getCreatingAdminUsername() != null) {
            AdminUser adminUser = adminUserDao.findByUsername(responsePopDto.getCreatingAdminUsername()).get();
            response.setCreatingAdminUserId(adminUser.getId());
            response.setCreatingParticipantUserId(null);
            auditInfo.setJustification(responsePopDto.getJustification());
        }

        SurveyResponse savedResponse;
        if (simulateSubmissions) {
            Instant taskCutoff = responsePopDto.shiftedInstant().plus(1, ChronoUnit.MINUTES);
            ParticipantTask task = participantTaskService
                    .findTaskForActivity(ppUser.getId(), enrollee.getStudyEnvironmentId(), survey.getStableId(), taskCutoff)
                    .orElseThrow(() -> new NotFoundException("No task found. enrollee: %s, survey: %s".formatted(enrollee.getShortcode(), survey.getStableId())));

            if (responsePopDto.getSurveyVersion() != task.getTargetAssignedVersion()) {
                /**
                 * in simulateSubmission mode, tasks will be automatically created with the curren versions of the survey.
                 * To enable mocking of submissions of prior versions, if the version specified in the enrollee's seed file
                 * doesn't match the latest, update the task so it's as if the task had been assigned to the prior versipm
                 */
                task.setTargetAssignedVersion(responsePopDto.getSurveyVersion());
                participantTaskService.update(task, auditInfo);
            }
            List<ParticipantTask> pastTasks = participantTaskService.findByEnrolleeId(enrollee.getId());
            HubResponse<SurveyResponse> hubResponse = surveyResponseService
                    .updateResponse(response, responsibleUser, responsePopDto.getJustification(), ppUser, enrollee, task.getId(), survey.getPortalId());
            savedResponse = hubResponse.getResponse();
            if (responsePopDto.isTimeShifted()) {
                timeShiftDao.changeSurveyResponseTime(savedResponse.getId(), responsePopDto.shiftedInstant());
                if (responsePopDto.isComplete()) {
                    timeShiftDao.changeTaskCompleteTime(task.getId(), responsePopDto.shiftedInstant());
                }

                // if this response created new tasks, update their creation times
                List<ParticipantTask> newTasks = hubResponse.getTasks().stream().filter(
                        t -> !pastTasks.stream().anyMatch(pt -> pt.getId().equals(t.getId()))
                ).toList();
                for (ParticipantTask newTask : newTasks) {
                    timeShiftDao.changeTaskCreationTime(newTask.getId(), responsePopDto.shiftedInstant());
                }
            }
        } else {
            savedResponse = surveyResponseService.create(response);
        }

        enrollee.getSurveyResponses().add(savedResponse);
    }

    public String makeResumeData(Integer currentPageNo, UUID participantUserId) throws JsonProcessingException {
        if (currentPageNo != null) {
            return objectMapper.writeValueAsString(Map.of(participantUserId,
                    Map.of("currentPageNo", currentPageNo)));
        }
        return null;
    }

    public Answer convertAnswerPopDto(AnswerPopDto popDto, UUID enrolleeId) throws JsonProcessingException {
        if (popDto.getObjectJsonValue() != null) {
            popDto.setObjectValue(objectMapper.writeValueAsString(popDto.getObjectJsonValue()));
        } else if (popDto.getFileNames() != null && !popDto.getFileNames().isEmpty()) {
            List<Map<String, String>> fileAnswers = popDto.getFileNames().stream()
                    .map(name -> {
                        ParticipantFile file = participantFileService.findByEnrolleeIdAndFileName(enrolleeId, name)
                                .orElseThrow(() -> new NotFoundException("Participant file not found: " + name));
                        return Map.of("participantFileId", file.getId().toString());
                    })
                    .toList();
            popDto.setObjectValue(objectMapper.writeValueAsString(fileAnswers));
            popDto.setAnswerType(bio.terra.pearl.core.model.survey.AnswerType.OBJECT);
            popDto.setFormat(bio.terra.pearl.core.model.survey.AnswerFormat.FILE_UPLOAD);
        }
        return popDto;
    }

    public Enrollee autoConsent(Enrollee enrollee, ParticipantUser user, PortalParticipantUser ppUser, EnrolleePopulateType popType, StudyPopulateContext popContext) {
        List<ParticipantTask> tasks = participantTaskService.findByEnrolleeId(enrollee.getId());
        List<ParticipantTask> consentTasks = tasks.stream().filter(task -> task.getTaskType().equals(TaskType.CONSENT)).toList();
        consentTasks.forEach(task -> autoCompleteSurvey(task, enrollee, user, ppUser, popType, popContext));
        return enrollee;
    }

    public Enrollee autoCompleteAll(Enrollee enrollee, ParticipantUser user, PortalParticipantUser ppUser, EnrolleePopulateType popType, StudyPopulateContext popContext) {
        List<ParticipantTask> tasks = participantTaskService.findByEnrolleeId(enrollee.getId());
        List<ParticipantTask> surveyTasks = tasks.stream().filter(task -> task.getTaskType().equals(TaskType.SURVEY)).toList();
        surveyTasks.forEach(task -> autoCompleteSurvey(task, enrollee, user, ppUser, popType, popContext));
        return enrollee;
    }

    public void autoCompleteSurvey(ParticipantTask task, Enrollee enrollee, ParticipantUser user, PortalParticipantUser ppUser, EnrolleePopulateType popType, StudyPopulateContext popContext) {
        UUID portalId = portalService.findByPortalEnvironmentId(ppUser.getPortalEnvironmentId()).get().getId();
        Survey survey = surveyService.findByStableId(task.getTargetStableId(), task.getTargetAssignedVersion(), portalId).get();
        List<SurveyQuestionDefinition> questionDefs = surveyQuestionDefinitionDao.findAllBySurveyId(survey.getId());

        List<AnswerPopDto> answers = questionDefs.stream().map(this::createAnswerFromQuestionDef).toList();
        SurveyResponsePopDto responsePopDto = SurveyResponsePopDto.builder()
                .surveyStableId(survey.getStableId())
                .surveyVersion(survey.getVersion())
                .complete(true)
                .answerPopDtos(answers)
                .build();
        try {
            populateResponse(enrollee, responsePopDto, ppUser, true, popContext, user);
        } catch (JsonProcessingException e) {
            throw new InternalServerException("Unable to complete survey enrollee due to error: " + e.getMessage());
        }
    }

    private AnswerPopDto createAnswerFromQuestionDef(SurveyQuestionDefinition questionDef) {
        AnswerPopDto answer = AnswerPopDto.builder()
                .questionStableId(questionDef.getQuestionStableId())
                .build();

        if (!StringUtils.isBlank(questionDef.getChoices())) {
            answer.setStringValue(parseAndSelectRandomChoice(questionDef.getChoices(), RandomStringUtils.randomAlphabetic(RandomUtils.nextInt(5, 30))));
        } else {
            answer.setStringValue(RandomStringUtils.randomAlphabetic(RandomUtils.nextInt(5, 30)));
        }
        return answer;
    }

    private String parseAndSelectRandomChoice(String choicesString, String defaultChoice) {
        // parse choices and select random one
        List<QuestionChoice> choices;
        try {
            choices = objectMapper.readValue(choicesString, new TypeReference<List<QuestionChoice>>() {
            });
        } catch (IllegalArgumentException | JsonProcessingException e) {
            log.warn("Unable to parse choices for question", e);
            choices = new ArrayList<>();
        }

        if (!choices.isEmpty()) {
            int valueIdx = RandomUtils.nextInt(0, choices.size());
            QuestionChoice choiceJson = choices.get(valueIdx);

            return choiceJson.stableId();
        }
        return defaultChoice;

    }

    /**
     * persists any preEnrollmentResponse, and then attaches it to the enrollee
     */
    public PreEnrollmentResponse populatePreEnrollResponse(EnrolleePopDto enrolleeDto, StudyEnvironment studyEnv, StudyPopulateContext context) throws JsonProcessingException {
        PreEnrollmentResponsePopDto responsePopDto = enrolleeDto.getPreEnrollmentResponseDto();
        if (responsePopDto == null) {
            return null;
        }
        Survey survey = surveyService.findByStableIdAndPortalShortcode(responsePopDto.getSurveyStableId(),
                responsePopDto.getSurveyVersion(), context.getPortalShortcode()).get();
        String fullData = objectMapper.writeValueAsString(responsePopDto.getAnswers());
        PreEnrollmentResponse response = PreEnrollmentResponse.builder()
                .surveyId(survey.getId())
                .creatingParticipantUserId(enrolleeDto.getParticipantUserId())
                .studyEnvironmentId(studyEnv.getId())
                .qualified(responsePopDto.isQualified())
                .fullData(fullData)
                .build();

        return preEnrollmentResponseDao.create(response);
    }

    private final PreEnrollmentResponseDao preEnrollmentResponseDao;
    private final ObjectMapper objectMapper;
    private final SurveyService surveyService;
    private final SurveyResponseService surveyResponseService;
    private final ParticipantTaskService participantTaskService;
    private final TimeShiftDao timeShiftDao;
    private final AdminUserDao adminUserDao;
    private final PortalService portalService;
    private final SurveyQuestionDefinitionDao surveyQuestionDefinitionDao;
    private final ParticipantFileService participantFileService;
}
