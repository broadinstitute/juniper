package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.dao.participant.PortalParticipantUserDao;
import bio.terra.pearl.core.dao.survey.PreregistrationResponseDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.CrudService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class PortalParticipantUserService extends CrudService<PortalParticipantUser, PortalParticipantUserDao> {
    private ProfileService profileService;
    private PreregistrationResponseDao preregistrationResponseDao;

    public PortalParticipantUserService(PortalParticipantUserDao dao,
                                        ProfileService profileService,
                                        PreregistrationResponseDao preregistrationResponseDao) {
        super(dao);
        this.profileService = profileService;
        this.preregistrationResponseDao = preregistrationResponseDao;
    }

    @Transactional
    public PortalParticipantUser create(PortalParticipantUser ppUser) {
        Profile newProfile = null;
        if (ppUser.getProfile() != null) {
            newProfile = profileService.create(ppUser.getProfile());
            ppUser.setProfileId(newProfile.getId());
        }
        PortalParticipantUser createdUser = dao.create(ppUser);
        createdUser.setProfile(newProfile);
        return createdUser;
    }

    public List<PortalParticipantUser> findByParticipantUserId(UUID userId) {
        return dao.findByParticipantUserId(userId);
    }

    public Optional<PortalParticipantUser> findOne(UUID userId, UUID portalEnvId) {
        return dao.findOne(userId, portalEnvId);
    }

    public Optional<PortalParticipantUser> findOne(UUID participantUserId, String portalShortcode, EnvironmentName envName) {
        return dao.findOne(participantUserId, portalShortcode, envName);
    }

    public Optional<PortalParticipantUser> findOne(UUID participantUserId, String portalShortcode) {
        return dao.findOne(participantUserId, portalShortcode);
    }

    public List<PortalParticipantUser> findByPortalEnvironmentId(UUID portalId) {
        return dao.findByPortalEnvironmentId(portalId);
    }

    public void delete(UUID portalParticipantUserId, Set<CascadeProperty> cascades) {
        PortalParticipantUser ppUser = dao.find(portalParticipantUserId).get();
        preregistrationResponseDao.deleteByPortalParticipantUserId(portalParticipantUserId);
        dao.delete(portalParticipantUserId);

        if (ppUser.getProfileId() != null) {
            profileService.delete(ppUser.getProfileId(), cascades);
        }
    }

    @Transactional
    public void deleteByPortalEnvironmentId(UUID portalEnvId) {
        List<PortalParticipantUser> users = dao.findByPortalEnvironmentId(portalEnvId);
        for(PortalParticipantUser ppUser : users) {
            delete(ppUser.getId(), CascadeProperty.EMPTY_SET);
        }
    }
}
