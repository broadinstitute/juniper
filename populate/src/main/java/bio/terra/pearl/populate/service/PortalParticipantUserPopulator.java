package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.service.participant.ParticipantUserService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.populate.service.contexts.PortalPopulateContext;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class PortalParticipantUserPopulator extends Populator<PortalParticipantUser, PortalPopulateContext> {
    private ParticipantUserService participantUserService;
    private PortalParticipantUserService portalParticipantUserService;
    private PortalEnvironmentService portalEnvironmentService;

    public PortalParticipantUserPopulator(ParticipantUserService participantUserService,
                                          PortalParticipantUserService portalParticipantUserService,
                                          PortalEnvironmentService portalEnvironmentService) {
        this.participantUserService = participantUserService;
        this.portalParticipantUserService = portalParticipantUserService;
        this.portalEnvironmentService = portalEnvironmentService;
    }

    @Override
    public PortalParticipantUser populateFromString(String fileContent, PortalPopulateContext context) throws IOException {
        // first populate the participant user
        PortalParticipantUser ppUserDto = objectMapper.readValue(fileContent, PortalParticipantUser.class);
        ParticipantUser participantUserDto = ppUserDto.getParticipantUser();
        // we ignore any environment name in the json file so that we can easily re-use participant files across
        // environments
        participantUserDto.setEnvironmentName(context.getEnvironmentName());
        Optional<ParticipantUser> existingUserOpt = participantUserService
                .findOne(participantUserDto.getUsername(), context.getEnvironmentName());
        ParticipantUser user = existingUserOpt.orElseGet(() -> participantUserService.create(participantUserDto));
        ppUserDto.setParticipantUserId(user.getId());

        // now populate the PortalParticipantUser
        PortalEnvironment portalEnvironment = portalEnvironmentService
                .findOne(context.getPortalShortcode(), context.getEnvironmentName()).get();
        Optional<PortalParticipantUser> existingPPUser = portalParticipantUserService
                .findOne(user.getId(), portalEnvironment.getId());
        existingPPUser.ifPresent(ppUser -> portalParticipantUserService.delete(ppUser.getId(), new HashSet<>()));
        ppUserDto.setPortalEnvironmentId(portalEnvironment.getId());
        PortalParticipantUser newPPUser = portalParticipantUserService.create(ppUserDto);
        return newPPUser;
    }
}
