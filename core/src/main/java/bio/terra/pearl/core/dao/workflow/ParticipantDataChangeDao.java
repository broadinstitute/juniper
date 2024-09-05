package bio.terra.pearl.core.dao.workflow;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.audit.ParticipantDataChange;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ParticipantDataChangeDao extends BaseJdbiDao<ParticipantDataChange> {

    public ParticipantDataChangeDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<ParticipantDataChange> getClazz() {
        return ParticipantDataChange.class;
    }

    public List<ParticipantDataChange> findByEnrolleeId(UUID enrolleeId) {
        return findAllByProperty("enrollee_id", enrolleeId);
    }

    public List<ParticipantDataChange> findAllRecordsForEnrollee(UUID enrolleeId, UUID portalParticipantUserId) {
        return jdbi.withHandle(handle ->
                handle
                        .createQuery("SELECT * FROM " + tableName +
                                "    WHERE enrollee_id = :enrolleeId" +
                                "    OR portal_participant_user_id = :portalParticipantUserId;")
                        .bind("enrolleeId", enrolleeId)
                        .bind("portalParticipantUserId", portalParticipantUserId)
                        .mapTo(clazz)
                        .list()
        );
    }

    public List<ParticipantDataChange> findAllRecordsForEnrolleeAndModelName(UUID enrolleeId, UUID portalParticipantUserId, String modelName) {
        return jdbi.withHandle(handle ->
                handle
                        .createQuery("SELECT * FROM " + tableName +
                                "    WHERE enrollee_id = :enrolleeId" +
                                "    AND model_name = :modelName" +
                                "    OR portal_participant_user_id = :portalParticipantUserId;")
                        .bind("enrolleeId", enrolleeId)
                        .bind("modelName", modelName)
                        .bind("portalParticipantUserId", portalParticipantUserId)
                        .mapTo(clazz)
                        .list()
        );
    }


    public List<ParticipantDataChange> findByModelId(UUID modelId) {
        return findAllByProperty("model_id", modelId);
    }

    public List<ParticipantDataChange> findByPortalEnvironmentId(UUID portalEnvId) {
        return findAllByProperty("portal_environment_id", portalEnvId);
    }

    public void deleteByPortalParticipantUserId(UUID ppUserId) {
        deleteByProperty("portal_participant_user_id", ppUserId);
    }

    public void deleteByResponsibleUserId(UUID participantUserId) {
        deleteByProperty("responsible_user_id", participantUserId);
    }

    public void deleteByResponsibleAdminUserId(UUID adminUserId) {
        deleteByProperty("responsible_admin_user_id", adminUserId);
    }

    public void deleteByPortalEnvironmentId(UUID portalEnvId) {
        deleteByProperty("portal_environment_id", portalEnvId);
    }

    public void deleteByEnrolleeId(UUID enrolleeId) {
        deleteByProperty("enrollee_id", enrolleeId);
    }

    public List<ParticipantDataChange> findByFamilyId(UUID familyId) {
        return findAllByProperty("family_id", familyId);
    }

    public List<ParticipantDataChange> findByFamilyIdAndModelName(UUID familyId, String model) {
        return findAllByTwoProperties("family_id", familyId, "model_name", model);
    }

    public void deleteByStudyEnvironmentId(UUID studyEnvironmentId) {
        // delete records from enrollees and families in this study environment.
        jdbi.withHandle(handle ->
                handle.createUpdate(
                                "DELETE FROM " + tableName +
                                        " dcr WHERE dcr.enrollee_id IN (SELECT id FROM enrollee WHERE study_environment_id = :studyEnvironmentId) " +
                                        "OR dcr.family_id IN (SELECT id FROM family WHERE study_environment_id = :studyEnvironmentId)")
                        .bind("studyEnvironmentId", studyEnvironmentId)
                        .execute()
        );
    }
}
