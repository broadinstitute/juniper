package bio.terra.pearl.core.service.workflow;

import bio.terra.pearl.core.dao.survey.PreEnrollmentResponseDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.PreEnrollmentResponse;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.workflow.HubResponse;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.rule.EnrolleeRuleData;
import bio.terra.pearl.core.service.rule.EnrolleeRuleService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.survey.SurveyService;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnrollmentService {
    private static final Logger logger = LoggerFactory.getLogger(EnrollmentService.class);
    private SurveyService surveyService;
    private PreEnrollmentResponseDao preEnrollmentResponseDao;
    private StudyEnvironmentService studyEnvironmentService;
    private PortalParticipantUserService portalParticipantUserService;
    private EnrolleeRuleService enrolleeRuleService;
    private EnrolleeService enrolleeService;

    public EnrollmentService(SurveyService surveyService, PreEnrollmentResponseDao preEnrollmentResponseDao,
                             StudyEnvironmentService studyEnvironmentService,
                             PortalParticipantUserService portalParticipantUserService,
                             EnrolleeRuleService enrolleeRuleService,
                             EnrolleeService enrolleeService) {
        this.surveyService = surveyService;
        this.preEnrollmentResponseDao = preEnrollmentResponseDao;
        this.studyEnvironmentService = studyEnvironmentService;
        this.portalParticipantUserService = portalParticipantUserService;
        this.enrolleeRuleService = enrolleeRuleService;
        this.enrolleeService = enrolleeService;
    }

    /** confirms that the preEnrollmentResponse exists and had not yet already been used to create an enrollee */
    public Optional<PreEnrollmentResponse> confirmPreEnrollResponse(UUID responseId) {
        if (enrolleeService.findByPreEnrollResponseId(responseId).isPresent()) {
            return Optional.empty();
        }
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
    public HubResponse<Enrollee> enroll(ParticipantUser user, PortalParticipantUser ppUser, EnvironmentName envName,
                                            String studyShortcode, UUID preEnrollResponseId) {
        logger.info("creating enrollee for user {}, study {}", user.getId(), studyShortcode);
        StudyEnvironment studyEnv = studyEnvironmentService.findByStudy(studyShortcode, envName).get();
        Enrollee enrollee = Enrollee.builder()
                .studyEnvironmentId(studyEnv.getId())
                .participantUserId(user.getId())
                .profileId(ppUser.getProfileId())
                .preEnrollmentResponseId(preEnrollResponseId)
                .build();
        enrollee = enrolleeService.create(enrollee);
        if (preEnrollResponseId != null) {
            PreEnrollmentResponse response = preEnrollmentResponseDao.find(preEnrollResponseId).get();
            if (response.getCreatingParticipantUserId() != null &&
                    response.getCreatingParticipantUserId() != user.getId()) {
                throw new IllegalArgumentException("user does not match preEnrollment response user");
            }
            response.setCreatingParticipantUserId(user.getId());
            response.setPortalParticipantUserId(ppUser.getId());
            preEnrollmentResponseDao.update(response);
        }
        EnrolleeRuleData enrolleeRuleData = enrolleeRuleService.fetchData(enrollee);
        EnrolleeCreationEvent enrolleeEvent = EnrolleeCreationEvent.builder()
                .enrollee(enrollee)
                .portalParticipantUser(ppUser)
                .enrolleeRuleData(enrolleeRuleData)
                .build();
        applicationEventPublisher.publishEvent(enrolleeEvent);
        logger.info("created enrollee for user {}, study {} - shortcode {}, {} tasks added",
                user.getId(), studyShortcode, enrollee.getShortcode());
        HubResponse hubResponse = HubResponse.builder()
                .enrollee(enrollee)
                .response(enrollee)
                .tasks(enrollee.getParticipantTasks().stream().toList())
                .build();
        return hubResponse;
    }


    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
}
