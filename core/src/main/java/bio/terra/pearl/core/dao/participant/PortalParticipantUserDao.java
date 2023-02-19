package bio.terra.pearl.core.dao.participant;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

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
        return findByTwoProperties("participant_user_id", userId,
                "portal_environment_id", portalEnvId);
    }

    public Optional<PortalParticipantUser> findOne(UUID participantUserId, String portalShortcode, EnvironmentName envName) {
        return jdbi.withHandle(handle ->
                handle.createQuery("select " + prefixedGetQueryColumns("a") + " from " + tableName + " a "
                        + " join portal_environment on a.portal_environment_id = portal_environment.id"
                        + " join portal on portal.id = portal_environment.portal_id"
                        + " join participant_user on a.participant_user_id = participant_user.id"
                        + " where portal.shortcode = :portalShortcode and participant_user_id = :participantUserId"
                        + " and portal_environment.environment_name = :envName;")
                        .bind("portalShortcode", portalShortcode)
                        .bind("participantUserId", participantUserId)
                        .bind("envName", envName)
                        .mapTo(clazz)
                        .findOne()
        );
    }

    public Optional<PortalParticipantUser> findOne(UUID participantUserId, String portalShortcode) {
        return jdbi.withHandle(handle ->
                handle.createQuery("select " + prefixedGetQueryColumns("a") + " from " + tableName + " a "
                                + " join portal_environment on a.portal_environment_id = portal_environment.id"
                                + " join portal on portal.id = portal_environment.portal_id"
                                + " join participant_user on a.participant_user_id = participant_user.id"
                                + " where portal.shortcode = :portalShortcode and participant_user_id = :participantUserId;")
                        .bind("portalShortcode", portalShortcode)
                        .bind("participantUserId", participantUserId)
                        .mapTo(clazz)
                        .findOne()
        );
    }

    public List<PortalParticipantUser> findByParticipantUserId(UUID userId) {
        return findAllByProperty("participant_user_id", userId);
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
        deleteByProperty("portal_environment_id", portalEnvId);
    }

}
