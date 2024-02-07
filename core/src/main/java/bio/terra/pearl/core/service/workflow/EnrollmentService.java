package bio.terra.pearl.core.service.workflow;

import bio.terra.pearl.core.dao.survey.PreEnrollmentResponseDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import bio.terra.pearl.core.model.survey.ParsedPreEnrollResponse;
import bio.terra.pearl.core.model.survey.PreEnrollmentResponse;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.workflow.HubResponse;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.participant.EnrolleeRelationService;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.rule.EnrolleeRuleService;
import bio.terra.pearl.core.service.study.StudyEnvironmentConfigService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.study.exception.StudyEnvConfigMissing;
import bio.terra.pearl.core.service.survey.SurveyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class EnrollmentService {
    private static final String QUALIFIED_STABLE_ID = "qualified";
    private SurveyService surveyService;
    private PreEnrollmentResponseDao preEnrollmentResponseDao;
    private StudyEnvironmentService studyEnvironmentService;
    private StudyEnvironmentConfigService studyEnvironmentConfigService;
    private PortalParticipantUserService portalParticipantUserService;
    private EnrolleeService enrolleeService;
    private PortalService portalService;
    private EnrolleeRelationService enrolleeRelationService;
    private RegistrationService registrationService;
    private EventService eventService;
    private ObjectMapper objectMapper;

    public EnrollmentService(SurveyService surveyService, PreEnrollmentResponseDao preEnrollmentResponseDao,
                             StudyEnvironmentService studyEnvironmentService,
                             PortalParticipantUserService portalParticipantUserService,
                             EnrolleeRuleService enrolleeRuleService,
                             StudyEnvironmentConfigService studyEnvironmentConfigService,
                             EnrolleeService enrolleeService,
                             EventService eventService, ObjectMapper objectMapper,
                             RegistrationService registrationService,
                             PortalService portalService,
                             EnrolleeRelationService enrolleeRelationService) {
        this.surveyService = surveyService;
        this.preEnrollmentResponseDao = preEnrollmentResponseDao;
        this.studyEnvironmentService = studyEnvironmentService;
        this.portalParticipantUserService = portalParticipantUserService;
        this.studyEnvironmentConfigService = studyEnvironmentConfigService;
        this.eventService = eventService;
        this.enrolleeService = enrolleeService;
        this.objectMapper = objectMapper;
        this.registrationService = registrationService;
        this.portalService = portalService;
        this.enrolleeRelationService = enrolleeRelationService;
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
            ParsedPreEnrollResponse parsedResponse) throws JsonProcessingException {
        Survey survey = surveyService.findByStableId(surveyStableId, surveyVersion).get();

        PreEnrollmentResponse response = PreEnrollmentResponse.builder()
                .surveyId(survey.getId())
                .qualified(parsedResponse.isQualified())
                .fullData(objectMapper.writeValueAsString(parsedResponse.getAnswers()))
                .studyEnvironmentId(studyEnvironmentId).build();
        return preEnrollmentResponseDao.create(response);
    }

    @Transactional
    public HubResponse<Enrollee> enroll(String portalShortcode, EnvironmentName envName, String studyShortcode, ParticipantUser user, PortalParticipantUser ppUser, UUID preEnrollResponseId, boolean isProxy) {
        log.info("creating enrollee for user {}, study {}", user.getId(), studyShortcode);
        StudyEnvironment studyEnv = studyEnvironmentService.findByStudy(studyShortcode, envName).orElseThrow(() -> new NotFoundException("Study environment %s %s not found".formatted(studyShortcode, envName)));
        StudyEnvironmentConfig studyEnvConfig = studyEnvironmentConfigService.find(studyEnv.getStudyEnvironmentConfigId())
                .orElseThrow(StudyEnvConfigMissing::new);
        if (!studyEnvConfig.isAcceptingEnrollment()) {
            throw new IllegalArgumentException("study %s is not accepting enrollment".formatted(studyShortcode));
        }
        PreEnrollmentResponse preEnrollResponse = validatePreEnrollResponse(studyEnv, preEnrollResponseId, user.getId());
        Enrollee enrollee;
        if (isProxy) {
            enrollee = enrollGovernedUser( portalShortcode, user, studyEnv, preEnrollResponseId);
        }
        else {
            enrollee = Enrollee.builder()
                    .studyEnvironmentId(studyEnv.getId())
                    .participantUserId(user.getId())
                    .profileId(ppUser.getProfileId())
                    .preEnrollmentResponseId(preEnrollResponseId)
                    .build();
            enrollee = enrolleeService.create(enrollee);

        }

        if (preEnrollResponse != null) {
            preEnrollResponse.setCreatingParticipantUserId(user.getId());
            preEnrollResponse.setPortalParticipantUserId(ppUser.getId());
            preEnrollmentResponseDao.update(preEnrollResponse);
        }
        EnrolleeEvent event = eventService.publishEnrolleeCreationEvent(enrollee, ppUser);
        log.info("Enrollee created: user {}, study {}, shortcode {}, {} tasks added",
                user.getId(), studyShortcode, enrollee.getShortcode(), enrollee.getParticipantTasks().size());
        HubResponse hubResponse = eventService.buildHubResponse(event, enrollee);
        return hubResponse;
    }

    @Transactional
    protected Enrollee enrollGovernedUser(String portalShortcode, ParticipantUser proxyUser, StudyEnvironment studyEnv,
                                          UUID preEnrollResponseId) {
        // Before this, at time of registration we have registered the proxy as a participant user, but now we need to both register and enroll the child they are enrolling
        RegistrationService.RegistrationResult registrationResult = registrationService.registerGovernedUser(portalShortcode, proxyUser);
        Portal portal = portalService.findOneByShortcode(portalShortcode).orElseThrow(() -> new NotFoundException("portal %s not found".formatted(portalShortcode)));
        log.info("Governed user {} registered", registrationResult.participantUser().getId());
        ParticipantUser governedUser = registrationResult.participantUser();
        PortalParticipantUser governedPpUser = registrationResult.portalParticipantUser();
        Enrollee enrollee = Enrollee.builder()
                .studyEnvironmentId(studyEnv.getId())
                .participantUserId(governedUser.getId())
                .profileId(governedPpUser.getProfileId())
                .preEnrollmentResponseId(preEnrollResponseId)
                .build();
        enrollee = enrolleeService.create(enrollee);

        return enrolleeRelationService.newGovernedEnrolleeCreationRecord(enrollee, portal, proxyUser);

    }

    private PreEnrollmentResponse validatePreEnrollResponse(StudyEnvironment studyEnv,
                                                            UUID preEnrollResponseId, UUID participantUserId) {
        if (studyEnv.getPreEnrollSurveyId() == null) {
            // no pre-enroll required
            return null;
        }
        if (preEnrollResponseId == null) {
            log.warn("Could not match enrollee to pre-enrollment survey results; user {}", participantUserId);
            return null;
        }
        PreEnrollmentResponse response = preEnrollmentResponseDao.find(preEnrollResponseId).get();
        if (!response.isQualified()) {
            throw new IllegalArgumentException("pre-enrollment survey did not meet criteria");
        }
        if (response.getCreatingParticipantUserId() != null &&
                !response.getCreatingParticipantUserId().equals(participantUserId)) {
            throw new IllegalArgumentException("user does not match preEnrollment response user");
        }
        return response;
    }
}
