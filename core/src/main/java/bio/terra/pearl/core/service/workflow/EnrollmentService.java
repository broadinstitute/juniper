package bio.terra.pearl.core.service.workflow;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import bio.terra.pearl.core.dao.survey.PreEnrollmentResponseDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.participant.*;
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

    /**
     * confirms that the preEnrollmentResponse exists and had not yet already been used to create an enrollee
     */
    public Optional<PreEnrollmentResponse> confirmPreEnrollResponse(UUID responseId) {
        if (enrolleeService.findByPreEnrollResponseId(responseId).isPresent()) {
            return Optional.empty();
        }
        return preEnrollmentResponseDao.find(responseId);
    }

    /**
     * Creates a preenrollment survey record for a user who is not signed in
     */
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
    public HubResponse<Enrollee> enroll(EnvironmentName envName, String studyShortcode, ParticipantUser user, PortalParticipantUser ppUser,
                                        UUID preEnrollResponseId, boolean isSubject) {
        log.info("creating enrollee for user {}, study {}", user.getId(), studyShortcode);
        StudyEnvironment studyEnv = studyEnvironmentService.findByStudy(studyShortcode, envName)
                .orElseThrow(() -> new NotFoundException("Study environment %s %s not found".formatted(studyShortcode, envName)));
        StudyEnvironmentConfig studyEnvConfig = studyEnvironmentConfigService.find(studyEnv.getStudyEnvironmentConfigId())
                .orElseThrow(StudyEnvConfigMissing::new);
        if (!studyEnvConfig.isAcceptingEnrollment()) {
            throw new IllegalArgumentException("study %s is not accepting enrollment".formatted(studyShortcode));
        }
        PreEnrollmentResponse preEnrollResponse = validatePreEnrollResponse(studyEnv, preEnrollResponseId, user.getId(), isSubject);
        Enrollee enrollee;

        enrollee = Enrollee.builder()
                .studyEnvironmentId(studyEnv.getId())
                .participantUserId(user.getId())
                .profileId(ppUser.getProfileId())
                .preEnrollmentResponseId(preEnrollResponseId)
                .subject(isSubject)
                .build();
        enrollee = enrolleeService.create(enrollee);

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

    /**
     * Will create an enrollee for both the given user and a new governed user that is registered.  The HubResponse
     * returned will include the governedEnrollee as the "response", and the enrollee corresponding to the proxy user as
     * the "enrollee"
     */
    @Transactional
    public HubResponse<Enrollee> enrollAsProxy(EnvironmentName envName, String studyShortcode, ParticipantUser proxyUser,
                                               PortalParticipantUser ppUser, UUID preEnrollResponseId) {
        Optional<Enrollee> maybeProxyEnrollee =
                enrolleeService.findOneByParticipantUserIdAndStudyEnvironmentId(proxyUser.getId(), studyShortcode, envName);
        if (maybeProxyEnrollee.isEmpty()) {
            maybeProxyEnrollee = Optional.of(enroll(envName, studyShortcode, proxyUser, ppUser, null, false).getEnrollee());
        }
        Enrollee proxyEnrollee = maybeProxyEnrollee.orElseThrow(() -> new NotFoundException(
                "Proxy enrolee with participant user id %s for study %s env %s was not found".formatted(proxyUser.getId(), studyShortcode,
                        envName)));
        HubResponse<Enrollee> governedResponse =
                enrollGovernedUser(envName, studyShortcode, proxyEnrollee, proxyUser, ppUser, preEnrollResponseId);
        governedResponse.setEnrollee(proxyEnrollee);
        return governedResponse;
    }

    @Transactional
    public HubResponse<Enrollee> enrollGovernedUser(EnvironmentName envName, String studyShortcode, Enrollee governingEnrollee,
                                                    ParticipantUser proxyUser, PortalParticipantUser proxyPpUser,
                                                    UUID preEnrollResponseId) {
        // Before this, at time of registration we have registered the proxy as a participant user, but now we need to both register and enroll the child they are enrolling
        RegistrationService.RegistrationResult registrationResult = registrationService.registerGovernedUser(proxyUser, proxyPpUser);

        HubResponse<Enrollee> hubResponse =
                enroll(envName, studyShortcode, registrationResult.participantUser(), registrationResult.portalParticipantUser(),
                        preEnrollResponseId, true);

        EnrolleeRelation relation = EnrolleeRelation.builder()
                .enrolleeId(governingEnrollee.getId())
                .targetEnrolleeId(hubResponse.getEnrollee().getId())
                .relationshipType(RelationshipType.PROXY)
                .beginDate(Instant.now()).build();
        DataAuditInfo auditInfo = DataAuditInfo.builder()
                .enrolleeId(hubResponse.getEnrollee().getId())
                .responsibleUserId(proxyUser.getId()).build();
        enrolleeRelationService.create(relation, auditInfo);
        log.info("Created proxy relationship: {} is proxy for {}", governingEnrollee.getShortcode(),
                hubResponse.getEnrollee().getShortcode());

        return hubResponse;
    }

    private PreEnrollmentResponse validatePreEnrollResponse(StudyEnvironment studyEnv, UUID preEnrollResponseId,
                                                            UUID participantUserId, boolean isSubject) {
        if (studyEnv.getPreEnrollSurveyId() == null) {
            // no pre-enroll required
            return null;
        }
        if (preEnrollResponseId == null) {
            if (isSubject) {
                log.warn("Could not match enrollee to pre-enrollment survey results; user {}", participantUserId);
            }
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
