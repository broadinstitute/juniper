package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.service.participant.ParticipantUserService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.populate.service.contexts.PortalPopulateContext;
import java.util.HashSet;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class PortalParticipantUserPopulator extends Populator<PortalParticipantUser, PortalParticipantUser, PortalPopulateContext> {
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
    protected void updateDtoFromContext(PortalParticipantUser popDto, PortalPopulateContext context) {
        ParticipantUser userDto = popDto.getParticipantUser();
        userDto.setEnvironmentName(context.getEnvironmentName());
        Optional<ParticipantUser> existingUserOpt = participantUserService
                .findOne(userDto.getUsername(), context.getEnvironmentName());
        ParticipantUser user = existingUserOpt.orElseGet(() -> participantUserService.create(userDto));

        PortalEnvironment portalEnvironment = portalEnvironmentService
                .findOne(context.getPortalShortcode(), context.getEnvironmentName()).get();
        popDto.setPortalEnvironmentId(portalEnvironment.getId());
        popDto.setParticipantUserId(user.getId());
    }

    @Override
    protected Class<PortalParticipantUser> getDtoClazz() {
        return PortalParticipantUser.class;
    }

    @Override
    public Optional<PortalParticipantUser> findFromDto(PortalParticipantUser popDto, PortalPopulateContext context) {
        ParticipantUser user = popDto.getParticipantUser();
        PortalEnvironment portalEnvironment = portalEnvironmentService
                .findOne(context.getPortalShortcode(), context.getEnvironmentName()).get();
        return portalParticipantUserService.findOne(user.getId(), portalEnvironment.getId());
    }

    @Override
    public PortalParticipantUser overwriteExisting(PortalParticipantUser existingObj, PortalParticipantUser popDto, PortalPopulateContext context) {
        portalParticipantUserService.delete(existingObj.getId(), new HashSet<>());
        return createNew(popDto, context, false);
    }

    @Override
    public PortalParticipantUser createPreserveExisting(PortalParticipantUser existingObj, PortalParticipantUser popDto, PortalPopulateContext context) {
        // we don't support preserving existing for participant/enrollee type objects
        return overwriteExisting(existingObj, popDto, context);
    }

    @Override
    public PortalParticipantUser createNew(PortalParticipantUser popDto, PortalPopulateContext context, boolean overwrite) {
        return portalParticipantUserService.create(popDto);
    }
}
