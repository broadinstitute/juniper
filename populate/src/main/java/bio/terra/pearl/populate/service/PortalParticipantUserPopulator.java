package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.service.notification.email.SendgridClient;
import bio.terra.pearl.core.service.participant.ParticipantUserService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.populate.dto.participant.ParticipantUserPopDto;
import bio.terra.pearl.populate.dto.participant.PortalParticipantUserPopDto;
import bio.terra.pearl.populate.service.contexts.PortalPopulateContext;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import bio.terra.pearl.populate.service.contexts.StudyPopulateContext;
import bio.terra.pearl.populate.util.PopulateUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class PortalParticipantUserPopulator extends BasePopulator<PortalParticipantUser, PortalParticipantUserPopDto, PortalPopulateContext> {
    private final ParticipantUserService participantUserService;
    private final PortalParticipantUserService portalParticipantUserService;
    private final PortalEnvironmentService portalEnvironmentService;
    private final String emailRedirectUsername;

    public PortalParticipantUserPopulator(ParticipantUserService participantUserService,
                                          PortalParticipantUserService portalParticipantUserService,
                                          PortalEnvironmentService portalEnvironmentService,
                                          Environment env) {
        this.participantUserService = participantUserService;
        this.portalParticipantUserService = portalParticipantUserService;
        this.portalEnvironmentService = portalEnvironmentService;
        emailRedirectUsername = env.getProperty(SendgridClient.EMAIL_REDIRECT_VAR, "nobody@nowhere")
                .split("@")[0];
    }

    @Override
    protected void preProcessDto(PortalParticipantUserPopDto popDto, PortalPopulateContext context) {
        ParticipantUserPopDto userDto = popDto.getParticipantUser();
        userDto.setEnvironmentName(context.getEnvironmentName());
        if (userDto.getUsernameKey() != null) {
            // concoct a username with the email redirect user and the key, so that b2c can be repeatedly tested
            userDto.setUsername("%s+%s-%s@broadinstitute.org".formatted(emailRedirectUsername, userDto.getUsernameKey(), RandomStringUtils.randomAlphabetic(8)));
        }
        Optional<ParticipantUser> existingUserOpt = participantUserService
                .findOne(userDto.getUsername(), context.getEnvironmentName());
        ParticipantUser user = existingUserOpt.orElseGet(() -> participantUserService.create(userDto));
        if (userDto.getLastLoginHoursAgo() != null) {
            Instant loginTime = Instant.now().minus(userDto.getLastLoginHoursAgo(), ChronoUnit.HOURS);
            user.setLastLogin(loginTime);
            participantUserService.update(user);
            popDto.setLastLogin(loginTime);
        }
        PortalEnvironment portalEnvironment = portalEnvironmentService
                .findOne(context.getPortalShortcode(), context.getEnvironmentName()).get();
        popDto.setPortalEnvironmentId(portalEnvironment.getId());
        popDto.setParticipantUserId(user.getId());
    }

    @Override
    protected Class<PortalParticipantUserPopDto> getDtoClazz() {
        return PortalParticipantUserPopDto.class;
    }

    @Override
    public Optional<PortalParticipantUser> findFromDto(PortalParticipantUserPopDto popDto, PortalPopulateContext context) {
        PortalEnvironment portalEnvironment = portalEnvironmentService
                .findOne(context.getPortalShortcode(), context.getEnvironmentName()).get();
        return portalParticipantUserService.findOne(popDto.getParticipantUserId(), portalEnvironment.getId());
    }

    @Override
    public PortalParticipantUser overwriteExisting(PortalParticipantUser existingObj, PortalParticipantUserPopDto popDto, PortalPopulateContext context) {
        portalParticipantUserService.delete(existingObj.getId(), new HashSet<>());
        return createNew(popDto, context, false);
    }

    @Override
    public PortalParticipantUser createPreserveExisting(PortalParticipantUser existingObj, PortalParticipantUserPopDto popDto, PortalPopulateContext context) {
        // we don't support updating participant users in-place yet
        return existingObj;
    }

    @Override
    public PortalParticipantUser createNew(PortalParticipantUserPopDto popDto, PortalPopulateContext context, boolean overwrite) {
        return portalParticipantUserService.create(popDto);
    }

    public List<String> bulkPopulateParticipants(String portalShortcode, EnvironmentName envName, String studyShortcode, Integer numEnrollees) {
        StudyPopulateContext context = new StudyPopulateContext("portals/" + portalShortcode + "/participants/seed.json", portalShortcode, studyShortcode, envName, new HashMap<>(), false, null);

        List<String> populatedUsernames = IntStream.range(0, numEnrollees).mapToObj(i -> {
            try {
                String fileString = filePopulateService.readFile(context.getRootFileName(), context);

                PortalParticipantUserPopDto popDto = objectMapper.readValue(fileString, getDtoClazz());
                popDto.setParticipantUserId(UUID.randomUUID());

                String username = PopulateUtils.generateEmail();

                ParticipantUser user = popDto.getParticipantUser();
                user.setUsername(username);
                user.setLastLogin(Instant.now().minusSeconds(Math.round(Math.random() * 60 * 60 * 24 * 30)));
                popDto.setParticipantUser(user);

                Profile profile = popDto.getProfile();
                profile.setContactEmail(username);
                profile.setGivenName(PopulateUtils.randomString(7));
                profile.setFamilyName(PopulateUtils.randomString(7));
                profile.setBirthDate(PopulateUtils.generateRandomDate());
                //do not attempt to send any emails to these users. it could easily eat up sendgrid quota
                profile.setDoNotEmail(true);
                profile.setDoNotEmailSolicit(true);
                popDto.setProfile(profile);

                populateFromDto(popDto, context, false);

                return username;
            } catch (IOException e) {
                throw new RuntimeException("Unable to bulk populate participants due to error: " + e.getMessage());
            }
        }).collect(Collectors.toList());

        return populatedUsernames;
    }

}
