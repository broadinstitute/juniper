package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.dao.admin.AdminUserDao;
import bio.terra.pearl.core.dao.kit.KitTypeDao;
import bio.terra.pearl.core.dao.survey.PreEnrollmentResponseDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.consent.ConsentForm;
import bio.terra.pearl.core.model.consent.ConsentResponse;
import bio.terra.pearl.core.model.consent.ConsentResponseDto;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.kit.KitRequestStatus;
import bio.terra.pearl.core.model.notification.NotificationConfig;
import bio.terra.pearl.core.model.participant.*;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.Answer;
import bio.terra.pearl.core.model.survey.PreEnrollmentResponse;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.survey.SurveyResponse;
import bio.terra.pearl.core.model.workflow.*;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.consent.ConsentFormService;
import bio.terra.pearl.core.service.consent.ConsentResponseService;
import bio.terra.pearl.core.service.kit.KitRequestDetails;
import bio.terra.pearl.core.service.kit.KitRequestService;
import bio.terra.pearl.core.service.kit.pepper.PepperKit;
import bio.terra.pearl.core.service.notification.NotificationConfigService;
import bio.terra.pearl.core.service.notification.NotificationService;
import bio.terra.pearl.core.service.participant.*;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.survey.AnswerProcessingService;
import bio.terra.pearl.core.service.survey.SurveyResponseService;
import bio.terra.pearl.core.service.survey.SurveyService;
import bio.terra.pearl.core.service.workflow.EnrollmentService;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import bio.terra.pearl.populate.dao.TimeShiftPopulateDao;
import bio.terra.pearl.populate.dto.consent.ConsentResponsePopDto;
import bio.terra.pearl.populate.dto.kit.KitRequestPopDto;
import bio.terra.pearl.populate.dto.notifications.NotificationPopDto;
import bio.terra.pearl.populate.dto.participant.EnrolleePopDto;
import bio.terra.pearl.populate.dto.participant.ParticipantNotePopDto;
import bio.terra.pearl.populate.dto.participant.ParticipantTaskPopDto;
import bio.terra.pearl.populate.dto.survey.AnswerPopDto;
import bio.terra.pearl.populate.dto.survey.PreEnrollmentResponsePopDto;
import bio.terra.pearl.populate.dto.survey.SurveyResponsePopDto;
import bio.terra.pearl.populate.service.contexts.StudyPopulateContext;
import bio.terra.pearl.populate.util.PopulateUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;

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
    private WithdrawnEnrolleeService withdrawnEnrolleeService;
    private TimeShiftPopulateDao timeShiftPopulateDao;
    private KitRequestService kitRequestService;
    private KitTypeDao kitTypeDao;
    private AdminUserDao adminUserDao;
    private ParticipantNotePopulator participantNotePopulator;
    private ObjectMapper objectMapper;


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
                             EnrollmentService enrollmentService, ProfileService profileService,
                             WithdrawnEnrolleeService withdrawnEnrolleeService,
                             TimeShiftPopulateDao timeShiftPopulateDao,
                             KitRequestService kitRequestService, KitTypeDao kitTypeDao, AdminUserDao adminUserDao,
                             ParticipantNotePopulator participantNotePopulator,
                             ObjectMapper objectMapper
    ) {
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
        this.withdrawnEnrolleeService = withdrawnEnrolleeService;
        this.participantNotePopulator = participantNotePopulator;
        this.timeShiftPopulateDao = timeShiftPopulateDao;
        this.kitRequestService = kitRequestService;
        this.kitTypeDao = kitTypeDao;
        this.adminUserDao = adminUserDao;
        this.objectMapper = objectMapper;
    }

    private void populateResponse(Enrollee enrollee, SurveyResponsePopDto responsePopDto,
                                  PortalParticipantUser ppUser, boolean simulateSubmissions)
            throws JsonProcessingException {
        Survey survey = surveyService.findByStableIdWithMappings(responsePopDto.getSurveyStableId(),
                responsePopDto.getSurveyVersion()).get();

        SurveyResponse response = SurveyResponse.builder()
                .surveyId(survey.getId())
                .enrolleeId(enrollee.getId())
                .complete(responsePopDto.isComplete())
                .creatingParticipantUserId(enrollee.getParticipantUserId())
                .resumeData(makeResumeData(responsePopDto.getCurrentPageNo(), enrollee.getParticipantUserId()))
                .build();
        for (AnswerPopDto answerPopDto : responsePopDto.getAnswerPopDtos()) {
            Answer answer = convertAnswerPopDto(answerPopDto);
            response.getAnswers().add(answer);
        }
        SurveyResponse savedResponse;
        if (simulateSubmissions) {
            ParticipantTask task = participantTaskService
                    .findTaskForActivity(ppUser.getId(), enrollee.getStudyEnvironmentId(), survey.getStableId()).get();
            if (responsePopDto.getSurveyVersion() != task.getTargetAssignedVersion()) {
                /**
                 * in simulateSubmission mode, tasks will be automatically created with the curren versions of the survey.
                 * To enable mocking of submissions of prior versions, if the version specified in the enrollee's seed file
                 * doesn't match the latest, update the task so it's as if the task had been assigned to the prior versipm
                 */
                task.setTargetAssignedVersion(responsePopDto.getSurveyVersion());
                participantTaskService.update(task);
            }
            HubResponse<SurveyResponse> hubResponse = surveyResponseService
                    .updateResponse(response, ppUser.getParticipantUserId(), ppUser, enrollee, task.getId());
            savedResponse = hubResponse.getResponse();
            if (responsePopDto.isTimeShifted()) {
                timeShiftPopulateDao.changeSurveyResponseTime(savedResponse.getId(), responsePopDto.shiftedInstant());
                if (responsePopDto.isComplete()) {
                    timeShiftPopulateDao.changeTaskCompleteTime(task.getId(), responsePopDto.shiftedInstant());
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

    public Answer convertAnswerPopDto(AnswerPopDto popDto) throws JsonProcessingException {
        if (popDto.getObjectJsonValue() != null) {
            popDto.setObjectValue(objectMapper.writeValueAsString(popDto.getObjectJsonValue()));
        }
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

        ConsentResponseDto responseDto = ConsentResponseDto.builder()
                .consentFormId(consentForm.getId())
                .enrolleeId(enrollee.getId())
                .creatingParticipantUserId(enrollee.getParticipantUserId())
                .consented(responsePopDto.isConsented())
                .answers(responsePopDto.getAnswers())
                .resumeData(makeResumeData(responsePopDto.getCurrentPageNo(), enrollee.getParticipantUserId()))
                .build();
        if (simulateSubmissions) {
            HubResponse<ConsentResponse> hubResponse = consentResponseService.submitResponse(enrollee.getParticipantUserId(),
                    ppUser, enrollee, responseDto);
            savedResponse = hubResponse.getResponse();
        } else {
            String fullData = objectMapper.writeValueAsString(responsePopDto.getAnswers());
            responseDto.setFullData(fullData);
            savedResponse = consentResponseService.create(responseDto);
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

    /** creates the kit request in the DB and attaches it to the passed-in enrollee object */
    private KitRequest populateKitRequest(Enrollee enrollee, Profile profile, KitRequestPopDto kitRequestPopDto) throws JsonProcessingException {
        var adminUser = adminUserDao.findByUsername(kitRequestPopDto.getCreatingAdminUsername()).get();
        var kitType = kitTypeDao.findByName(kitRequestPopDto.getKitTypeName()).get();
        var sentToAddress = KitRequestService.makePepperKitAddress(profile);
        var kitRequest = KitRequest.builder()
                .creatingAdminUserId(adminUser.getId())
                .enrolleeId(enrollee.getId())
                .kitTypeId(kitType.getId())
                .sentToAddress(objectMapper.writeValueAsString(sentToAddress))
                .status(kitRequestPopDto.getStatus())
                .externalKit(kitRequestPopDto.getExternalKitJson().toString())
                .externalKitFetchedAt(Instant.now())
                .build();
        kitRequest = kitRequestService.create(kitRequest);
        if (kitRequestPopDto.getCreatedAt() != null) {
            timeShiftPopulateDao.changeKitCreationTime(kitRequest.getId(), kitRequestPopDto.getCreatedAt());
        }
        enrollee.getKitRequests().add(
            new KitRequestDetails(kitRequest, kitType, enrollee.getShortcode(), objectMapper));
        return kitRequest;
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
        var profileWithAddress = profileService.loadWithMailingAddress(profile.getId()).get();
        for (KitRequestPopDto kitRequestPopDto : popDto.getKitRequestDtos()) {
            populateKitRequest(enrollee, profileWithAddress, kitRequestPopDto);
        }
        populateNotifications(enrollee, popDto, attachedEnv.getId(), ppUser);
        for (ParticipantNotePopDto notePopDto : popDto.getParticipantNoteDtos()) {
            participantNotePopulator.populate(enrollee, notePopDto);
        }

        /**
         * restore the email status
         * note that the email process is async, and so this may reset the email preference before the email
         * process actually triggers.  That's ok, though, because the Enrollee information is loaded from the DB as
         * part of the synchronous submission processes, and that's what's passed to the EmailService.
         */
        profile = profileService.find(ppUser.getProfileId()).get();
        profile.setDoNotEmail(isDoNotEmail);
        profileService.update(profile);
        if (popDto.isTimeShifted()) {
            timeShiftPopulateDao.changeEnrolleeCreationTime(enrollee.getId(), popDto.shiftedInstant());
        }
        if (popDto.isWithdrawn()) {
            withdrawnEnrolleeService.withdrawEnrollee(enrollee);
        }
        return enrollee;
    }

    public void bulkPopulateEnrollees(String portalShortcode, EnvironmentName envName, String studyShortcode, List<String> usernamesToLink) {
        StudyPopulateContext context = new StudyPopulateContext("portals/" + portalShortcode + "/studies/" + studyShortcode + "/enrollees/seed.json", portalShortcode, studyShortcode, envName, new HashMap<>(), false);

        usernamesToLink.forEach(username -> {
            try {
                String fileString = filePopulateService.readFile(context.getRootFileName(), context);
                EnrolleePopDto popDto = objectMapper.readValue(fileString, getDtoClazz());
                popDto.setLinkedUsername(username);
                popDto.setConsented(PopulateUtils.randomBoolean(95)); //95% chance an enrollee will be consented
                popDto.setWithdrawn(PopulateUtils.randomBoolean(5)); //5% chance an enrollee will be withdrawn
                popDto.setSubmittedHoursAgo(PopulateUtils.randomInteger(0, 480)); //add some jitter to when they joined, to make graphs/views more interesting
                popDto.setShortcode(PopulateUtils.randomShortcode(""));

                if (PopulateUtils.randomBoolean(20)) {
                    popDto.getKitRequestDtos().clear();
                } else {
                    populateKitRequests(popDto);
                }

                populateFromDto(popDto, context, false);
            } catch (IOException e) {
                throw new RuntimeException("Unable to bulk populate enrollees due to error: " + e.getMessage());
            }
        });
    }

    private void populateKitRequests(EnrolleePopDto popDto) {
        popDto.getKitRequestDtos().forEach(kitDto -> {
            try {
                KitRequestStatus kitRequestStatus = PopulateUtils.randomItem(Arrays.asList(KitRequestStatus.values()));
                PepperKit pepperRequest = objectMapper.readValue(kitDto.getExternalKitJson().toString(), PepperKit.class);
                pepperRequest.setCurrentStatus(kitRequestStatus.toString());
                generateFakeDates(kitDto, kitRequestStatus, pepperRequest);
                kitDto.setExternalKitJson(objectMapper.valueToTree(pepperRequest));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void generateFakeDates(KitRequestPopDto kitDto, KitRequestStatus status, PepperKit pepperRequest) {
        Instant recent = Instant.now();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.systemDefault());
        switch (status) {
            // Intentional fall-through to set all dates up to and including the date of `status`
            case RECEIVED:
                recent = recent.minus(PopulateUtils.randomInteger(1, 72), HOURS);
                pepperRequest.setReceiveDate(formatter.format(recent));
            case SENT:
                recent = recent.minus(PopulateUtils.randomInteger(10, 30), DAYS);
                pepperRequest.setScanDate(formatter.format(recent));
                pepperRequest.setReturnTrackingNumber("1Z%s".formatted(PopulateUtils.randomString(12)));
            case QUEUED:
                recent = recent.minus(PopulateUtils.randomInteger(5, 7), DAYS);
                pepperRequest.setLabelDate(formatter.format(recent));
                pepperRequest.setTrackingNumber("1Z%s".formatted(PopulateUtils.randomString(12)));
            case CREATED:
                kitDto.setCreatedAt(recent.minus(PopulateUtils.randomInteger(12, 48), HOURS));
        }
    }


}

