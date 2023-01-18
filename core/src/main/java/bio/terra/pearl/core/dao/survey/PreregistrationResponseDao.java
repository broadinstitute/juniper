package bio.terra.pearl.core.dao.survey;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.survey.PreregistrationResponse;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class PreregistrationResponseDao extends BaseMutableJdbiDao<PreregistrationResponse> {
    public PreregistrationResponseDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<PreregistrationResponse> getClazz() {
        return PreregistrationResponse.class;
    }

    public void deleteByPortalEnvironmentId(UUID studyEnvId) {
        deleteByUuidProperty("portal_environment_id", studyEnvId);
    }

    public void deleteByPortalParticipantUserId(UUID portalParticipantUserId) {
        deleteByUuidProperty("portal_participant_user_id", portalParticipantUserId);
    }
}
