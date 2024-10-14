package bio.terra.pearl.core.dao.dataimport;

import bio.terra.pearl.core.dao.workflow.ParticipantDataChangeDao;
import bio.terra.pearl.core.model.audit.ParticipantDataChange;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** dao for operations that are illegal outside of merge contexts */
@Component
public class MergeDao {
    private final Jdbi jdbi;
    private final ParticipantDataChangeDao participantDataChangeDao;

    public MergeDao(Jdbi jdbi, ParticipantDataChangeDao participantDataChangeDao) {
        this.jdbi = jdbi;
        this.participantDataChangeDao = participantDataChangeDao;
    }

    public void reassignEnrolleeEvents(UUID sourceEnrolleeId, UUID targetEnrolleeId) {
        // reassign all events from source to target
        jdbi.withHandle(handle ->
                handle.createUpdate("""
                        update event set enrollee_id = :targetEnrolleeId where enrollee_id = :sourceEnrolleeId;
                        """)
                        .bind("sourceEnrolleeId", sourceEnrolleeId)
                        .bind("targetEnrolleeId", targetEnrolleeId)
                        .execute()
        );
    }

    public void reassignEnrolleeNotifications(UUID sourceEnrolleeId, UUID targetEnrolleeId, UUID targetParticipantUserId) {
        jdbi.withHandle(handle ->
                handle.createUpdate("""
                        update notification set enrollee_id = :targetEnrolleeId,
                         participant_user_id = :targetParticipantUserId where enrollee_id = :sourceEnrolleeId;
                        """)
                        .bind("sourceEnrolleeId", sourceEnrolleeId)
                        .bind("targetEnrolleeId", targetEnrolleeId)
                        .bind("targetParticipantUserId", targetParticipantUserId)
                        .execute()
        );
    }

    public void reassignParticipantNotes(UUID sourceEnrolleeId, UUID targetEnrolleeId) {
        jdbi.withHandle(handle ->
                handle.createUpdate("""
                        update participant_note set enrollee_id = :targetEnrolleeId where enrollee_id = :sourceEnrolleeId;
                        """)
                        .bind("sourceEnrolleeId", sourceEnrolleeId)
                        .bind("targetEnrolleeId", targetEnrolleeId)
                        .execute()
        );
    }

    public void reassignFamily(UUID sourceEnrolleeId, UUID targetEnrolleeId) {
        jdbi.withHandle(handle ->
                handle.createUpdate("""
                        update family set proband_enrollee_id = :targetEnrolleeId where proband_enrollee_id = :sourceEnrolleeId;
                        """)
                        .bind("sourceEnrolleeId", sourceEnrolleeId)
                        .bind("targetEnrolleeId", targetEnrolleeId)
                        .execute()
        );
        jdbi.withHandle(handle ->
                handle.createUpdate("""
                        update family_enrollee set enrollee_id = :targetEnrolleeId where enrollee_id = :sourceEnrolleeId;
                        """)
                        .bind("sourceEnrolleeId", sourceEnrolleeId)
                        .bind("targetEnrolleeId", targetEnrolleeId)
                        .execute()
        );
        jdbi.withHandle(handle ->
                handle.createUpdate("""
                        update enrollee_relation set enrollee_id = :targetEnrolleeId where enrollee_id = :sourceEnrolleeId;
                        """)
                        .bind("sourceEnrolleeId", sourceEnrolleeId)
                        .bind("targetEnrolleeId", targetEnrolleeId)
                        .execute()
        );
        jdbi.withHandle(handle ->
                handle.createUpdate("""
                        update enrollee_relation set target_enrollee_id = :targetEnrolleeId where target_enrollee_id = :sourceEnrolleeId;
                        """)
                        .bind("sourceEnrolleeId", sourceEnrolleeId)
                        .bind("targetEnrolleeId", targetEnrolleeId)
                        .execute()
        );

    }

    public void updateParticipantDataChange(ParticipantDataChange change) {
        jdbi.withHandle(handle ->
                handle.createUpdate("""
                        update participant_data_change set responsible_user_id = :responsibleUserId,
                        enrollee_id = :enrolleeId,
                        portal_participant_user_id = :portalParticipantUserId
                        where id = :id;
                        """)
                        .bind("enrolleeId", change.getEnrolleeId())
                        .bind("responsibleUserId", change.getResponsibleUserId())
                        .bind("portalParticipantUserId", change.getPortalParticipantUserId())
                        .bind("id", change.getId())
                        .execute()
        );
    }
}
