package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.dao.participant.ParticipantUserDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.service.CascadeTree;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ParticipantUserService {
    private ParticipantUserDao participantUserDao;
    private ProfileService profileService;
    private PortalParticipantUserService portalParticipantUserService;

    public ParticipantUserService(ParticipantUserDao participantUserDao, ProfileService profileService, PortalParticipantUserService portalParticipantUserService) {
        this.participantUserDao = participantUserDao;
        this.profileService = profileService;
        this.portalParticipantUserService = portalParticipantUserService;
    }

    @Transactional
    public ParticipantUser create(ParticipantUser participantUser) {
        Profile newProfile = null;
        if (participantUser.getProfile() != null) {
            newProfile = profileService.create(participantUser.getProfile());
            participantUser.setProfileId(newProfile.getId());
        }
        ParticipantUser newUser = participantUserDao.create(participantUser);
        newUser.setProfile(newProfile);
        return newUser;
    }

    @Transactional
    public void delete(UUID id) {
        ParticipantUser user = participantUserDao.find(id).get();
        participantUserDao.delete(id);
        if (user.getProfileId() != null) {
            profileService.delete(user.getProfileId());
        }
    }

    @Transactional
    public void deleteOrphans(List<UUID> userIds, CascadeTree cascades) {
        userIds.stream().forEach(userId -> {
            if (portalParticipantUserService.findByParticipantUserId(userId).size() == 0) {
                delete(userId);
            }
        });
    }

    public Optional<ParticipantUser> findOne(String username, EnvironmentName environmentName) {
        return participantUserDao.findOne(username, environmentName);
    }
}
