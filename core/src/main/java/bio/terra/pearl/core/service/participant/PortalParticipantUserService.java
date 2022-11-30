package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.dao.participant.PortalParticipantUserDao;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PortalParticipantUserService {
    private PortalParticipantUserDao portalParticipantUserDao;

    public PortalParticipantUserService(PortalParticipantUserDao portalParticipantUserDao) {
        this.portalParticipantUserDao = portalParticipantUserDao;
    }

    @Transactional
    public PortalParticipantUser create(UUID portalId, UUID participantUserId) {
        PortalParticipantUser ppUser = PortalParticipantUser.builder()
                .portalId(portalId).participantUserId(participantUserId).build();
        return portalParticipantUserDao.create(ppUser);
    }

    public List<PortalParticipantUser> findByParticipantUserId(UUID userId) {
        return portalParticipantUserDao.findByParticipantUserId(userId);
    }

    public List<PortalParticipantUser> findByPortalId(UUID portalId) {
        return portalParticipantUserDao.findByPortalId(portalId);
    }

    @Transactional
    public void deleteByPortalId(UUID portalId) {
        portalParticipantUserDao.deleteByPortalId(portalId);
    }
}
