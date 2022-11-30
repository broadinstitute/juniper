package bio.terra.pearl.core.dao.participant;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class PortalParticipantUserDao extends BaseJdbiDao<PortalParticipantUser> {
    public PortalParticipantUserDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<PortalParticipantUser> getClazz() {
        return PortalParticipantUser.class;
    }

    public List<PortalParticipantUser> findByParticipantUserId(UUID userId) {
        return findAllByProperty("participant_user_od", userId);
    }

    public List<PortalParticipantUser> findByPortalId(UUID portalId) {
        return findAllByProperty("portal_id", portalId);
    }
    public void deleteByPortalId(UUID portalId) {
        deleteByUuidProperty("portal_id", portalId);
    }

}
