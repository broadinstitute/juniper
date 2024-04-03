package bio.terra.pearl.core.service.workflow;

import bio.terra.pearl.core.dao.survey.AnswerMappingDao;
import bio.terra.pearl.core.dao.survey.PreEnrollmentResponseDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.EnrolleeRelation;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.participant.RelationshipType;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import bio.terra.pearl.core.model.survey.AnswerMapping;
import bio.terra.pearl.core.model.survey.AnswerMappingTargetType;
import bio.terra.pearl.core.model.survey.ParsedPreEnrollResponse;
import bio.terra.pearl.core.model.survey.PreEnrollmentResponse;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.workflow.HubResponse;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.participant.EnrolleeRelationService;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.ParticipantUserService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.participant.RandomUtilService;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.rule.EnrolleeContextService;
import bio.terra.pearl.core.service.study.StudyEnvironmentConfigService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.study.exception.StudyEnvConfigMissing;
import bio.terra.pearl.core.service.survey.SurveyParseUtils;
import bio.terra.pearl.core.service.survey.SurveyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class EnrollmentService {
    private static final String QUALIFIED_STABLE_ID = "qualified";
    private SurveyService surveyService;
    private PreEnrollmentResponseDao preEnrollmentResponseDao;
    private StudyEnvironmentService studyEnvironmentService;
    private StudyEnvironmentConfigService studyEnvironmentConfigService;
    private PortalParticipantUserService portalParticipantUserService;
    private ParticipantUserService participantUserService;
    private EnrolleeService enrolleeService;
    private PortalService portalService;
    private EnrolleeRelationService enrolleeRelationService;
    private RegistrationService registrationService;
    private EventService eventService;
    private ObjectMapper objectMapper;
    private RandomUtilService randomUtilService;
    private AnswerMappingDao answerMappingDao;

    public EnrollmentService(SurveyService surveyService, PreEnrollmentResponseDao preEnrollmentResponseDao,
                             StudyEnvironmentService studyEnvironmentService,
                             PortalParticipantUserService portalParticipantUserService,
                             EnrolleeContextService enrolleeContextService,
                             StudyEnvironmentConfigService studyEnvironmentConfigService,
                             EnrolleeService enrolleeService,
                             EventService eventService, ObjectMapper objectMapper,
                             RegistrationService registrationService,
                             PortalService portalService,
                             EnrolleeRelationService enrolleeRelationService,
                             RandomUtilService randomUtilService,
                             ParticipantUserService participantUserService,
                             AnswerMappingDao answerMappingDao) {
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
        this.randomUtilService = randomUtilService;
        this.participantUserService = participantUserService;
        this.answerMappingDao = answerMappingDao;
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
            UUID portalId,
            UUID studyEnvironmentId,
            String surveyStableId,
            Integer surveyVersion,
            ParsedPreEnrollResponse parsedResponse) throws JsonProcessingException {
        Survey survey = surveyService.findByStableId(surveyStableId, surveyVersion, portalId).get();

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
     * This method uses the preEnrollResponseId to find the preEnroll Survey for the study and also the user's responses.
     * It then checks if there is a PROXY answer mapping for any of the questions in the preEnroll survey.
     * If there is one, it will check the user's response to the question in the PreEnrollment Response.
     * If the user's response is true, it will return true, otherwise it returns false;
     */

    protected boolean isProxyEnrollment(UUID preEnrollResponseId) {
        PreEnrollmentResponse preEnrollResponse = preEnrollmentResponseDao.find(preEnrollResponseId).orElse(null);
        if (preEnrollResponse == null || !preEnrollResponse.isQualified()) {
            return false;
        }
        UUID surveyId = preEnrollResponse.getSurveyId();
        Optional<AnswerMapping> answerMappingForProxyOpt =
                answerMappingDao.findByTargetField(surveyId, AnswerMappingTargetType.PROXY, "isProxy");
        if (answerMappingForProxyOpt.isEmpty()) {
            return false;
        }
        String questionStableId = answerMappingForProxyOpt.get().getQuestionStableId();
        Boolean proxyAnswer =
                SurveyParseUtils.getAnswerByStableId(preEnrollResponse.getFullData(), questionStableId, Boolean.class, objectMapper,
                        "stringValue");
        return proxyAnswer != null && proxyAnswer;
    }

    /**
     * Will check if proxy is already enrolled, if not will create an enrollee for the proxy. Will then create a new
     * governed user that is registered.  The HubResponse returned will include the governedEnrollee as the "response",
     * and the enrollee corresponding to the proxy user as the "enrollee"
     */
    @Transactional
    public HubResponse<Enrollee> enrollAsProxy(EnvironmentName envName, String studyShortcode, ParticipantUser proxyUser,
                                               PortalParticipantUser ppUser, UUID preEnrollResponseId) {
        String governedUserName = registrationService.getGovernedUsername(proxyUser.getUsername(), proxyUser.getEnvironmentName());
        return this.enrollAsProxy(envName, studyShortcode, proxyUser, ppUser, preEnrollResponseId, governedUserName);
    }

    @Transactional
    public HubResponse<Enrollee> enrollAsProxy(EnvironmentName envName, String studyShortcode, ParticipantUser proxyUser,
                                               PortalParticipantUser ppUser, UUID preEnrollResponseId, String governedUsername) {
        Enrollee proxyEnrollee = enrolleeService.findByParticipantUserIdAndStudyEnv(proxyUser.getId(), studyShortcode, envName)
                .orElseGet(() -> this.enroll(envName, studyShortcode, proxyUser, ppUser, null, false).getEnrollee());
        HubResponse<Enrollee> governedResponse =
                this.enrollGovernedUser(envName, studyShortcode, proxyEnrollee, proxyUser, ppUser, preEnrollResponseId, governedUsername);
        governedResponse.setEnrollee(proxyEnrollee);
        return governedResponse;
    }

    @Transactional
    public HubResponse<Enrollee> enrollGovernedUser(EnvironmentName envName, String studyShortcode, Enrollee governingEnrollee,
                                                    ParticipantUser proxyUser, PortalParticipantUser proxyPpUser,
                                                    UUID preEnrollResponseId, String governedUserName) {
        ParticipantUser governedUserParticipantUserOpt = participantUserService.findOne(governedUserName, envName).orElse(null);
        // Before this, at time of registration we have registered the proxy as a participant user, but now we need to both register and enroll the child they are enrolling
        RegistrationService.RegistrationResult registrationResult =
                registrationService.registerGovernedUser(proxyUser, proxyPpUser, governedUserName, governedUserParticipantUserOpt);

        HubResponse<Enrollee> hubResponse =
                this.enroll(envName, studyShortcode, registrationResult.participantUser(), registrationResult.portalParticipantUser(),
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

    /**
     * This method calls the isProxyEnrollment method using the preEnrollResponseId to determine if the user is enrolling
     * as a proxy. If they are, it will call the enrollAsProxy method, otherwise it will call the enroll method.
     */
    public HubResponse enroll(EnvironmentName environmentName, String studyShortcode, ParticipantUser user,
                              PortalParticipantUser portalParticipantUser, UUID preEnrollResponseId) {
        if (preEnrollResponseId != null && isProxyEnrollment(preEnrollResponseId)) {
            return enrollAsProxy(environmentName, studyShortcode, user, portalParticipantUser, preEnrollResponseId);
        }
        Optional<Enrollee> proxyEnrolleeOpt =
                enrolleeService.findByParticipantUserIdAndStudyEnv(user.getId(), studyShortcode, environmentName);

        if (proxyEnrolleeOpt.isPresent()) {
            if (!proxyEnrolleeOpt.get().isSubject()) {
               return  enrollProxyInStudy(proxyEnrolleeOpt.get(), environmentName, studyShortcode, user, portalParticipantUser,
                        preEnrollResponseId);
            } else {
                throw new IllegalArgumentException("User is already enrolled in study");
            }
        }
        return enroll(environmentName, studyShortcode, user, portalParticipantUser, preEnrollResponseId, true);
    }

    private HubResponse enrollProxyInStudy(Enrollee proxyEnrollee, EnvironmentName envName, String studyShortcode, ParticipantUser user,
                                           PortalParticipantUser portalParticipantUser, UUID preEnrollResponseId) {
        StudyEnvironment studyEnv = studyEnvironmentService.findByStudy(studyShortcode, envName)
                .orElseThrow(() -> new NotFoundException("Study environment %s %s not found".formatted(studyShortcode, envName)));
        PreEnrollmentResponse preEnrollResponse = validatePreEnrollResponse(studyEnv, preEnrollResponseId, user.getId(), true);
        proxyEnrollee.setSubject(true);
        proxyEnrollee.setPreEnrollmentResponseId(preEnrollResponseId);
        enrolleeService.update(proxyEnrollee);
        if (preEnrollResponse != null) {
            preEnrollResponse.setCreatingParticipantUserId(user.getId());
            preEnrollResponse.setPortalParticipantUserId(portalParticipantUser.getId());
            preEnrollmentResponseDao.update(preEnrollResponse);
        }
        EnrolleeEvent event = eventService.publishEnrolleeCreationEvent(proxyEnrollee, portalParticipantUser);
        log.info("Enrollee created: user {}, study {}, shortcode {}, {} tasks added",
                user.getId(), studyShortcode, proxyEnrollee.getShortcode(), proxyEnrollee.getParticipantTasks().size());
        HubResponse hubResponse = eventService.buildHubResponse(event, proxyEnrollee);
        return hubResponse;
    }
}
