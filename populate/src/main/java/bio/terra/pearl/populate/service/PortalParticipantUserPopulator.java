package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.service.participant.ParticipantUserService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;

@Component
public class PortalParticipantUserPopulator extends Populator<PortalParticipantUser> {
    private ParticipantUserService participantUserService;
    private PortalParticipantUserService portalParticipantUserService;
    private PortalEnvironmentService portalEnvironmentService;

    public PortalParticipantUserPopulator(ObjectMapper objectMapper, ParticipantUserService participantUserService,
                                          FilePopulateService filePopulateService,
                                          PortalParticipantUserService portalParticipantUserService,
                                          PortalEnvironmentService portalEnvironmentService) {
        this.participantUserService = participantUserService;
        this.portalParticipantUserService = portalParticipantUserService;
        this.portalEnvironmentService = portalEnvironmentService;
        this.filePopulateService = filePopulateService;
        this.objectMapper = objectMapper;
    }

    @Override
    public PortalParticipantUser populateFromString(String fileContent, FilePopulateConfig config) throws IOException {
        // first populate the participant user
        PortalParticipantUser ppUserDto = objectMapper.readValue(fileContent, PortalParticipantUser.class);
        ParticipantUser participantUserDto = ppUserDto.getParticipantUser();
        // we ignore any environment name in the json file so that we can easily re-use participant files across
        // environments
        participantUserDto.setEnvironmentName(config.getEnvironmentName());
        Optional<ParticipantUser> existingUserOpt = participantUserService
                .findOne(participantUserDto.getUsername(), config.getEnvironmentName());
        ParticipantUser user = existingUserOpt.orElse(participantUserService.create(participantUserDto));
        ppUserDto.setParticipantUserId(user.getId());

        // now populate the PortalParticipantUser
        PortalEnvironment portalEnvironment = portalEnvironmentService
                .findOne(config.getPortalShortcode(), config.getEnvironmentName()).get();
        Optional<PortalParticipantUser> existingPPUser = portalParticipantUserService
                .findOne(user.getId(), portalEnvironment.getId());
        existingPPUser.ifPresent(ppUser -> portalParticipantUserService.delete(ppUser.getId(), new HashSet<>()));
        ppUserDto.setPortalEnvironmentId(portalEnvironment.getId());
        PortalParticipantUser newPPUser = portalParticipantUserService.create(ppUserDto);
        return newPPUser;
    }
}
