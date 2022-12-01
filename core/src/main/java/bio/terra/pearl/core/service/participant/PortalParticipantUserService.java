package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.dao.participant.PortalParticipantUserDao;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.service.CascadeProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class PortalParticipantUserService {
    private PortalParticipantUserDao portalParticipantUserDao;
    private ProfileService profileService;

    public PortalParticipantUserService(PortalParticipantUserDao portalParticipantUserDao,
                                        ProfileService profileService) {
        this.portalParticipantUserDao = portalParticipantUserDao;
        this.profileService = profileService;
    }

    @Transactional
    public PortalParticipantUser create(PortalParticipantUser ppUser) {
        Profile newProfile = null;
        if (ppUser.getProfile() != null) {
            newProfile = profileService.create(ppUser.getProfile());
            ppUser.setProfileId(newProfile.getId());
        }
        PortalParticipantUser createdUser = portalParticipantUserDao.create(ppUser);
        createdUser.setProfile(newProfile);
        return createdUser;
    }

    public List<PortalParticipantUser> findByParticipantUserId(UUID userId) {
        return portalParticipantUserDao.findByParticipantUserId(userId);
    }

    public Optional<PortalParticipantUser> findOne(UUID userId, UUID portalEnvId) {
        return portalParticipantUserDao.findOne(userId, portalEnvId);
    }

    public List<PortalParticipantUser> findByPortalEnvironmentId(UUID portalId) {
        return portalParticipantUserDao.findByPortalEnvironmentId(portalId);
    }

    public void delete(UUID portalParticipantUserId, Set<CascadeProperty> cascades) {
        PortalParticipantUser ppUser = portalParticipantUserDao.find(portalParticipantUserId).get();
        portalParticipantUserDao.delete(portalParticipantUserId);
        if (ppUser.getProfileId() != null) {
            profileService.delete(ppUser.getProfileId(), cascades);
        }
    }

    @Transactional
    public void deleteByPortalEnvironmentId(UUID portalEnvId) {
        portalParticipantUserDao.deleteByPortalEnvironmentId(portalEnvId);
    }
}
