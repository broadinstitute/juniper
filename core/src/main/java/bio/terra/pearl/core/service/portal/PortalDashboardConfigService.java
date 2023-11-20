package bio.terra.pearl.core.service.portal;

import bio.terra.pearl.core.dao.dashboard.ParticipantDashboardAlertDao;
import bio.terra.pearl.core.model.ParticipantDashboardAlert;
import bio.terra.pearl.core.service.CrudService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PortalDashboardConfigService extends CrudService<ParticipantDashboardAlert, ParticipantDashboardAlertDao> {

    public PortalDashboardConfigService(ParticipantDashboardAlertDao participantDashboardAlertDao) {
        super(participantDashboardAlertDao);
    }

    public List<ParticipantDashboardAlert> findByPortalEnvId(UUID portalEnvId) {
        return dao.findByPortalEnvironmentId(portalEnvId);
    }

    public ParticipantDashboardAlert update(ParticipantDashboardAlert participantDashboardAlert) {
        return dao.update(participantDashboardAlert);
    }

}
