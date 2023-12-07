package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.dao.participant.PortalParticipantUserDao;
import bio.terra.pearl.core.dao.survey.PreregistrationResponseDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.ImmutableEntityService;
import bio.terra.pearl.core.service.workflow.DataChangeRecordService;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PortalParticipantUserService extends ImmutableEntityService<PortalParticipantUser, PortalParticipantUserDao> {
    private ProfileService profileService;
    private PreregistrationResponseDao preregistrationResponseDao;
    private DataChangeRecordService dataChangeRecordService;

    public PortalParticipantUserService(PortalParticipantUserDao dao,
                                        ProfileService profileService,
                                        PreregistrationResponseDao preregistrationResponseDao,
                                        @Lazy DataChangeRecordService dataChangeRecordService) {
        super(dao);
        this.profileService = profileService;
        this.preregistrationResponseDao = preregistrationResponseDao;
        this.dataChangeRecordService = dataChangeRecordService;
    }

    @Transactional
    public PortalParticipantUser create(PortalParticipantUser ppUser) {
        Profile newProfile;
        if (ppUser.getProfileId() != null) {
            newProfile = profileService.find(ppUser.getProfileId()).get();
        } else if (ppUser.getProfile() != null) {
            newProfile = profileService.create(ppUser.getProfile());
        } else {
            // Make sure profile is always non-null
            newProfile = profileService.create(Profile.builder().build());
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

    public Optional<PortalParticipantUser> findOne(UUID participantUserId, String portalShortcode) {
        return dao.findOne(participantUserId, portalShortcode);
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

    @Override @Transactional
    public void delete(UUID portalParticipantUserId, Set<CascadeProperty> cascades) {
        PortalParticipantUser ppUser = dao.find(portalParticipantUserId).get();
        preregistrationResponseDao.deleteByPortalParticipantUserId(portalParticipantUserId);
        dataChangeRecordService.deleteByPortalParticipantUserId(portalParticipantUserId);
        dao.delete(portalParticipantUserId);

        if (ppUser.getProfileId() != null) {
            profileService.delete(ppUser.getProfileId(), cascades);
        }
    }

    @Transactional
    public void deleteByParticipantUserId(UUID participantUserId) {
        List<PortalParticipantUser> users = dao.findByParticipantUserId(participantUserId);
        for(PortalParticipantUser ppUser : users) {
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
