package bio.terra.pearl.core.service.workflow;

import bio.terra.pearl.core.dao.survey.PreregistrationResponseDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.survey.ParsedPreRegResponse;
import bio.terra.pearl.core.model.survey.PreregistrationResponse;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.participant.ParticipantUserService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.participant.ProfileService;
import bio.terra.pearl.core.service.participant.RandomUtilService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.survey.AnswerProcessingService;
import bio.terra.pearl.core.service.survey.SurveyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
@Slf4j
public class RegistrationService {
    private SurveyService surveyService;
    private RandomUtilService randomUtilService;
    private PortalEnvironmentService portalEnvService;
    private PreregistrationResponseDao preregistrationResponseDao;
    private AnswerProcessingService answerProcessingService;
    private ParticipantUserService participantUserService;
    private PortalParticipantUserService portalParticipantUserService;
    private ProfileService profileService;
    private EventService eventService;
    private ObjectMapper objectMapper;

    private static final String GOVERNED_USERNAME_SUFFIX_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String GOVERNED_USERNAME_INDICATOR = "%s-prox-%s";
    private static final int GOVERNED_EMAIL_SUFFIX_LENGTH = 4;
    public RegistrationService(SurveyService surveyService,
                               PortalEnvironmentService portalEnvService,
                               PreregistrationResponseDao preregistrationResponseDao,
                               AnswerProcessingService answerProcessingService,
                               ParticipantUserService participantUserService,
                               PortalParticipantUserService portalParticipantUserService,
                               EventService eventService, ObjectMapper objectMapper,
                               RandomUtilService randomUtilService,
                               ProfileService profileService) {
        this.surveyService = surveyService;
        this.portalEnvService = portalEnvService;
        this.preregistrationResponseDao = preregistrationResponseDao;
        this.answerProcessingService = answerProcessingService;
        this.participantUserService = participantUserService;
        this.portalParticipantUserService = portalParticipantUserService;
        this.eventService = eventService;
        this.objectMapper = objectMapper;
        this.randomUtilService = randomUtilService;
        this.profileService = profileService;
    }

    /**
     * Creates a preregistration survey record for a user who is not signed in
     */
    @Transactional
    public PreregistrationResponse createAnonymousPreregistration(
            String portalShortcode,
            EnvironmentName envName,
            String surveyStableId,
            Integer surveyVersion,
            ParsedPreRegResponse parsedResponse) throws JsonProcessingException {
        PreregistrationResponse response = new PreregistrationResponse();
        Survey survey = surveyService.findByStableId(surveyStableId, surveyVersion).get();
        PortalEnvironment portalEnv = portalEnvService.findOne(portalShortcode, envName).get();

        response.setSurveyId(survey.getId());
        response.setPortalEnvironmentId(portalEnv.getId());
        response.setFullData(objectMapper.writeValueAsString(parsedResponse.getAnswers()));
        return preregistrationResponseDao.create(response);
    }

    public Optional<PreregistrationResponse> find(UUID preRegResponseId) {
        return preregistrationResponseDao.find(preRegResponseId);
    }

    @Transactional
    public RegistrationResult register(String portalShortcode, EnvironmentName environmentName,
                                       String email, UUID preRegResponseId) {
        RequiredRegistrationInfo info = RequiredRegistrationInfo.builder().email(email).build();
        return register(portalShortcode, environmentName, preRegResponseId, info);
    }

    private RegistrationResult register(String portalShortcode, EnvironmentName environmentName, UUID preRegResponseId,
                                        RequiredRegistrationInfo info) {
        PortalEnvironment portalEnv = portalEnvService.findOne(portalShortcode, environmentName).get();
        PreregistrationResponse preRegResponse = null;
        if (portalEnv.getPreRegSurveyId() != null) {
            preRegResponse = validatePreRegResponseId(preRegResponseId);
        }

        Optional<ParticipantUser> existingUserOpt = participantUserService.findOne(info.email, environmentName);
        ParticipantUser user = existingUserOpt.orElseGet(() -> {
            ParticipantUser newUser = new ParticipantUser();
            newUser.setEnvironmentName(environmentName);
            newUser.setUsername(info.email);
            return participantUserService.create(newUser);
        });

        PortalParticipantUser ppUser = new PortalParticipantUser();
        ppUser.setPortalEnvironmentId(portalEnv.getId());
        ppUser.setParticipantUserId(user.getId());

        Profile profile =  Profile.builder()
                .contactEmail(info.getEmail())
                .givenName(info.getFirstName())
                .familyName(info.getLastName())
                .build();
        ppUser.setProfile(profile);

        ppUser = portalParticipantUserService.create(ppUser);
        if (preRegResponse != null) {
            preRegResponse.setPortalParticipantUserId(ppUser.getId());
            preregistrationResponseDao.update(preRegResponse);
        }
        eventService.publishPortalRegistrationEvent(user, ppUser, portalEnv);
        log.info("Portal registration: userId: {}, portal: {}", user.getId(), portalShortcode);
        return new RegistrationResult(user, ppUser, profile);
    }

    @Transactional
    public RegistrationResult registerGovernedUser(ParticipantUser proxyUser, PortalParticipantUser proxyPpUser, String governedUsername, ParticipantUser governedUser) {
        if (!proxyPpUser.getParticipantUserId().equals(proxyUser.getId())) {
            throw new IllegalArgumentException("user and portal participant user do not match");
        }
        if (governedUser == null) {
            governedUser = new ParticipantUser();
            governedUser.setEnvironmentName(proxyUser.getEnvironmentName());
            governedUser.setUsername(governedUsername);
            governedUser = participantUserService.create(governedUser);
        }

        PortalParticipantUser governedPpUser = new PortalParticipantUser();
        governedPpUser.setPortalEnvironmentId(proxyPpUser.getPortalEnvironmentId());
        governedPpUser.setParticipantUserId(governedUser.getId());

        Profile proxyProfile = profileService.find(proxyPpUser.getProfileId()).orElseThrow();
        Profile governedProfile =  Profile.builder()
                .contactEmail(proxyProfile.getContactEmail())
                .givenName(null)
                .familyName(null)
                .build();
        governedPpUser.setProfile(governedProfile);
        governedPpUser = portalParticipantUserService.create(governedPpUser);

        PortalEnvironment portalEnv = portalEnvService.find(proxyPpUser.getPortalEnvironmentId()).orElseThrow();

        eventService.publishPortalRegistrationEvent(governedUser, governedPpUser, portalEnv);
        log.info("Governed user registration: userId: {}, portal: {}, env: {}", governedUser.getId(), portalEnv.getPortalId(), portalEnv.getEnvironmentName());
        return new RegistrationResult(governedUser, governedPpUser, governedProfile);
    }

    protected PreregistrationResponse validatePreRegResponseId(UUID preRegResponseId) {
        if (preRegResponseId == null) {
            throw new IllegalArgumentException("Preregistration response was not provided");
        }
        Optional<PreregistrationResponse> preRegResponseOpt = preregistrationResponseDao.find(preRegResponseId);
        if (preRegResponseOpt.isEmpty()) {
            throw new IllegalArgumentException("Preregistration response id was not valid");
        }
        PreregistrationResponse preRegResponse = preRegResponseOpt.get();
        if (preRegResponse.getPortalParticipantUserId() != null) {
            throw new IllegalArgumentException("Preregistration response has already been registered");
        }
        return preRegResponse;
    }

    public String generateGovernedUsernameSuffix(String proxyUserName, EnvironmentName environmentName) {
        String guid = randomUtilService.generateSecureRandomString(GOVERNED_EMAIL_SUFFIX_LENGTH, GOVERNED_USERNAME_SUFFIX_CHARS);
        while(!participantUserService.findOne(proxyUserName + guid, environmentName).isEmpty()){
            guid = randomUtilService.generateSecureRandomString(GOVERNED_EMAIL_SUFFIX_LENGTH, GOVERNED_USERNAME_SUFFIX_CHARS);
        }
        return guid;
    }

    public String getGovernedUsername(String proxyUserName, EnvironmentName environmentName) {
        String governedUsernameSuffix = generateGovernedUsernameSuffix(proxyUserName, environmentName);
        return GOVERNED_USERNAME_INDICATOR.formatted(proxyUserName, governedUsernameSuffix);//a@b.com-prox-guid

    }
    public record RegistrationResult(ParticipantUser participantUser,
                                     PortalParticipantUser portalParticipantUser,
                                     Profile profile) {
    }

    @SuperBuilder
    @NoArgsConstructor
    public static class RequiredRegistrationInfo {
        @Getter
        @Setter
        private String firstName;
        @Getter
        @Setter
        private String lastName;
        @Getter
        @Setter
        private String email;
    }


    /** we'll likely want to move this into the database at some point along with the registration survey */
    public static final Map<String, String> REGISTRATION_FIELD_MAP = Map.of(
            "reg_firstName", "firstName",
            "reg_lastName", "lastName",
            "reg_email", "email");
}
