package bio.terra.pearl.core.dao.participant;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class PortalParticipantUserDao extends BaseJdbiDao<PortalParticipantUser> {
    private ProfileDao profileDao;
    public PortalParticipantUserDao(Jdbi jdbi, ProfileDao profileDao) {
        super(jdbi);
        this.profileDao = profileDao;
    }

    @Override
    protected Class<PortalParticipantUser> getClazz() {
        return PortalParticipantUser.class;
    }

    public Optional<PortalParticipantUser> findOne(UUID userId, UUID portalEnvId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("select * from " + tableName
                                + " where participant_user_id = :userId and portal_environment_id = :portalEnvId")
                        .bind("userId", userId)
                        .bind("portalEnvId", portalEnvId)
                        .mapTo(clazz)
                        .findOne()
        );
    }

    public List<PortalParticipantUser> findByParticipantUserId(UUID userId) {
        return findAllByProperty("participant_user_od", userId);
    }

    public List<PortalParticipantUser> findByPortalEnvironmentId(UUID portalEnvId) {
        return findAllByProperty("portal_environment_id", portalEnvId);
    }



    /**
     * loads the user along with their profile
     * */
    public Optional<PortalParticipantUser> getWithProfile(UUID portalParticipantUserId) {
        return findWithChild(portalParticipantUserId, "profileId", "profile", profileDao);
    }


    public void deleteByPortalEnvironmentId(UUID portalEnvId) {
        deleteByUuidProperty("portal_environment_id", portalEnvId);
    }

}
