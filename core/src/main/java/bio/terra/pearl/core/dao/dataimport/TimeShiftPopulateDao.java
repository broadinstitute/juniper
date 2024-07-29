package bio.terra.pearl.core.dao.dataimport;

import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * collection of methods for timeshifting records in otherwise unpermissible ways
 */
@Component
public class TimeShiftPopulateDao {
    private Jdbi jdbi;

    public TimeShiftPopulateDao(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public void changeEnrolleeCreationTime(UUID enrolleeId, Instant creationTime) {
        jdbi.withHandle(handle ->
                handle.createUpdate("update enrollee set created_at = :creationTime where id = :enrolleeId;")
                        .bind("enrolleeId", enrolleeId)
                        .bind("creationTime", creationTime)
                        .execute()
        );
        // also update any tasks to match the enrollee creation time
        jdbi.withHandle(handle ->
                handle.createUpdate("update participant_task set created_at = :creationTime where enrollee_id = :enrolleeId;")
                        .bind("enrolleeId", enrolleeId)
                        .bind("creationTime", creationTime)
                        .execute()
        );
    }

    /**
     * update the survey and answers to be created at the given time
     */
    public void changeSurveyResponseTime(UUID surveyResponseId, Instant responseTime) {
        jdbi.withHandle(handle ->
                handle.createUpdate("update survey_response set created_at = :responseTime where id = :surveyResponseId;")
                        .bind("surveyResponseId", surveyResponseId)
                        .bind("responseTime", responseTime)
                        .execute()
        );
        jdbi.withHandle(handle ->
                handle.createUpdate("update answer set created_at = :responseTime where survey_response_id = :surveyResponseId;")
                        .bind("surveyResponseId", surveyResponseId)
                        .bind("responseTime", responseTime)
                        .execute()
        );
    }

    public void changeTaskCompleteTime(UUID taskId, Instant completionTime) {
        jdbi.withHandle(handle ->
                handle.createUpdate("update participant_task set completed_at = :completionTime where id = :taskId;")
                        .bind("taskId", taskId)
                        .bind("completionTime", completionTime)
                        .execute()
        );
    }

    /**
     * update both the creation and the lastUpdatedAt times to the given time
     */
    public void changeParticipantNoteTime(UUID noteId, Instant creationTime) {
        jdbi.withHandle(handle ->
                handle.createUpdate("update participant_note set created_at = :creationTime, last_updated_at = :creationTime where id = :noteId;")
                        .bind("noteId", noteId)
                        .bind("creationTime", creationTime)
                        .execute()
        );
    }

    public void changeKitCreationTime(UUID kitRequestId, Instant creationTime) {
        jdbi.withHandle(handle ->
                handle.createUpdate("update kit_request set created_at = :creationTime where id = :kitRequestId;")
                        .bind("kitRequestId", kitRequestId)
                        .bind("creationTime", creationTime)
                        .execute()
        );
    }

    public void changeAdminTaskCreationTime(UUID adminTaskId, Instant creationTime) {
        jdbi.withHandle(handle ->
                handle.createUpdate("update admin_task set created_at = :creationTime where id = :adminTaskId;")
                        .bind("adminTaskId", adminTaskId)
                        .bind("creationTime", creationTime)
                        .execute()
        );
    }

    public void changeParticipantAccountCreationTime(UUID participantId, Instant creationTime) {
        jdbi.withHandle(handle ->
                handle.createUpdate("update participant_user set created_at = :creationTime where id = :participantId;")
                        .bind("participantId", participantId)
                        .bind("creationTime", creationTime)
                        .execute()
        );
    }

    public void changePortalEnvChangeCreationTime(UUID changeRecordId, Instant creationTime) {
        jdbi.withHandle(handle ->
                handle.createUpdate("update portal_environment_change_record set created_at = :creationTime where id = :changeRecordId;")
                        .bind("changeRecordId", changeRecordId)
                        .bind("creationTime", creationTime)
                        .execute()
        );
    }

}
