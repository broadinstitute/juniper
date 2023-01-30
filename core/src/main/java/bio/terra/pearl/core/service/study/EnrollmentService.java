package bio.terra.pearl.core.service.study;

import bio.terra.pearl.core.dao.survey.PreEnrollmentResponseDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.*;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.PreEnrollmentResponse;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.consent.ConsentTaskService;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.ParticipantTaskService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.participant.ProfileService;
import bio.terra.pearl.core.service.rule.EnrolleeRuleData;
import bio.terra.pearl.core.service.survey.SurveyService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnrollmentService {
    private static final Logger logger = LoggerFactory.getLogger(EnrollmentService.class);
    private SurveyService surveyService;
    private PreEnrollmentResponseDao preEnrollmentResponseDao;
    private StudyEnvironmentService studyEnvironmentService;
    private PortalParticipantUserService portalParticipantUserService;
    private ProfileService profileService;
    private EnrolleeService enrolleeService;
    private ConsentTaskService consentTaskService;
    private ParticipantTaskService participantTaskService;

    public EnrollmentService(SurveyService surveyService, PreEnrollmentResponseDao preEnrollmentResponseDao,
                             StudyEnvironmentService studyEnvironmentService,
                             PortalParticipantUserService portalParticipantUserService,
                             ProfileService profileService, EnrolleeService enrolleeService,
                             ConsentTaskService consentTaskService, ParticipantTaskService participantTaskService) {
        this.surveyService = surveyService;
        this.preEnrollmentResponseDao = preEnrollmentResponseDao;
        this.studyEnvironmentService = studyEnvironmentService;
        this.portalParticipantUserService = portalParticipantUserService;
        this.profileService = profileService;
        this.enrolleeService = enrolleeService;
        this.consentTaskService = consentTaskService;
        this.participantTaskService = participantTaskService;
    }

    public Optional<PreEnrollmentResponse> findPreEnrollResponse(UUID responseId) {
        return preEnrollmentResponseDao.find(responseId);
    }

    /** Creates a preenrollment survey record for a user who is not signed in */
    @Transactional
    public PreEnrollmentResponse createAnonymousPreEnroll(
            UUID studyEnvironmentId,
            String surveyStableId,
            Integer surveyVersion,
            String fullData) {
        Survey survey = surveyService.findByStableId(surveyStableId, surveyVersion).get();
        PreEnrollmentResponse response = PreEnrollmentResponse.builder()
                        .surveyId(survey.getId())
                        .fullData(fullData)
                        .studyEnvironmentId(studyEnvironmentId).build();
        return preEnrollmentResponseDao.create(response);
    }

    @Transactional
    public Enrollee enroll(ParticipantUser user, String portalShortcode, EnvironmentName envName,
                                            String studyShortcode, UUID preEnrollResponseId) {
        logger.info("creating enrollee for user {}, portal {}, study {}", user.getId(), portalShortcode, studyShortcode);
        StudyEnvironment studyEnv = studyEnvironmentService.findByStudy(studyShortcode, envName).get();
        PortalParticipantUser ppUser = portalParticipantUserService.findOne(user.getId(), portalShortcode).get();
        Enrollee enrollee = Enrollee.builder()
                .studyEnvironmentId(studyEnv.getId())
                .participantUserId(user.getId())
                .build();
        enrollee = enrolleeService.create(enrollee);
        if (preEnrollResponseId != null) {
            PreEnrollmentResponse response = preEnrollmentResponseDao.find(preEnrollResponseId).get();
            if (response.getCreatingParticipantUserId() != null &&
                    response.getCreatingParticipantUserId() != user.getId()) {
                throw new IllegalArgumentException("user does not match preEnrollment response user");
            }
            response.setEnrolleeId(enrollee.getId());
            response.setCreatingParticipantUserId(user.getId());
            response.setPortalParticipantUserId(ppUser.getId());
            preEnrollmentResponseDao.update(response);
        }
        EnrolleeCreationEvent enrolleeEvent = EnrolleeCreationEvent.builder()
                .enrollee(enrollee)
                .portalParticipantUser(ppUser)
                .studyEnvironment(studyEnv)
                .build();
        applicationEventPublisher.publishEvent(enrolleeEvent);
        logger.info("created enrollee for user {}, portal {}, study {} - shortcode {}",
                user.getId(), portalShortcode, studyShortcode, enrollee.getShortcode());
        enrollee.getParticipantTasks().addAll(enrolleeEvent.getNewParticipantTasks());
        return enrollee;
    }

    /**
     * on creation of an enrollee, create ParticipantTask objects for their initial tasks
     * This is implemented synchronously to ensure the tasks are created and capable of being returned with the enrollee
     * */
    @EventListener
    public void assignInitialTasks(EnrolleeCreationEvent enrolleeEvent) {
        logger.info("assignInitialTasks for StudyEnrolleeCreationEvent");
        EnrolleeRuleData enrolleeRuleData = EnrolleeRuleData.builder()
                .enrollee(enrolleeEvent.getEnrollee())
                .profile(profileService.find(enrolleeEvent.getPortalParticipantUser().getProfileId()).orElse(null))
                .build();
        List<ParticipantTask> allTasks = participantTaskService.findByEnrolleeId(enrolleeEvent.getEnrollee().getId());
        allTasks.addAll(consentTaskService.buildConsentTasks(enrolleeEvent, allTasks, enrolleeRuleData));
        for (ParticipantTask task : allTasks) {
            ParticipantTask createdTask = participantTaskService.create(task);
            enrolleeEvent.getNewParticipantTasks().add(createdTask);
        }
    }


    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
}
