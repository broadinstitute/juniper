package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.dao.admin.AdminUserDao;
import bio.terra.pearl.core.dao.dataimport.TimeShiftPopulateDao;
import bio.terra.pearl.core.dao.kit.KitTypeDao;
import bio.terra.pearl.core.dao.survey.PreEnrollmentResponseDao;
import bio.terra.pearl.core.dao.survey.SurveyQuestionDefinitionDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.audit.ResponsibleEntity;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.kit.KitRequestStatus;
import bio.terra.pearl.core.model.kit.KitType;
import bio.terra.pearl.core.model.notification.Trigger;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.*;
import bio.terra.pearl.core.model.workflow.HubResponse;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.exception.internal.InternalServerException;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.kit.KitRequestDto;
import bio.terra.pearl.core.service.kit.KitRequestService;
import bio.terra.pearl.core.service.kit.pepper.PepperKit;
import bio.terra.pearl.core.service.kit.pepper.PepperKitAddress;
import bio.terra.pearl.core.service.notification.NotificationService;
import bio.terra.pearl.core.service.notification.TriggerService;
import bio.terra.pearl.core.service.participant.*;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.survey.AnswerProcessingService;
import bio.terra.pearl.core.service.survey.SurveyResponseService;
import bio.terra.pearl.core.service.survey.SurveyService;
import bio.terra.pearl.core.service.workflow.EnrollmentService;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import bio.terra.pearl.populate.dao.ParticipantUserPopulateDao;
import bio.terra.pearl.populate.dto.kit.KitRequestPopDto;
import bio.terra.pearl.populate.dto.notifications.NotificationPopDto;
import bio.terra.pearl.populate.dto.participant.*;
import bio.terra.pearl.populate.dto.survey.AnswerPopDto;
import bio.terra.pearl.populate.dto.survey.PreEnrollmentResponsePopDto;
import bio.terra.pearl.populate.dto.survey.SurveyResponsePopDto;
import bio.terra.pearl.populate.service.contexts.StudyPopulateContext;
import bio.terra.pearl.populate.util.PopulateUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;

@Service
@Slf4j
public class EnrolleePopulator extends BasePopulator<Enrollee, EnrolleePopDto, StudyPopulateContext> {

    public EnrolleePopulator(EnrolleeService enrolleeService,
                             StudyEnvironmentService studyEnvironmentService,
                             ParticipantUserService participantUserService,
                             PortalParticipantUserService portalParticipantUserService,
                             PreEnrollmentResponseDao preEnrollmentResponseDao, SurveyService surveyService,
                             SurveyResponseService surveyResponseService,
                             ParticipantTaskService participantTaskService,
                             TriggerService triggerService,
                             NotificationService notificationService, AnswerProcessingService answerProcessingService,
                             EnrollmentService enrollmentService, ProfileService profileService,
                             WithdrawnEnrolleeService withdrawnEnrolleeService,
                             TimeShiftPopulateDao timeShiftPopulateDao,
                             KitRequestService kitRequestService, KitTypeDao kitTypeDao, AdminUserDao adminUserDao,
                             ParticipantNotePopulator participantNotePopulator,
                             ParticipantUserPopulateDao participantUserPopulateDao, PortalParticipantUserPopulator portalParticipantUserPopulator, ObjectMapper objectMapper, PortalService portalService,
                             SurveyQuestionDefinitionDao surveyQuestionDefinitionDao, ShortcodeService shortcodeService) {
        this.portalParticipantUserService = portalParticipantUserService;
        this.preEnrollmentResponseDao = preEnrollmentResponseDao;
        this.surveyService = surveyService;
        this.surveyResponseService = surveyResponseService;
        this.participantTaskService = participantTaskService;
        this.triggerService = triggerService;
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
        this.participantUserPopulateDao = participantUserPopulateDao;
        this.portalParticipantUserPopulator = portalParticipantUserPopulator;
        this.objectMapper = objectMapper;
        this.portalService = portalService;
        this.surveyQuestionDefinitionDao = surveyQuestionDefinitionDao;
        this.shortcodeService = shortcodeService;
    }

    private void populateResponse(Enrollee enrollee, SurveyResponsePopDto responsePopDto,
                                  PortalParticipantUser ppUser, boolean simulateSubmissions, StudyPopulateContext context,
                                  ParticipantUser pUser)
            throws JsonProcessingException {
        ResponsibleEntity responsibleUser;
        if(responsePopDto.getCreatingAdminUsername() != null) {
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
            Answer answer = convertAnswerPopDto(answerPopDto);
            response.getAnswers().add(answer);
        }
        DataAuditInfo auditInfo = DataAuditInfo.builder()
                .enrolleeId(enrollee.getId())
                .portalParticipantUserId(ppUser.getId()).build();

        auditInfo.setResponsibleEntity(responsibleUser);

        if(responsePopDto.getCreatingAdminUsername() != null) {
            AdminUser adminUser = adminUserDao.findByUsername(responsePopDto.getCreatingAdminUsername()).get();
            response.setCreatingAdminUserId(adminUser.getId());
            response.setCreatingParticipantUserId(null);
            auditInfo.setJustification(responsePopDto.getJustification());
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
                participantTaskService.update(task, auditInfo);
            }
            HubResponse<SurveyResponse> hubResponse = surveyResponseService
                    .updateResponse(response, responsibleUser, responsePopDto.getJustification(), ppUser, enrollee, task.getId(), survey.getPortalId());
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

    /**
     * persists any preEnrollmentResponse, and then attaches it to the enrollee
     */
    private PreEnrollmentResponse populatePreEnrollResponse(EnrolleePopDto enrolleeDto, StudyEnvironment studyEnv, StudyPopulateContext context) throws JsonProcessingException {
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

    private void populateTask(Enrollee enrollee, PortalParticipantUser ppUser, ParticipantTaskPopDto taskDto) {
        taskDto.setEnrolleeId(enrollee.getId());
        taskDto.setStudyEnvironmentId(enrollee.getStudyEnvironmentId());
        taskDto.setPortalParticipantUserId(ppUser.getId());
        Portal portal = portalService.findByPortalEnvironmentId(ppUser.getPortalEnvironmentId()).orElseThrow();
        DataAuditInfo auditInfo = DataAuditInfo.builder()
                .enrolleeId(enrollee.getId())
                .portalParticipantUserId(ppUser.getId())
                .systemProcess(DataAuditInfo.systemProcessName(getClass(), ".populateTask")).build();
        if (taskDto.getTargetName() == null) {
            taskDto.setTargetName(getTargetName(taskDto.getTaskType(), taskDto.getTargetStableId(), portal.getId(),
                    taskDto.getTargetAssignedVersion()));
        }
        participantTaskService.create(taskDto, auditInfo);
    }

    /**
     * creates the kit request in the DB and attaches it to the passed-in enrollee object
     */
    private KitRequest populateKitRequest(Enrollee enrollee, Profile profile, KitRequestPopDto kitRequestPopDto) throws JsonProcessingException {
        AdminUser adminUser = adminUserDao.findByUsername(kitRequestPopDto.getCreatingAdminUsername()).get();
        KitType kitType = kitTypeDao.findByName(kitRequestPopDto.getKitTypeName()).get();
        PepperKitAddress sentToAddress = KitRequestService.makePepperKitAddress(profile);
        KitRequest kitRequest = KitRequest.builder()
                .creatingAdminUserId(adminUser.getId())
                .enrolleeId(enrollee.getId())
                .kitTypeId(kitType.getId())
                .skipAddressValidation(kitRequestPopDto.isSkipAddressValidation())
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
                new KitRequestDto(kitRequest, kitType, enrollee.getShortcode(), objectMapper));
        return kitRequest;
    }

    private String getTargetName(TaskType taskType, String stableId, UUID portalId, int version) {
        if (taskType.equals(TaskType.SURVEY) || taskType.equals(TaskType.CONSENT)) {
            return surveyService.findByStableId(stableId, version, portalId).get().getName();
        }
        throw new IllegalArgumentException("cannot find target name for TaskType " + taskType);
    }

    private void populateNotifications(Enrollee enrollee, EnrolleePopDto enrolleeDto, UUID studyEnvironmentId,
                                       PortalParticipantUser ppUser) {
        List<Trigger> triggers = triggerService.findByStudyEnvironmentId(studyEnvironmentId);
        for (NotificationPopDto notificationPopDto : enrolleeDto.getNotifications()) {
            Trigger matchedConfig = matchTriggerToNotification(triggers, notificationPopDto);
            notificationPopDto.setTriggerId(matchedConfig.getId());
            notificationPopDto.setStudyEnvironmentId(enrollee.getStudyEnvironmentId());
            notificationPopDto.setEnrolleeId(enrollee.getId());
            notificationPopDto.setParticipantUserId(enrollee.getParticipantUserId());
            notificationPopDto.setPortalEnvironmentId(ppUser.getPortalEnvironmentId());
            notificationService.create(notificationPopDto);
        }
    }

    /**
     * quick-and-dirty match based on types -- this is not robust but it's sufficient for our current testing needs
     */
    private Trigger matchTriggerToNotification(List<Trigger> triggers,
                                               NotificationPopDto notification) {
        return triggers.stream().filter(config ->
                        config.getEventType().equals(notification.getTriggerEventType()) &&
                                config.getTriggerType().equals(notification.getTriggerType()) &&
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

        PreEnrollmentResponse preEnrollmentResponse = populatePreEnrollResponse(popDto, attachedEnv, context);
        popDto.setPreEnrollmentResponseId(preEnrollmentResponse != null ? preEnrollmentResponse.getId() : null);
        EnrolleePopulationData enrolleePopulationData = null;
        if (!popDto.getProxyPopDtos().isEmpty()) {
            // for now assuming only one proxy per user
            ProxyPopDto firstProxy = popDto.getProxyPopDtos().get(0);
            if (firstProxy.isEnrollAsProxy()) {
                enrolleePopulationData = this.createNewGovernedEnrollee(attachedEnv.getEnvironmentName(), popDto, context, firstProxy);
            }
        } else {
            enrolleePopulationData = this.createNewEnrollee(attachedEnv.getEnvironmentName(), popDto, context);
        }
        PortalParticipantUser ppUser = enrolleePopulationData.getPpUser();

        popDto.setProfileId(ppUser.getProfileId());
        populateEnrolleeData(enrolleePopulationData.getEnrollee(), popDto, ppUser,
                enrolleePopulationData.getTasks(), attachedEnv, context);
        updateDoNotEmail(ppUser, enrolleePopulationData.doNotEmailSetting, "createNew");
        return enrolleePopulationData.getEnrollee();
    }

    //creates a new independent adult (non-governed) enrollee
    private EnrolleePopulationData createNewEnrollee(EnvironmentName environmentName, EnrolleePopDto popDto, StudyPopulateContext context) {
        ParticipantUser attachedUser = findLinkedUser(popDto, environmentName);
        PortalParticipantUser ppUser = portalParticipantUserService
                .findOne(attachedUser.getId(), context.getPortalShortcode()).get();
        popDto.setParticipantUserId(attachedUser.getId());
        boolean prevEmailSetting = updateDoNotEmail(ppUser, true, "createNewEnrollee");
        Enrollee enrollee;
        List<ParticipantTask> tasks;
        if (popDto.isSimulateSubmissions()) {
            HubResponse<Enrollee> hubResponse = enrollmentService.enroll(ppUser, environmentName, context.getStudyShortcode(),
                    attachedUser, ppUser, popDto.getPreEnrollmentResponseId(), true);
            enrollee = hubResponse.getEnrollee();
            tasks = hubResponse.getTasks();
            // we want the shortcode to not be random so that test enrollee urls are consistent, so set it manually
            enrollee.setShortcode(popDto.getShortcode());
            enrolleeService.update(enrollee);
        } else {
            popDto.setProfileId(ppUser.getProfileId());
            enrollee = enrolleeService.create(popDto);
            tasks = new ArrayList<>();
        }
        return new EnrolleePopulationData(popDto, enrollee, ppUser, tasks, null, prevEmailSetting);
    }

    private ParticipantUser findLinkedUser(EnrolleePopDto popDto, EnvironmentName environmentName) {
        if (popDto.getLinkedUsernameKey() != null) {
            List<ParticipantUser> users = participantUserPopulateDao.findUserByPrefix(popDto.getLinkedUsernameKey(), environmentName);
            if (users.size() > 1) {
                throw new IllegalStateException("Multiple usernames found for enrollee with prefix: " + popDto.getLinkedUsernameKey());
            }
            if (users.size() == 0) {
                throw new IllegalStateException("No usernames found for enrollee with prefix: " + popDto.getLinkedUsernameKey());
            }
            return users.get(0);
        }
        return participantUserService
                .findOne(popDto.getLinkedUsername(), environmentName).get();
    }

    /**
     * updates the doNotEmail status so that we can avoid spamming emails during population.  Returns the *previous*
     * doNotEmail value for convenience in resetting it later
     */
    private boolean updateDoNotEmail(PortalParticipantUser ppUser, boolean doNotEmail, String methodName) {
        // temporarily set the enrollee to doNotEmail so that we don't spam emails during population
        Profile profile = profileService.find(ppUser.getProfileId()).orElseThrow();
        boolean previousValue = profile.isDoNotEmail();
        profile.setDoNotEmail(doNotEmail);
        profileService.update(profile,
                DataAuditInfo.builder()
                        .portalParticipantUserId(ppUser.getId())
                        .systemProcess(DataAuditInfo.systemProcessName(getClass(), methodName))
                        .build()
        );
        return previousValue;
    }

    private void populateEnrolleeData(Enrollee enrollee, EnrolleePopDto popDto, PortalParticipantUser ppUser,
                                      List<ParticipantTask> tasks, StudyEnvironment attachedEnv, StudyPopulateContext context)
            throws JsonProcessingException {
        ParticipantUser responsibleUser = participantUserService.find(enrollee.getParticipantUserId()).orElseThrow();
        for (SurveyResponsePopDto responsePopDto : popDto.getSurveyResponseDtos()) {
            populateResponse(enrollee, responsePopDto, ppUser, popDto.isSimulateSubmissions(), context, responsibleUser);
        }
        for (ParticipantTaskPopDto taskDto : popDto.getParticipantTaskDtos()) {
            populateTask(enrollee, ppUser, taskDto);
        }
        Profile profileWithAddress = profileService.loadWithMailingAddress(ppUser.getProfileId()).get();
        for (KitRequestPopDto kitRequestPopDto : popDto.getKitRequestDtos()) {
            populateKitRequest(enrollee, profileWithAddress, kitRequestPopDto);
        }
        populateNotifications(enrollee, popDto, attachedEnv.getId(), ppUser);
        for (ParticipantNotePopDto notePopDto : popDto.getParticipantNoteDtos()) {
            participantNotePopulator.populate(enrollee, notePopDto);
        }
        if (popDto.isTimeShifted()) {
            timeShiftPopulateDao.changeEnrolleeCreationTime(enrollee.getId(), popDto.shiftedInstant());
        }
        if (popDto.isWithdrawn()) {
            withdrawnEnrolleeService.withdrawEnrollee(enrollee, DataAuditInfo.builder().systemProcess("populateEnrolleeData").build());
        }
    }

    private EnrolleePopulationData createNewGovernedEnrollee(EnvironmentName environmentName, EnrolleePopDto popDto, StudyPopulateContext context,
                                                             ProxyPopDto proxyPopDto) {
        String governedUsername = popDto.getLinkedUsername();
        ParticipantUser proxyParticipantUser = participantUserService.findOne(proxyPopDto.getUsername(), environmentName).get();
        PortalParticipantUser portalParticipantUser = portalParticipantUserService.findOne(proxyParticipantUser.getId(), context.getPortalShortcode()).get();
        boolean prevEmailSetting = updateDoNotEmail(portalParticipantUser, true, "createNewGovernedEnrollee");
        // for governed and proxy users we always simulate submissions
        HubResponse<Enrollee> hubResponse = enrollmentService.enrollAsProxy(environmentName, context.getStudyShortcode(), proxyParticipantUser,
                portalParticipantUser, popDto.getPreEnrollmentResponseId(), governedUsername);
        Enrollee proxyEnrollee = hubResponse.getEnrollee();
        proxyEnrollee.setShortcode(proxyPopDto.getShortcode());
        enrolleeService.update(proxyEnrollee);
        hubResponse.setEnrollee(proxyEnrollee);

        Enrollee governedEnrollee = hubResponse.getResponse();
        governedEnrollee.setShortcode(popDto.getShortcode());
        enrolleeService.update(governedEnrollee);
        hubResponse.setResponse(governedEnrollee);

        popDto.setParticipantUserId(governedEnrollee.getParticipantUserId());

        PortalParticipantUser governedPPUser = portalParticipantUserService.findForEnrollee(governedEnrollee);
        // restore the proxy's email settings
        updateDoNotEmail(portalParticipantUser, prevEmailSetting, "createNewGovernedEnrollee");

        return new EnrolleePopulationData(popDto, governedEnrollee, governedPPUser, hubResponse.getTasks(), proxyEnrollee, prevEmailSetting);
    }

    @Transactional
    public void bulkPopulateEnrollees(String portalShortcode, EnvironmentName envName, String studyShortcode, List<String> usernamesToLink) {
        StudyPopulateContext context = new StudyPopulateContext("portals/" + portalShortcode + "/studies/" + studyShortcode + "/enrollees/seed.json", portalShortcode, studyShortcode, envName, new HashMap<>(), false, null);

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

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    protected class EnrolleePopulationData {
        EnrolleePopDto enrolleePopDto;
        Enrollee enrollee;
        PortalParticipantUser ppUser;
        List<ParticipantTask> tasks;
        Enrollee proxyEnrollee;
        boolean doNotEmailSetting; // the original doNotEmail setting, stored here so it can be restored post-populate
    }

    @Transactional
    public Enrollee populateFromType(EnrolleePopulateType popType, String username, StudyPopulateContext popContext) {
        // this operation always creates a new ParticipantUser
        if (StringUtils.isBlank(username)) {
            username = PopulateUtils.generateEmail(popType);
        }
        ParticipantUserPopDto userPopDto = ParticipantUserPopDto.builder()
                .username(username).build();
        PortalParticipantUserPopDto portalParticipantUserPopDto = PortalParticipantUserPopDto.builder()
                .participantUser(userPopDto)
                .profile(Profile.builder()
                        .givenName("Synth")
                        .familyName(popType.name() + "-Family")
                        .contactEmail(username)
                        .build())
                .build();
        try {
            PortalParticipantUser ppUser = portalParticipantUserPopulator.populateFromDto(portalParticipantUserPopDto, popContext, true);
            EnrolleePopDto enrolleePopDto = EnrolleePopDto.builder()
                    .linkedUsername(username)
                    .simulateSubmissions(true)
                    .shortcode(shortcodeService.generateShortcode("", enrolleeService::findOneByShortcode))
                    .build();
            Enrollee enrollee = createNew(enrolleePopDto, popContext, true);
            ParticipantUser user = participantUserService.find(enrollee.getParticipantUserId()).orElseThrow();
            if (popType.equals(EnrolleePopulateType.CONSENTED) || popType.equals(EnrolleePopulateType.ALL_COMPLETE)) {
                autoConsent(enrollee, user, ppUser, popType, popContext);
            }
            if (popType.equals(EnrolleePopulateType.ALL_COMPLETE)) {
                autoCompleteAll(enrollee, user,  ppUser, popType, popContext);
            }
            return enrollee;
        } catch (IOException e) {
            throw new InternalServerException("Unable to populate enrollee due to error: " + e.getMessage());
        }
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
        List<AnswerPopDto> answers = questionDefs.stream().map(questionDef -> {
            AnswerPopDto answer = AnswerPopDto.builder()
                    .questionStableId(questionDef.getQuestionStableId())
                    .stringValue("blah")
                    .build();
            return answer;
        }).toList();
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

    private final EnrolleeService enrolleeService;
    private final StudyEnvironmentService studyEnvironmentService;
    private final ParticipantUserService participantUserService;
    private final PortalParticipantUserService portalParticipantUserService;
    private final PreEnrollmentResponseDao preEnrollmentResponseDao;
    private final SurveyService surveyService;
    private final SurveyResponseService surveyResponseService;
    private final ParticipantTaskService participantTaskService;
    private final TriggerService triggerService;
    private final NotificationService notificationService;
    private final AnswerProcessingService answerProcessingService;
    private final EnrollmentService enrollmentService;
    private final ProfileService profileService;
    private final WithdrawnEnrolleeService withdrawnEnrolleeService;
    private final TimeShiftPopulateDao timeShiftPopulateDao;
    private final KitRequestService kitRequestService;
    private final KitTypeDao kitTypeDao;
    private final AdminUserDao adminUserDao;
    private final ParticipantNotePopulator participantNotePopulator;
    private final ParticipantUserPopulateDao participantUserPopulateDao;
    private final PortalParticipantUserPopulator portalParticipantUserPopulator;
    private final ObjectMapper objectMapper;
    private final PortalService portalService;
    private final SurveyQuestionDefinitionDao surveyQuestionDefinitionDao;
    private final ShortcodeService shortcodeService;
}

