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
import bio.terra.pearl.core.model.survey.*;
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
import bio.terra.pearl.core.service.survey.SnapshotProcessingService;
import bio.terra.pearl.core.service.survey.SurveyResponseService;
import bio.terra.pearl.core.service.survey.SurveyService;
import bio.terra.pearl.core.service.workflow.EnrollmentService;
import bio.terra.pearl.populate.dto.EnrolleePopDto;
import bio.terra.pearl.populate.dto.ParticipantTaskPopDto;
import bio.terra.pearl.populate.dto.consent.ConsentResponsePopDto;
import bio.terra.pearl.populate.dto.notifications.NotificationPopDto;
import bio.terra.pearl.populate.dto.survey.PreEnrollmentResponsePopDto;
import bio.terra.pearl.populate.dto.survey.ResponseSnapshotPopDto;
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
public class EnrolleePopulator extends Populator<Enrollee, StudyPopulateContext> {
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
    private SnapshotProcessingService snapshotProcessingService;
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
                             NotificationService notificationService, SnapshotProcessingService snapshotProcessingService,
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
        this.snapshotProcessingService = snapshotProcessingService;
        this.enrollmentService = enrollmentService;
        this.profileService = profileService;
    }

    @Override
    public Enrollee populateFromString(String fileString, StudyPopulateContext context) throws IOException {
        EnrolleePopDto enrolleeDto = objectMapper.readValue(fileString, EnrolleePopDto.class);
        Optional<Enrollee> existingEnrollee = enrolleeService.findOneByShortcode(enrolleeDto.getShortcode());
        existingEnrollee.ifPresent(exEnrollee ->
                enrolleeService.delete(exEnrollee.getId(), CascadeProperty.EMPTY_SET)
        );
        StudyEnvironment attachedEnv = studyEnvironmentService
                .findByStudy(context.getStudyShortcode(), context.getEnvironmentName()).get();
        enrolleeDto.setStudyEnvironmentId(attachedEnv.getId());
        ParticipantUser attachedUser = participantUserService
                .findOne(enrolleeDto.getLinkedUsername(), context.getEnvironmentName()).get();
        PortalParticipantUser ppUser = portalParticipantUserService
                .findOne(attachedUser.getId(), context.getPortalShortcode()).get();
        enrolleeDto.setParticipantUserId(attachedUser.getId());
        enrolleeDto.setProfileId(ppUser.getProfileId());

        PreEnrollmentResponse preEnrollmentResponse = populatePreEnrollResponse(enrolleeDto, attachedEnv);
        enrolleeDto.setPreEnrollmentResponseId(preEnrollmentResponse != null ? preEnrollmentResponse.getId() : null);
        Enrollee enrollee;
        List<ParticipantTask> tasks;

        // temporarily set the enrollee to doNotEmail so that we don't spam emails during population
        Profile profile = profileService.find(ppUser.getProfileId()).get();
        boolean isDoNotEmail = profile.isDoNotEmail();
        profile.setDoNotEmail(true);
        profileService.update(profile);

        if (enrolleeDto.isSimulateSubmissions()) {
            HubResponse<Enrollee>  hubResponse = enrollmentService.enroll(attachedUser, ppUser,
                    attachedEnv.getEnvironmentName(), context.getStudyShortcode(), enrolleeDto.getPreEnrollmentResponseId());
            enrollee = hubResponse.getEnrollee();
            tasks = hubResponse.getTasks();
            // we want the shortcode to not be random so that test enrollee urls are consistent, so set it manually
            enrollee.setShortcode(enrolleeDto.getShortcode());
            enrolleeService.update(enrollee);

        } else {
            enrollee = enrolleeService.create(enrolleeDto);
            tasks = new ArrayList<>();
        }

        for (SurveyResponsePopDto responsePopDto : enrolleeDto.getSurveyResponseDtos()) {
            populateResponse(enrollee, responsePopDto, ppUser);
        }
        for (ConsentResponsePopDto consentPopDto : enrolleeDto.getConsentResponseDtos()) {
            populateConsent(enrollee, ppUser, consentPopDto, tasks, enrolleeDto.isSimulateSubmissions());
        }
        for (ParticipantTaskPopDto taskDto : enrolleeDto.getParticipantTaskDtos()) {
            populateTask(enrollee, ppUser, taskDto);
        }
        populateNotifications(enrollee, enrolleeDto, attachedEnv.getId(), ppUser);

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

    private void populateResponse(Enrollee enrollee, SurveyResponsePopDto responsePopDto, PortalParticipantUser ppUser)
            throws JsonProcessingException {
        Survey survey = surveyService.findByStableIdWithMappings(responsePopDto.getSurveyStableId(),
                responsePopDto.getSurveyVersion()).get();

        SurveyResponse response = SurveyResponse.builder()
                .surveyId(survey.getId())
                .enrolleeId(enrollee.getId())
                .creatingParticipantUserId(enrollee.getParticipantUserId())
                .build();
        for (ResponseSnapshotPopDto snapDto : responsePopDto.getResponseSnapshotDtos()) {
            // we need to convert this to a ResponseData to hydrate some additional fields,
            // then we turn it back to JSON to serialize it to the database
            ResponseData responseData = objectMapper.convertValue(snapDto.getFullDataJson(), ResponseData.class);
            responseData = processResponsePopulateData(responseData);
            response.getSnapshots().add(
                    ResponseSnapshot.builder()
                            .fullData(objectMapper.writeValueAsString(responseData))
                            .resumeData(snapDto.getResumeDataJson().toString())
                            .creatingParticipantUserId(enrollee.getParticipantUserId())
                            .build()
            );
            if (snapDto.isProcessSnapshot()) {
                snapshotProcessingService.processAllAnswerMappings(responseData, survey.getAnswerMappings(),
                        ppUser, enrollee.getParticipantUserId(), enrollee.getId(), survey.getId());
            }
        }
        SurveyResponse savedResponse = surveyResponseService.create(response);
        enrollee.getSurveyResponses().add(savedResponse);
    }

    /** persists any preEnrollmentResponse, and then attaches it to the enrollee */
    private PreEnrollmentResponse populatePreEnrollResponse(EnrolleePopDto enrolleeDto, StudyEnvironment studyEnv) {
        PreEnrollmentResponsePopDto responsePopDto = enrolleeDto.getPreEnrollmentResponseDto();
        if (responsePopDto == null) {
            return null;
        }
        Survey survey = surveyService.findByStableId(responsePopDto.getSurveyStableId(),
                responsePopDto.getSurveyVersion()).get();

        PreEnrollmentResponse response = PreEnrollmentResponse.builder()
                .surveyId(survey.getId())
                .creatingParticipantUserId(enrolleeDto.getParticipantUserId())
                .studyEnvironmentId(studyEnv.getId())
                .qualified(responsePopDto.isQualified())
                .fullData(responsePopDto.getFullDataJson().toString())
                .build();

        return preEnrollmentResponseDao.create(response);
    }

    private void populateConsent(Enrollee enrollee, PortalParticipantUser ppUser, ConsentResponsePopDto responsePopDto,
                                 List<ParticipantTask> tasks, boolean simulateSubmissions) {
        ConsentForm consentForm = consentFormService.findByStableId(responsePopDto.getConsentStableId(),
                responsePopDto.getConsentVersion()).get();

        ConsentResponse savedResponse;
        if (simulateSubmissions) {
            ConsentResponseDto responseDto = ConsentResponseDto.builder()
                    .consentFormId(consentForm.getId())
                    .enrolleeId(enrollee.getId())
                    .creatingParticipantUserId(enrollee.getParticipantUserId())
                    .consented(responsePopDto.isConsented())
                    .parsedData(objectMapper.convertValue(responsePopDto.getFullDataJson(), ResponseData.class))
                    .resumeData(responsePopDto.getResumeDataJson().toString())
                    .build();
            ParticipantTask matchingTask = tasks.stream().filter(task ->
                    task.getTargetStableId().equals(consentForm.getStableId())).findFirst().orElse(null);

            HubResponse<ConsentResponse> hubResponse = consentResponseService.submitResponse(enrollee.getParticipantUserId(),
                    ppUser, enrollee.getShortcode(), matchingTask.getId(), responseDto);
            savedResponse = hubResponse.getResponse();
        } else {
            ConsentResponse response = ConsentResponse.builder()
                    .consentFormId(consentForm.getId())
                    .enrolleeId(enrollee.getId())
                    .creatingParticipantUserId(enrollee.getParticipantUserId())
                    .consented(responsePopDto.isConsented())
                    .fullData(responsePopDto.getFullDataJson().toString())
                    .resumeData(responsePopDto.getResumeDataJson().toString())
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

    /** iterates through the response data and adds displayValues and simpleValues as needed
     * convenience method so that the populate files don't have to repeat themselves */
    private ResponseData processResponsePopulateData(ResponseData fullData) {
        for (ResponseDataItem item : fullData.getItems()) {
            if (item.getValue() != null) {
                if (item.getSimpleValue() == null) {
                    item.setSimpleValue(item.getValue().asText());
                }
                if (item.getDisplayValue() == null) {
                    item.setDisplayValue(item.getValue().asText());
                }
            }
        }
        return fullData;
    }
}

