package bio.terra.pearl.core.dao.dashboard;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.dashboard.AlertTrigger;
import bio.terra.pearl.core.model.dashboard.ParticipantDashboardAlert;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ParticipantDashboardAlertDao extends BaseMutableJdbiDao<ParticipantDashboardAlert> {

    public ParticipantDashboardAlertDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<ParticipantDashboardAlert> getClazz() {
        return ParticipantDashboardAlert.class;
    }

    public List<ParticipantDashboardAlert> findByPortalEnvironmentId(UUID portalEnvironmentId) {
        return findAllByProperty("portal_environment_id", portalEnvironmentId);
    }

    public Optional<ParticipantDashboardAlert> findByPortalEnvironmentIdAndTrigger(UUID portalEnvironmentId, AlertTrigger trigger) {
        return findByTwoProperties("portal_environment_id", portalEnvironmentId, "trigger", trigger);
    }

    public void deleteByPortalEnvironmentId(UUID portalEnvironmentId) {
        deleteByProperty("portal_environment_id", portalEnvironmentId);
    }

}
