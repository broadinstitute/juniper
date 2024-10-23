package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.dao.participant.PortalParticipantUserDao;
import bio.terra.pearl.core.dao.survey.PreregistrationResponseDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.CrudService;
import bio.terra.pearl.core.service.ImmutableEntityService;
import bio.terra.pearl.core.service.workflow.ParticipantDataChangeService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class PortalParticipantUserService extends CrudService<PortalParticipantUser, PortalParticipantUserDao> {
    private final ProfileService profileService;
    private final PreregistrationResponseDao preregistrationResponseDao;
    private final ParticipantDataChangeService participantDataChangeService;
    private final ParticipantUserService participantUserService;

    public PortalParticipantUserService(PortalParticipantUserDao dao,
                                        ProfileService profileService,
                                        PreregistrationResponseDao preregistrationResponseDao,
                                        @Lazy ParticipantDataChangeService participantDataChangeService,
                                        @Lazy ParticipantUserService participantUserService) {
        super(dao);
        this.profileService = profileService;
        this.preregistrationResponseDao = preregistrationResponseDao;
        this.participantDataChangeService = participantDataChangeService;
        this.participantUserService = participantUserService;
    }

    @Transactional
    public PortalParticipantUser create(PortalParticipantUser ppUser) {
        Profile newProfile;
        if (ppUser.getProfileId() != null) {
            newProfile = profileService.find(ppUser.getProfileId()).get();
        } else if (ppUser.getProfile() != null) {
            newProfile = profileService.create(ppUser.getProfile(), DataAuditInfo
                    .builder()
                    .responsibleUserId(ppUser.getParticipantUserId())
                    .build());
        } else {
            // Make sure profile is always non-null
            newProfile = profileService.create(Profile.builder().build(), DataAuditInfo
                    .builder()
                    .responsibleUserId(ppUser.getParticipantUserId())
                    .build());
        }
        ppUser.setProfileId(newProfile.getId());
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

    public Optional<PortalParticipantUser> findOne(String username, String portalShortcode, EnvironmentName envName) {
        return participantUserService.findOne(username, envName).map(
             user -> findOne(user.getId(), portalShortcode, envName)
        ).orElse(Optional.empty());
    }

    public Optional<PortalParticipantUser> findOne(UUID participantUserId, String portalShortcode) {
        return dao.findOne(participantUserId, portalShortcode);
    }

    public void attachProfiles(List<PortalParticipantUser> portalParticipantUsers) {
        dao.attachProfiles(portalParticipantUsers);
    }

    public List<PortalParticipantUser> findByPortalEnvironmentId(UUID portalId) {
        return dao.findByPortalEnvironmentId(portalId);
    }

    public Optional<PortalParticipantUser> findByProfileId(UUID profileId) {
        return dao.findByProfileId(profileId);
    }

    public PortalParticipantUser findForEnrollee(Enrollee enrollee) {
        Optional<PortalParticipantUser> ppUser = dao.findByProfileId(enrollee.getProfileId());
        return ppUser.orElseThrow(() ->
                new IllegalStateException("No portal participant user found for enrollee %s".formatted(enrollee.getShortcode())));
    }

    public List<PortalParticipantUser> findByProfileIds(List<UUID> profileIds) {
        return dao.findByProfileIds(profileIds);
    }

    @Override @Transactional
    public void delete(UUID portalParticipantUserId, Set<CascadeProperty> cascades) {
        PortalParticipantUser ppUser = dao.find(portalParticipantUserId).get();
        preregistrationResponseDao.deleteByPortalParticipantUserId(portalParticipantUserId);
        participantDataChangeService.deleteByPortalParticipantUserId(portalParticipantUserId);

        dao.delete(portalParticipantUserId);
        if (ppUser.getProfileId() != null) {
            profileService.delete(
                    ppUser.getProfileId(),
                    DataAuditInfo
                            .builder()
                            .responsibleUserId(ppUser.getParticipantUserId())
                            .build());
        }
    }

    @Transactional
    public void deleteByParticipantUserId(UUID participantUserId) {
        List<PortalParticipantUser> users = dao.findByParticipantUserId(participantUserId);
        participantDataChangeService.deleteByResponsibleUserId(participantUserId);
        for(PortalParticipantUser ppUser : users) {
            participantDataChangeService.deleteByPortalParticipantUserId(ppUser.getId());
            delete(ppUser.getId(), CascadeProperty.EMPTY_SET);
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
