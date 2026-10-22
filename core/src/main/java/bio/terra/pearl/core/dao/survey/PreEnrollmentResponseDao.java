package bio.terra.pearl.core.dao.survey;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.survey.PreEnrollmentResponse;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PreEnrollmentResponseDao extends BaseMutableJdbiDao<PreEnrollmentResponse> {
    public PreEnrollmentResponseDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<PreEnrollmentResponse> getClazz() {
        return PreEnrollmentResponse.class;
    }

    public void deleteByStudyEnvironmentId(UUID studyEnvironmentId) {
        deleteByProperty("study_environment_id", studyEnvironmentId);
    }

    public void deleteByPortalParticipantUserId(UUID portalParticipantUserId) {
        deleteByProperty("portal_participant_user_id", portalParticipantUserId);
    }
}
