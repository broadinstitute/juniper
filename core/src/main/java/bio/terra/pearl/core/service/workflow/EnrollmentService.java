package bio.terra.pearl.core.service.workflow;

import bio.terra.pearl.core.dao.survey.AnswerMappingDao;
import bio.terra.pearl.core.dao.survey.PreEnrollmentResponseDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.audit.ResponsibleEntity;
import bio.terra.pearl.core.model.participant.*;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import bio.terra.pearl.core.model.survey.*;
import bio.terra.pearl.core.model.workflow.HubResponse;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.participant.EnrolleeRelationService;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.ParticipantUserService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.study.StudyEnvironmentConfigService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.study.exception.StudyEnvConfigMissing;
import bio.terra.pearl.core.service.survey.AnswerProcessingService;
import bio.terra.pearl.core.service.survey.SurveyParseUtils;
import bio.terra.pearl.core.service.survey.SurveyResponseService;
import bio.terra.pearl.core.service.survey.SurveyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import liquibase.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class EnrollmentService {
    private static final String QUALIFIED_STABLE_ID = "qualified";
    private final SurveyService surveyService;
    private final PreEnrollmentResponseDao preEnrollmentResponseDao;
    private final StudyEnvironmentService studyEnvironmentService;
    private final StudyEnvironmentConfigService studyEnvironmentConfigService;
    private final PortalParticipantUserService portalParticipantUserService;
    private final ParticipantUserService participantUserService;
    private final EnrolleeService enrolleeService;
    private final EnrolleeRelationService enrolleeRelationService;
    private final RegistrationService registrationService;
    private final EventService eventService;
    private final ObjectMapper objectMapper;
    private final AnswerMappingDao answerMappingDao;
    private final SurveyResponseService surveyResponseService;
    private final AnswerProcessingService answerProcessingService;

    public EnrollmentService(SurveyService surveyService,
                             PreEnrollmentResponseDao preEnrollmentResponseDao,
                             StudyEnvironmentService studyEnvironmentService,
                             PortalParticipantUserService portalParticipantUserService,
                             StudyEnvironmentConfigService studyEnvironmentConfigService,
                             EnrolleeService enrolleeService,
                             EventService eventService, ObjectMapper objectMapper,
                             RegistrationService registrationService,
                             EnrolleeRelationService enrolleeRelationService,
                             ParticipantUserService participantUserService,
                             AnswerMappingDao answerMappingDao,
                             SurveyResponseService surveyResponseService,
                             AnswerProcessingService answerProcessingService) {
        this.surveyService = surveyService;
        this.preEnrollmentResponseDao = preEnrollmentResponseDao;
        this.studyEnvironmentService = studyEnvironmentService;
        this.portalParticipantUserService = portalParticipantUserService;
        this.studyEnvironmentConfigService = studyEnvironmentConfigService;
        this.eventService = eventService;
        this.enrolleeService = enrolleeService;
        this.objectMapper = objectMapper;
        this.registrationService = registrationService;
        this.enrolleeRelationService = enrolleeRelationService;
        this.participantUserService = participantUserService;
        this.answerMappingDao = answerMappingDao;
        this.surveyResponseService = surveyResponseService;
        this.answerProcessingService = answerProcessingService;
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
    public HubResponse<Enrollee> enroll(PortalParticipantUser operator,
                                        EnvironmentName envName,
                                        String studyShortcode,
                                        ParticipantUser user,
                                        PortalParticipantUser ppUser,
                                        UUID preEnrollResponseId,
                                        boolean isSubject) {
        log.info("creating enrollee for user {}, study {}", user.getId(), studyShortcode);
        StudyEnvironment studyEnv = studyEnvironmentService.findByStudy(studyShortcode, envName)
                .orElseThrow(() -> new NotFoundException("Study environment %s %s not found".formatted(studyShortcode, envName)));
        StudyEnvironmentConfig studyEnvConfig = studyEnvironmentConfigService.find(studyEnv.getStudyEnvironmentConfigId())
                .orElseThrow(StudyEnvConfigMissing::new);
        if (!studyEnvConfig.isAcceptingEnrollment()) {
            throw new IllegalArgumentException("study %s is not accepting enrollment".formatted(studyShortcode));
        }
        PreEnrollmentResponse preEnrollResponse = validatePreEnrollResponse(operator, studyEnv, preEnrollResponseId, user.getId(), isSubject);

        // if the user is signed up, but not a subject, we can just return the existing enrollee,
        // otherwise create a new one for them
        Enrollee enrollee = findOrCreateEnrolleeForEnrollment(user, ppUser, studyEnv, studyShortcode, preEnrollResponseId, isSubject);

        if (preEnrollResponse != null) {
            preEnrollResponse.setCreatingParticipantUserId(user.getId());
            preEnrollResponse.setPortalParticipantUserId(ppUser.getId());
            preEnrollmentResponseDao.update(preEnrollResponse);

            // backfill the enrollee with the pre-enrollment response data
            this.backfillPreEnrollResponse(operator, enrollee, preEnrollResponse);
        }

        EnrolleeEvent event = eventService.publishEnrolleeCreationEvent(enrollee, ppUser);
        log.info("Enrollee created: user {}, study {}, shortcode {}, {} tasks added",
                user.getId(), studyShortcode, enrollee.getShortcode(), enrollee.getParticipantTasks().size());
        HubResponse hubResponse = eventService.buildHubResponse(event, enrollee);
        return hubResponse;
    }

    private Enrollee findOrCreateEnrolleeForEnrollment(ParticipantUser user, PortalParticipantUser ppUser, StudyEnvironment studyEnv, String studyShortcode, UUID preEnrollResponseId, boolean isSubjectEnrollment) {
        return enrolleeService
                .findByParticipantUserIdAndStudyEnv(user.getId(), studyShortcode, studyEnv.getEnvironmentName())
                .filter(e -> {
                    // if the user isn't a subject, but is now, update the enrollee
                    if (isSubjectEnrollment && !e.isSubject()) {
                        e.setSubject(true);
                        e.setPreEnrollmentResponseId(preEnrollResponseId);
                        enrolleeService.update(e);
                        return true;
                    }

                    // all other cases are duplicate enrollment, which is an error
                    throw new IllegalArgumentException("user already exists");
                })
                .orElseGet(() -> {
                    Enrollee newEnrollee;
                    newEnrollee = Enrollee.builder()
                            .studyEnvironmentId(studyEnv.getId())
                            .participantUserId(user.getId())
                            .profileId(ppUser.getProfileId())
                            .preEnrollmentResponseId(preEnrollResponseId)
                            .subject(isSubjectEnrollment)
                            .build();
                    return enrolleeService.create(newEnrollee);
                });
    }

    private void backfillPreEnrollResponse(PortalParticipantUser operator, Enrollee enrollee, PreEnrollmentResponse preEnrollResponse) {
        if (Objects.isNull(preEnrollResponse) || StringUtil.isEmpty(preEnrollResponse.getFullData())) {
            return;
        }

        List<Answer> answers;
        try {
            answers = objectMapper.readValue(preEnrollResponse.getFullData(), new TypeReference<List<Answer>>() {
            });
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Invalid pre-enrollment response data: " + e.getMessage());
        }

        answers.forEach(Answer::inferTypeIfMissing);

        PortalParticipantUser ppUser = portalParticipantUserService.findForEnrollee(enrollee);
        ParticipantUser participantUser = participantUserService.find(ppUser.getParticipantUserId())
                .orElseThrow(() -> new NotFoundException("Participant user not found"));

        SurveyResponse surveyResponse =
                SurveyResponse
                        .builder()
                        .surveyId(preEnrollResponse.getSurveyId())
                        .answers(answers)
                        .enrolleeId(enrollee.getId())
                        .complete(true)
                        .creatingParticipantUserId(enrollee.getParticipantUserId())
                        .build();

        surveyResponseService.create(surveyResponse);

        DataAuditInfo auditInfo = DataAuditInfo.builder()
                .responsibleUserId(enrollee.getParticipantUserId())
                .enrolleeId(enrollee.getId())
                .surveyId(preEnrollResponse.getSurveyId())
                .portalParticipantUserId(ppUser.getId())
                .build();
        
        // process any answers that need to be propagated elsewhere to the data model
        answerProcessingService.processAllAnswerMappings(
                enrollee,
                answers,
                answerMappingDao.findBySurveyId(preEnrollResponse.getSurveyId()),
                operator,
                new ResponsibleEntity(participantUser),
                auditInfo);
    }

    /**
     * This method uses the preEnrollResponseId to find the preEnroll Survey for the study and also the user's responses.
     * It then checks if there is a PROXY answer mapping for any of the questions in the preEnroll survey.
     * If there is one, it will check the user's response to the question in the PreEnrollment Response.
     * If the user's response is true, it will return true, otherwise it returns false;
     */
    protected boolean isProxyEnrollment(EnvironmentName envName, String studyShortcode, UUID preEnrollResponseId) {
        // in the future, we might want to consider consolidating our studyEnv/studyEnvConfig lookups
        // as it happens more than once per enroll
        StudyEnvironment studyEnv = studyEnvironmentService.findByStudy(studyShortcode, envName)
                .orElseThrow(() -> new NotFoundException("Study environment %s %s not found".formatted(studyShortcode, envName)));
        StudyEnvironmentConfig studyEnvConfig = studyEnvironmentConfigService.find(studyEnv.getStudyEnvironmentConfigId())
                .orElseThrow(StudyEnvConfigMissing::new);

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
        Boolean proxyAnswer = SurveyParseUtils.getAnswerByStableId(preEnrollResponse.getFullData(), questionStableId, Boolean.class, objectMapper, null);
        if (proxyAnswer == null) {
            return false;
        }

        if (proxyAnswer) {
            if (studyEnvConfig.isAcceptingProxyEnrollment()) {
                return true;
            } else {
                throw new IllegalArgumentException("Proxy enrollment not allowed for study %s".formatted(studyShortcode));
            }
        }
        return false;
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
                .orElseGet(() -> this.enroll(ppUser, envName, studyShortcode, proxyUser, ppUser, null, false).getEnrollee());
        HubResponse<Enrollee> governedResponse =
                this.registerAndEnrollGovernedUser(envName, studyShortcode, proxyEnrollee, proxyUser, ppUser, preEnrollResponseId, governedUsername);
        governedResponse.setEnrollee(proxyEnrollee);

        return governedResponse;
    }

    @Transactional
    public HubResponse<Enrollee> registerAndEnrollGovernedUser(EnvironmentName envName,
                                                               String studyShortcode,
                                                               Enrollee governingEnrollee,
                                                               ParticipantUser proxyUser,
                                                               PortalParticipantUser proxyPpUser,
                                                               UUID preEnrollResponseId,
                                                               String governedUserName) {
        ParticipantUser governedUserParticipantUserOpt = participantUserService.findOne(governedUserName, envName).orElse(null);
        // Before this, at time of registration we have registered the proxy as a participant user, but now we need to both register and enroll the child they are enrolling
        RegistrationService.RegistrationResult registrationResult =
                registrationService.registerGovernedUser(proxyUser, proxyPpUser, governedUserName, governedUserParticipantUserOpt);

        return enrollGovernedUser(
                envName,
                studyShortcode,
                governingEnrollee,
                proxyUser,
                proxyPpUser,
                registrationResult.participantUser(),
                registrationResult.portalParticipantUser(),
                preEnrollResponseId);
    }

    public HubResponse enrollGovernedUser(EnvironmentName envName,
                                          String studyShortcode,
                                          Enrollee governingEnrollee,
                                          ParticipantUser proxyUser,
                                          PortalParticipantUser proxyPpUser,
                                          ParticipantUser governedUser,
                                          PortalParticipantUser governedPpUser,
                                          UUID preEnrollResponseId) {
        HubResponse<Enrollee> hubResponse =
                this.enroll(proxyPpUser,
                        envName,
                        studyShortcode,
                        governedUser,
                        governedPpUser,
                        preEnrollResponseId,
                        true);

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

    private PreEnrollmentResponse validatePreEnrollResponse(PortalParticipantUser operator,
                                                            StudyEnvironment studyEnv,
                                                            UUID preEnrollResponseId,
                                                            UUID participantUserId,
                                                            boolean isSubject) {
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

        if (response.getCreatingParticipantUserId() != null
                && !response.getCreatingParticipantUserId().equals(participantUserId)
                && !response.getCreatingParticipantUserId().equals(operator.getParticipantUserId())) {
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
        if (preEnrollResponseId != null && isProxyEnrollment(environmentName, studyShortcode, preEnrollResponseId)) {
            return enrollAsProxy(environmentName, studyShortcode, user, portalParticipantUser, preEnrollResponseId);
        }
        return enroll(portalParticipantUser, environmentName, studyShortcode, user, portalParticipantUser, preEnrollResponseId, true);
    }
}
