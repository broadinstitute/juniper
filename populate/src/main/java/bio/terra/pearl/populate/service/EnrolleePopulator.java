package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.dao.survey.PreEnrollmentResponseDao;
import bio.terra.pearl.core.model.consent.ConsentForm;
import bio.terra.pearl.core.model.consent.ConsentResponse;
import bio.terra.pearl.core.model.consent.ConsentResponseDto;
import bio.terra.pearl.core.model.notification.NotificationConfig;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.Answer;
import bio.terra.pearl.core.model.survey.PreEnrollmentResponse;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.survey.SurveyResponse;
import bio.terra.pearl.core.model.workflow.HubResponse;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.consent.ConsentFormService;
import bio.terra.pearl.core.service.consent.ConsentResponseService;
import bio.terra.pearl.core.service.notification.NotificationConfigService;
import bio.terra.pearl.core.service.notification.NotificationService;
import bio.terra.pearl.core.service.participant.*;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.survey.AnswerProcessingService;
import bio.terra.pearl.core.service.survey.SurveyResponseService;
import bio.terra.pearl.core.service.survey.SurveyService;
import bio.terra.pearl.core.service.workflow.EnrollmentService;
import bio.terra.pearl.populate.dto.EnrolleePopDto;
import bio.terra.pearl.populate.dto.ParticipantTaskPopDto;
import bio.terra.pearl.populate.dto.consent.ConsentResponsePopDto;
import bio.terra.pearl.populate.dto.notifications.NotificationPopDto;
import bio.terra.pearl.populate.dto.survey.AnswerPopDto;
import bio.terra.pearl.populate.dto.survey.PreEnrollmentResponsePopDto;
import bio.terra.pearl.populate.dto.survey.SurveyResponsePopDto;
import bio.terra.pearl.populate.service.contexts.StudyPopulateContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class EnrolleePopulator extends BasePopulator<Enrollee, EnrolleePopDto, StudyPopulateContext> {
    private EnrolleeService enrolleeService;
    private StudyEnvironmentService studyEnvironmentService;
    private ParticipantUserService participantUserService;
    private PortalParticipantUserService portalParticipantUserService;
    private PreEnrollmentResponseDao preEnrollmentResponseDao;
    private SurveyService surveyService;
    private SurveyResponseService surveyResponseService;
    private ConsentFormService consentFormService;
    private ConsentResponseService consentResponseService;
    private ParticipantTaskService participantTaskService;
    private NotificationConfigService notificationConfigService;
    private NotificationService notificationService;
    private AnswerProcessingService answerProcessingService;
    private EnrollmentService enrollmentService;
    private ProfileService profileService;

    public EnrolleePopulator(EnrolleeService enrolleeService,
                             StudyEnvironmentService studyEnvironmentService,
                             ParticipantUserService participantUserService,
                             PortalParticipantUserService portalParticipantUserService,
                             PreEnrollmentResponseDao preEnrollmentResponseDao, SurveyService surveyService,
                             SurveyResponseService surveyResponseService, ConsentFormService consentFormService,
                             ConsentResponseService consentResponseService,
                             ParticipantTaskService participantTaskService,
                             NotificationConfigService notificationConfigService,
                             NotificationService notificationService, AnswerProcessingService answerProcessingService,
                             EnrollmentService enrollmentService, ProfileService profileService) {
        this.portalParticipantUserService = portalParticipantUserService;
        this.preEnrollmentResponseDao = preEnrollmentResponseDao;
        this.surveyService = surveyService;
        this.surveyResponseService = surveyResponseService;
        this.consentFormService = consentFormService;
        this.consentResponseService = consentResponseService;
        this.participantTaskService = participantTaskService;
        this.notificationConfigService = notificationConfigService;
        this.notificationService = notificationService;
        this.enrolleeService = enrolleeService;
        this.studyEnvironmentService = studyEnvironmentService;
        this.participantUserService = participantUserService;
        this.answerProcessingService = answerProcessingService;
        this.enrollmentService = enrollmentService;
        this.profileService = profileService;
    }

    private void populateResponse(Enrollee enrollee, SurveyResponsePopDto responsePopDto,
                                  PortalParticipantUser ppUser, boolean simulateSubmissions)
            throws JsonProcessingException {
        Survey survey = surveyService.findByStableIdWithMappings(responsePopDto.getSurveyStableId(),
                responsePopDto.getSurveyVersion()).get();

        SurveyResponse response = SurveyResponse.builder()
                .surveyId(survey.getId())
                .enrolleeId(enrollee.getId())
                .creatingParticipantUserId(enrollee.getParticipantUserId())
                .build();
        for (AnswerPopDto answerPopDto : responsePopDto.getAnswerPopDtos()) {
            Answer answer = convertAnswerPopDto(answerPopDto);
            response.getAnswers().add(answer);
        }
        SurveyResponse savedResponse;
        if (simulateSubmissions) {
            ParticipantTask task = participantTaskService
                    .findTaskForActivity(ppUser.getId(), enrollee.getStudyEnvironmentId(), survey.getStableId()).get();
            HubResponse<SurveyResponse> hubResponse = surveyResponseService
                    .submitResponse(response, ppUser.getParticipantUserId(), ppUser, enrollee, task.getId());
            savedResponse = hubResponse.getResponse();
        } else {
            savedResponse = surveyResponseService.create(response);
        }
        enrollee.getSurveyResponses().add(savedResponse);
    }

    public Answer convertAnswerPopDto(AnswerPopDto popDto) {
        // for now, do nothing -- SurveyResponse service takes care of setting the answer fields like enrolleeId
        // later, this will likely need to convert json values or otherwise handle convenience setters
        return popDto;
    }

    /** persists any preEnrollmentResponse, and then attaches it to the enrollee */
    private PreEnrollmentResponse populatePreEnrollResponse(EnrolleePopDto enrolleeDto, StudyEnvironment studyEnv) throws JsonProcessingException {
        PreEnrollmentResponsePopDto responsePopDto = enrolleeDto.getPreEnrollmentResponseDto();
        if (responsePopDto == null) {
            return null;
        }
        Survey survey = surveyService.findByStableId(responsePopDto.getSurveyStableId(),
                responsePopDto.getSurveyVersion()).get();
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

    private void populateConsent(Enrollee enrollee, PortalParticipantUser ppUser, ConsentResponsePopDto responsePopDto,
                                 List<ParticipantTask> tasks, boolean simulateSubmissions) throws JsonProcessingException {
        ConsentForm consentForm = consentFormService.findByStableId(responsePopDto.getConsentStableId(),
                responsePopDto.getConsentVersion()).get();

        ConsentResponse savedResponse;
        if (simulateSubmissions) {
            ConsentResponseDto responseDto = ConsentResponseDto.builder()
                    .consentFormId(consentForm.getId())
                    .enrolleeId(enrollee.getId())
                    .creatingParticipantUserId(enrollee.getParticipantUserId())
                    .consented(responsePopDto.isConsented())
                    .answers(responsePopDto.getAnswers())
                    .build();
            ParticipantTask matchingTask = tasks.stream().filter(task ->
                    task.getTargetStableId().equals(consentForm.getStableId())).findFirst().orElse(null);

            HubResponse<ConsentResponse> hubResponse = consentResponseService.submitResponse(enrollee.getParticipantUserId(),
                    ppUser, enrollee, matchingTask.getId(), responseDto);
            savedResponse = hubResponse.getResponse();
        } else {
            String fullData = objectMapper.writeValueAsString(responsePopDto.getAnswers());
            ConsentResponse response = ConsentResponse.builder()
                    .consentFormId(consentForm.getId())
                    .enrolleeId(enrollee.getId())
                    .creatingParticipantUserId(enrollee.getParticipantUserId())
                    .consented(responsePopDto.isConsented())
                    .fullData(fullData)
                    .currentPageNo(responsePopDto.getCurrentPageNo())
                    .build();
            savedResponse = consentResponseService.create(response);
        }
        enrollee.getConsentResponses().add(savedResponse);
    }

    private void populateTask(Enrollee enrollee, PortalParticipantUser ppUser, ParticipantTaskPopDto taskDto) {
        taskDto.setEnrolleeId(enrollee.getId());
        taskDto.setStudyEnvironmentId(enrollee.getStudyEnvironmentId());
        taskDto.setPortalParticipantUserId(ppUser.getId());
        if (taskDto.getTargetName() == null) {
            taskDto.setTargetName(getTargetName(taskDto.getTaskType(), taskDto.getTargetStableId(),
                    taskDto.getTargetAssignedVersion()));
        }
        participantTaskService.create(taskDto);
    }

    private String getTargetName(TaskType taskType, String stableId, int version) {
        if (taskType.equals(TaskType.SURVEY)) {
            return surveyService.findByStableId(stableId, version).get().getName();
        } else if (taskType.equals(TaskType.CONSENT)) {
            return consentFormService.findByStableId(stableId, version).get().getName();
        }
        throw new IllegalArgumentException("cannot find target name for TaskType " + taskType);
    }

    private void populateNotifications(Enrollee enrollee, EnrolleePopDto enrolleeDto, UUID studyEnvironmentId,
    PortalParticipantUser ppUser) {
        List<NotificationConfig> notificationConfigs = notificationConfigService.findByStudyEnvironmentId(studyEnvironmentId);
        for (NotificationPopDto notificationPopDto : enrolleeDto.getNotifications()) {
            NotificationConfig matchedConfig = matchConfigToNotification(notificationConfigs, notificationPopDto);
            notificationPopDto.setNotificationConfigId(matchedConfig.getId());
            notificationPopDto.setStudyEnvironmentId(enrollee.getStudyEnvironmentId());
            notificationPopDto.setEnrolleeId(enrollee.getId());
            notificationPopDto.setParticipantUserId(enrollee.getParticipantUserId());
            notificationPopDto.setPortalEnvironmentId(ppUser.getPortalEnvironmentId());
            notificationService.create(notificationPopDto);
        }
    }

    /** quick-and-dirty match based on types -- this is not robust but it's sufficient for our current testing needs */
    private NotificationConfig matchConfigToNotification(List<NotificationConfig> notificationConfigs,
                                                         NotificationPopDto notification) {
        return notificationConfigs.stream().filter(config ->
                config.getEventType().equals(notification.getNotificationConfigEventType()) &&
                config.getNotificationType().equals(notification.getNotificationConfigType()) &&
                config.getDeliveryType().equals(notification.getDeliveryType()))
                .findFirst().orElse(null);
    }

    @Override
    protected Class<EnrolleePopDto> getDtoClazz() {
        return EnrolleePopDto.class;
    }

    @Override
    public Optional<Enrollee> findFromDto(EnrolleePopDto popDto, StudyPopulateContext context) {
        return enrolleeService.findOneByShortcode(popDto.getShortcode());
    }

    @Override
    public Enrollee overwriteExisting(Enrollee existingObj, EnrolleePopDto popDto, StudyPopulateContext context) throws IOException {
        enrolleeService.delete(existingObj.getId(), CascadeProperty.EMPTY_SET);
        return createNew(popDto, context, true);
    }

    @Override
    public Enrollee createPreserveExisting(Enrollee existingObj, EnrolleePopDto popDto, StudyPopulateContext context) throws IOException {
        // we don't support preserving existing synthetic enrollees yet
        enrolleeService.delete(existingObj.getId(), CascadeProperty.EMPTY_SET);
        return createNew(popDto, context, true);
    }

    @Override
    public Enrollee createNew(EnrolleePopDto popDto, StudyPopulateContext context, boolean overwrite) throws IOException {
        StudyEnvironment attachedEnv = studyEnvironmentService
                .findByStudy(context.getStudyShortcode(), context.getEnvironmentName()).get();
        popDto.setStudyEnvironmentId(attachedEnv.getId());
        ParticipantUser attachedUser = participantUserService
                .findOne(popDto.getLinkedUsername(), context.getEnvironmentName()).get();
        PortalParticipantUser ppUser = portalParticipantUserService
                .findOne(attachedUser.getId(), context.getPortalShortcode()).get();
        popDto.setParticipantUserId(attachedUser.getId());
        popDto.setProfileId(ppUser.getProfileId());

        PreEnrollmentResponse preEnrollmentResponse = populatePreEnrollResponse(popDto, attachedEnv);
        popDto.setPreEnrollmentResponseId(preEnrollmentResponse != null ? preEnrollmentResponse.getId() : null);

        Enrollee enrollee;
        List<ParticipantTask> tasks;

        // temporarily set the enrollee to doNotEmail so that we don't spam emails during population
        Profile profile = profileService.find(ppUser.getProfileId()).get();
        boolean isDoNotEmail = profile.isDoNotEmail();
        profile.setDoNotEmail(true);
        profileService.update(profile);

        if (popDto.isSimulateSubmissions()) {
            HubResponse<Enrollee>  hubResponse = enrollmentService.enroll(attachedUser, ppUser,
                    attachedEnv.getEnvironmentName(), context.getStudyShortcode(), popDto.getPreEnrollmentResponseId());
            enrollee = hubResponse.getEnrollee();
            tasks = hubResponse.getTasks();
            // we want the shortcode to not be random so that test enrollee urls are consistent, so set it manually
            enrollee.setShortcode(popDto.getShortcode());
            enrolleeService.update(enrollee);

        } else {
            enrollee = enrolleeService.create(popDto);
            tasks = new ArrayList<>();
        }

        for (SurveyResponsePopDto responsePopDto : popDto.getSurveyResponseDtos()) {
            populateResponse(enrollee, responsePopDto, ppUser, popDto.isSimulateSubmissions());
        }
        for (ConsentResponsePopDto consentPopDto : popDto.getConsentResponseDtos()) {
            populateConsent(enrollee, ppUser, consentPopDto, tasks, popDto.isSimulateSubmissions());
        }
        for (ParticipantTaskPopDto taskDto : popDto.getParticipantTaskDtos()) {
            populateTask(enrollee, ppUser, taskDto);
        }
        populateNotifications(enrollee, popDto, attachedEnv.getId(), ppUser);

        /**
         * restore the email status
         * note that the email process is async, and so this may reset the email preference before the email
         * process actually triggers.  That's ok, though, because the Enrollee information is loaded from the DB as
         * part of the synchronous submission processes, and that's what's passed to the EmailService.
         */
        profile = profileService.find(ppUser.getProfileId()).get();
        profile.setDoNotEmail(isDoNotEmail);
        profileService.update(profile);

        return enrollee;
    }

}

