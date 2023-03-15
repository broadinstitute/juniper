package bio.terra.pearl.core.service.workflow;

import bio.terra.pearl.core.dao.survey.PreEnrollmentResponseDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.ParsedPreEnrollResponse;
import bio.terra.pearl.core.model.survey.PreEnrollmentResponse;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.workflow.HubResponse;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.rule.EnrolleeRuleService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.survey.SurveyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnrollmentService {
    private static final String QUALIFIED_STABLE_ID = "qualified";
    private static final Logger logger = LoggerFactory.getLogger(EnrollmentService.class);
    private SurveyService surveyService;
    private PreEnrollmentResponseDao preEnrollmentResponseDao;
    private StudyEnvironmentService studyEnvironmentService;
    private PortalParticipantUserService portalParticipantUserService;
    private EnrolleeService enrolleeService;
    private EventService eventService;
    private ObjectMapper objectMapper;

    public EnrollmentService(SurveyService surveyService, PreEnrollmentResponseDao preEnrollmentResponseDao,
                             StudyEnvironmentService studyEnvironmentService,
                             PortalParticipantUserService portalParticipantUserService,
                             EnrolleeRuleService enrolleeRuleService,
                             EnrolleeService enrolleeService, EventService eventService, ObjectMapper objectMapper) {
        this.surveyService = surveyService;
        this.preEnrollmentResponseDao = preEnrollmentResponseDao;
        this.studyEnvironmentService = studyEnvironmentService;
        this.portalParticipantUserService = portalParticipantUserService;
        this.eventService = eventService;
        this.enrolleeService = enrolleeService;
        this.objectMapper = objectMapper;
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
                .fullData(objectMapper.writeValueAsString(parsedResponse.getParsedData()))
                .studyEnvironmentId(studyEnvironmentId).build();
        return preEnrollmentResponseDao.create(response);
    }

    public HubResponse<Enrollee> enroll(ParticipantUser user, PortalParticipantUser ppUser, EnvironmentName envName,
                                            String studyShortcode, UUID preEnrollResponseId) {
        logger.info("creating enrollee for user {}, study {}", user.getId(), studyShortcode);
        StudyEnvironment studyEnv = studyEnvironmentService.findByStudy(studyShortcode, envName).get();

        PreEnrollmentResponse preEnrollResponse = validatePreEnrollResponse(studyEnv, preEnrollResponseId, user.getId());

        Enrollee enrollee = Enrollee.builder()
                .studyEnvironmentId(studyEnv.getId())
                .participantUserId(user.getId())
                .profileId(ppUser.getProfileId())
                .preEnrollmentResponseId(preEnrollResponseId)
                .build();
        enrollee = enrolleeService.create(enrollee);

        if (preEnrollResponse != null) {
            preEnrollResponse.setCreatingParticipantUserId(user.getId());
            preEnrollResponse.setPortalParticipantUserId(ppUser.getId());
            preEnrollmentResponseDao.update(preEnrollResponse);
        }
        eventService.publishEnrolleeCreationEvent(enrollee, ppUser);
        logger.info("Enrollee created: user {}, study {}, shortcode {}, {} tasks added",
                user.getId(), studyShortcode, enrollee.getShortcode(), enrollee.getParticipantTasks().size());
        HubResponse hubResponse = HubResponse.builder()
                .enrollee(enrollee)
                .response(enrollee)
                .tasks(enrollee.getParticipantTasks().stream().toList())
                .build();
        return hubResponse;
    }

    private PreEnrollmentResponse validatePreEnrollResponse(StudyEnvironment studyEnv,
                                                            UUID preEnrollResponseId, UUID participantUserId) {
        if (studyEnv.getPreEnrollSurveyId() == null) {
            // no pre-enroll required
            return null;
        }
        if (preEnrollResponseId == null) {
            throw new IllegalArgumentException("pre-enrollment survey is required to enroll in this study");
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
