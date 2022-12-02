package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.dao.participant.ParticipantUserDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.service.CascadeProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class ParticipantUserService {
    private ParticipantUserDao participantUserDao;
    private PortalParticipantUserService portalParticipantUserService;

    public ParticipantUserService(ParticipantUserDao participantUserDao,
                                  PortalParticipantUserService portalParticipantUserService) {
        this.participantUserDao = participantUserDao;
        this.portalParticipantUserService = portalParticipantUserService;
    }

    @Transactional
    public ParticipantUser create(ParticipantUser participantUser) {
        return participantUserDao.create(participantUser);
    }

    @Transactional
    public void delete(UUID id, Set<CascadeProperty> cascades) {
        participantUserDao.delete(id);
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
        return participantUserDao.findOne(username, environmentName);
    }
}
