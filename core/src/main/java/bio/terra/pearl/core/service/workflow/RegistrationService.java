package bio.terra.pearl.core.service.workflow;

import bio.terra.pearl.core.dao.survey.PreregistrationResponseDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.survey.ParsedPreRegResponse;
import bio.terra.pearl.core.model.survey.PreregistrationResponse;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.participant.ParticipantUserService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.participant.ProfileService;
import bio.terra.pearl.core.service.participant.RandomUtilService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.portal.PortalService;
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

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

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
    private PortalService portalService;
    private ObjectMapper objectMapper;

    private static final String GOVERNED_USERNAME_SUFFIX_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String GOVERNED_USERNAME_INDICATOR = "%s-prox-%s";
    private static final String DERIVED_USERNAME_INDICATOR = "%s-derived-%s";
    private static final int GOVERNED_EMAIL_SUFFIX_LENGTH = 4;
    public RegistrationService(SurveyService surveyService,
                               PortalEnvironmentService portalEnvService,
                               PreregistrationResponseDao preregistrationResponseDao,
                               AnswerProcessingService answerProcessingService,
                               ParticipantUserService participantUserService,
                               PortalParticipantUserService portalParticipantUserService,
                               EventService eventService, ObjectMapper objectMapper,
                               RandomUtilService randomUtilService,
                               ProfileService profileService, PortalService portalService) {
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
        this.portalService = portalService;
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
        Portal portal = portalService.findOneByShortcode(portalShortcode).orElseThrow();
        Survey survey = surveyService.findByStableId(surveyStableId, surveyVersion, portal.getId()).get();
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
                                       String email, UUID preRegResponseId, String preferredLanguage) {
        RequiredRegistrationInfo info = RequiredRegistrationInfo.builder().email(email).build();
        return register(portalShortcode, environmentName, preRegResponseId, preferredLanguage, info);
    }

    private RegistrationResult register(String portalShortcode, EnvironmentName environmentName, UUID preRegResponseId,
                                        String preferredLanguage, RequiredRegistrationInfo info) {
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

        Profile profile = Profile.builder()
                .contactEmail(info.getEmail())
                .givenName(info.getFirstName())
                .familyName(info.getLastName())
                .build();
        if(preferredLanguage != null){
            profile.setPreferredLanguage(preferredLanguage);
        }
        ppUser.setProfile(profile);

        ppUser = portalParticipantUserService.create(ppUser);

        if (preRegResponse != null) {
            preRegResponse.setPortalParticipantUserId(ppUser.getId());
            preregistrationResponseDao.update(preRegResponse);
        }
        eventService.publishPortalRegistrationEvent(user, ppUser, portalEnv);
        log.info("Portal registration: userId: {}, portal: {}", user.getId(), portalShortcode);
        return new RegistrationResult(user, ppUser, ppUser.getProfile());
    }

    /**
     * Registers a new user whose account is derived from an existing "source" user. Used for governed (proxied)
     * users and for staff created family members.
     */
    @Transactional
    public RegistrationResult registerDerivedUser(ParticipantUser sourceUser, PortalParticipantUser sourcePpUser, String derivedUsername, ParticipantUser derivedUser) {
        if (!sourcePpUser.getParticipantUserId().equals(sourceUser.getId())) {
            throw new IllegalArgumentException("user and portal participant user do not match");
        }
        if (derivedUser == null) {
            derivedUser = new ParticipantUser();
            derivedUser.setEnvironmentName(sourceUser.getEnvironmentName());
            derivedUser.setUsername(derivedUsername);
            derivedUser = participantUserService.create(derivedUser);
        }

        PortalParticipantUser derivedPpUser = new PortalParticipantUser();
        derivedPpUser.setPortalEnvironmentId(sourcePpUser.getPortalEnvironmentId());
        derivedPpUser.setParticipantUserId(derivedUser.getId());

        Profile sourceProfile = profileService.find(sourcePpUser.getProfileId()).orElseThrow();
        Profile derivedProfile = Profile.builder()
                .contactEmail(sourceProfile.getContactEmail())
                .doNotEmail(sourceProfile.isDoNotEmail())
                .preferredLanguage(sourceProfile.getPreferredLanguage())
                .givenName(null)
                .familyName(null)
                .build();
        derivedPpUser.setProfile(derivedProfile);
        derivedPpUser = portalParticipantUserService.create(derivedPpUser);

        PortalEnvironment portalEnv = portalEnvService.find(sourcePpUser.getPortalEnvironmentId()).orElseThrow();

        eventService.publishPortalRegistrationEvent(derivedUser, derivedPpUser, portalEnv);
        log.info("Derived user registration: userId: {}, portal: {}, env: {}", derivedUser.getId(), portalEnv.getPortalId(), portalEnv.getEnvironmentName());
        return new RegistrationResult(derivedUser, derivedPpUser, derivedProfile);
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

    /** username for a user derived from (but not governed by) another user, e.g. a@b.com-derived-ABCD */
    public String getDerivedUsername(String sourceUserName, EnvironmentName environmentName) {
        String suffix = generateGovernedUsernameSuffix(sourceUserName, environmentName);
        return DERIVED_USERNAME_INDICATOR.formatted(sourceUserName, suffix);
    }

    public static String removeProxySuffix(String email) {
        // if the email ends with -prox-XXXX or -derived-XXXX, remove it
        Pattern proxySuffix = Pattern.compile("-(prox|derived)-[A-Z]{4}$");

        return proxySuffix.matcher(email).replaceAll("");
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
