package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.dao.participant.ParticipantUserDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.CrudService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class ParticipantUserService extends CrudService<ParticipantUser, ParticipantUserDao> {
    private PortalParticipantUserService portalParticipantUserService;

    public ParticipantUserService(ParticipantUserDao participantUserDao,
                                  PortalParticipantUserService portalParticipantUserService) {
        super(participantUserDao);
        this.portalParticipantUserService = portalParticipantUserService;
    }

    @Transactional
    public void deleteOrphans(List<UUID> userIds, Set<CascadeProperty> cascades) {
        userIds.stream().forEach(userId -> {
            if (portalParticipantUserService.findByParticipantUserId(userId).size() == 0) {
                delete(userId, cascades);
            }
        });
    }

    public Optional<ParticipantUser> findOne(String username, EnvironmentName environmentName) {
        return dao.findOne(username, environmentName);
    }
}
