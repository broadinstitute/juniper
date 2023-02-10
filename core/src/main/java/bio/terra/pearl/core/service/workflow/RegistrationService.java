package bio.terra.pearl.core.service.workflow;

import bio.terra.pearl.core.dao.survey.PreregistrationResponseDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.survey.ParsedSnapshot;
import bio.terra.pearl.core.model.survey.PreregistrationResponse;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.participant.ParticipantUserService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.survey.SnapshotParsingService;
import bio.terra.pearl.core.service.survey.SurveyService;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationService {
    private static final Logger logger = LoggerFactory.getLogger(RegistrationService.class);
    private SurveyService surveyService;
    private PortalEnvironmentService portalEnvService;
    private PreregistrationResponseDao preregistrationResponseDao;
    private SnapshotParsingService snapshotParsingService;
    private ParticipantUserService participantUserService;
    private PortalParticipantUserService portalParticipantUserService;

    public RegistrationService(SurveyService surveyService,
                               PortalEnvironmentService portalEnvService,
                               PreregistrationResponseDao preregistrationResponseDao,
                               SnapshotParsingService snapshotParsingService,
                               ParticipantUserService participantUserService,
                               PortalParticipantUserService portalParticipantUserService) {
        this.surveyService = surveyService;
        this.portalEnvService = portalEnvService;
        this.preregistrationResponseDao = preregistrationResponseDao;
        this.snapshotParsingService = snapshotParsingService;
        this.participantUserService = participantUserService;
        this.portalParticipantUserService = portalParticipantUserService;
    }

    /** Creates a preregistration survey record for a user who is not signed in */
    @Transactional
    public PreregistrationResponse createAnonymousPreregistration(
            String portalShortcode,
            EnvironmentName envName,
            String surveyStableId,
            Integer surveyVersion,
            String fullData) {
        PreregistrationResponse response = new PreregistrationResponse();
        Survey survey = surveyService.findByStableId(surveyStableId, surveyVersion).get();
        PortalEnvironment portalEnv = portalEnvService.findOne(portalShortcode, envName).get();

        response.setSurveyId(survey.getId());
        response.setPortalEnvironmentId(portalEnv.getId());
        response.setFullData(fullData);
        return preregistrationResponseDao.create(response);
    }

    public Optional<PreregistrationResponse> find(UUID preRegResponseId) {
        return preregistrationResponseDao.find(preRegResponseId);
    }


    public record RegistrationResult(ParticipantUser participantUser,
                                     PortalParticipantUser portalParticipantUser) {}

    /** creates a new PortalParticipantUser, and also a new ParticipantUser if necessary */
    @Transactional
    public RegistrationResult register(String portalShortcode, EnvironmentName environmentName,
                         ParsedSnapshot response, UUID preRegResponseId) {
        RequiredRegistrationInfo info = extractRegistrationValues(response);
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

        Profile profile = new Profile();
        profile.setContactEmail(info.getEmail());
        profile.setGivenName(info.getFirstName());
        profile.setFamilyName(info.getLastName());
        ppUser.setProfile(profile);

        PortalParticipantUser savedPPUSer = portalParticipantUserService.create(ppUser);
        if (preRegResponse != null) {
            preRegResponse.setPortalParticipantUserId(savedPPUSer.getId());
            preregistrationResponseDao.update(preRegResponse);
        }
        logger.info("Portal registration: userId: {}, portal: {}", user.getId(), portalShortcode);
        return new RegistrationResult(user, savedPPUSer);
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

    public static class RequiredRegistrationInfo {
        @Getter
        @Setter
        private String firstName;
        @Getter @Setter
        private String lastName;
        @Getter
        @Setter
        private String email;
    }

    protected RequiredRegistrationInfo extractRegistrationValues(ParsedSnapshot data) {
        return snapshotParsingService.extractValues(data,
                REGISTRATION_FIELD_MAP, RegistrationService.RequiredRegistrationInfo.class);
    }

    /** we'll likely want to move this into the database at some point along with the registration survey */
    public static final Map<String, String> REGISTRATION_FIELD_MAP = Map.of(
            "reg_firstName", "firstName",
            "reg_lastName", "lastName",
            "reg_email", "email");
}
