package bio.terra.pearl.core.dao.participant;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.dao.kit.KitRequestDao;
import bio.terra.pearl.core.dao.kit.KitTypeDao;
import bio.terra.pearl.core.dao.StudyEnvAttachedDao;
import bio.terra.pearl.core.dao.survey.PreEnrollmentResponseDao;
import bio.terra.pearl.core.dao.survey.SurveyResponseDao;
import bio.terra.pearl.core.dao.workflow.ParticipantTaskDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.Enrollee;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Component
public class EnrolleeDao extends BaseMutableJdbiDao<Enrollee> implements StudyEnvAttachedDao<Enrollee> {
    private final KitRequestDao kitRequestDao;
    private final KitTypeDao kitTypeDao;
    private final ParticipantTaskDao participantTaskDao;
    private final PreEnrollmentResponseDao preEnrollmentResponseDao;
    private final ProfileDao profileDao;
    private final SurveyResponseDao surveyResponseDao;
    private final ParticipantNoteDao participantNoteDao;

    public EnrolleeDao(Jdbi jdbi,
                       KitRequestDao kitRequestDao,
                       KitTypeDao kitTypeDao,
                       ParticipantTaskDao participantTaskDao,
                       PreEnrollmentResponseDao preEnrollmentResponseDao,
                       ProfileDao profileDao,
                       SurveyResponseDao surveyResponseDao,
                       ParticipantNoteDao participantNoteDao) {
        super(jdbi);
        this.kitRequestDao = kitRequestDao;
        this.kitTypeDao = kitTypeDao;
        this.participantTaskDao = participantTaskDao;
        this.preEnrollmentResponseDao = preEnrollmentResponseDao;
        this.profileDao = profileDao;
        this.surveyResponseDao = surveyResponseDao;
        this.participantNoteDao = participantNoteDao;
    }

    @Override
    protected Class<Enrollee> getClazz() {
        return Enrollee.class;
    }

    public Optional<Enrollee> findOneByShortcode(String shortcode) {
        return findByProperty("shortcode", shortcode);
    }

    public List<Enrollee> findByStudyEnvironmentId(UUID studyEnvironmentId, Boolean isSubject, String sortProperty, String sortDir) {
        if (isSubject == null) {
            return findAllByPropertySorted("study_environment_id", studyEnvironmentId,
                    sortProperty, sortDir);
        }
        return findAllByTwoPropertiesSorted("study_environment_id", studyEnvironmentId,
                "subject", isSubject,
                sortProperty, sortDir);
    }

    @Transactional
    public Stream<Enrollee> streamByStudyEnvironmentId(UUID studyEnvironmentId) {
        return streamAllByProperty("study_environment_id", studyEnvironmentId);
    }

    public List<Enrollee> findAllByShortcodes(List<String> shortcodes) {
        return findAllByPropertyCollection("shortcode", shortcodes);
    }

    public List<Enrollee> findByParticipantUserId(UUID userId) {
        return findAllByProperty("participant_user_id", userId);
    }

    public List<Enrollee> findByProfileId(UUID profileId) {
        return findAllByProperty("profile_id", profileId);
    }

    public Optional<Enrollee> findByParticipantUserId(UUID userId, UUID studyEnvironmentId) {
        return findByTwoProperties("participant_user_id", userId,
                "study_environment_id", studyEnvironmentId);
    }

    public Optional<Enrollee> findByEnrolleeId(UUID userId, UUID enrolleeId) {
        return findByTwoProperties("participant_user_id", userId, "id", enrolleeId);
    }

    public Optional<Enrollee> findByParticipantUserIdAndShortcode(UUID userId, String enrolleeShortcode) {
        return findByTwoProperties("participant_user_id", userId, "shortcode", enrolleeShortcode);
    }

    public Optional<Enrollee> findByPreEnrollResponseId(UUID preEnrollResponseId) {
        return findByProperty("pre_enrollment_response_id", preEnrollResponseId);
    }

    /** updates the global consent status of the enrollee */
    public void updateConsented(UUID enrolleeId, boolean consented) {
        updateProperty(enrolleeId, "consented", consented);
    }

    public Optional<Enrollee> findByParticipantUserIdAndStudyEnvId(UUID participantUserId, UUID studyEnvId) {
        return findByTwoProperties("participant_user_id", participantUserId, "study_environment_id", studyEnvId);
    }

    public List<Enrollee> findAllByFamilyId(UUID id) {
        return jdbi.withHandle(handle -> handle.createQuery("""
                        SELECT enrollee.* FROM enrollee enrollee
                        INNER JOIN family_enrollee family_enrollee ON enrollee.id = family_enrollee.enrollee_id
                        WHERE family_enrollee.family_id = :id
                        """)
                .bind("id", id)
                .mapTo(clazz)
                .list());
    }


    /** returns all the enrollees in the given portal and environment */
    public List<Enrollee> findAllByPortalEnv(UUID portalId, EnvironmentName environmentName) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                        select e.* from enrollee e
                        where e.study_environment_id in (
                            select se.id from study_environment se
                            where se.study_id in (
                                select study_id from portal_study
                                where portal_id = :portalId
                            ) and se.environment_name = :environmentName
                        )
                        """)
                        .bind("portalId", portalId)
                        .bind("environmentName", environmentName)
                        .mapTo(clazz)
                        .list()
        );
    }

    public Optional<Enrollee> findByShortcodeAndStudyEnvId(String enrolleeShortcode, UUID studyEnvId) {
        return findByTwoProperties("shortcode", enrolleeShortcode, "study_environment_id", studyEnvId);
    }


    public List<Enrollee> findUnassignedToTask(UUID studyEnvironmentId,
                                               String targetStableId,
                                               Integer targetAssignedVersion) {

        return jdbi.withHandle(handle -> {
            String versionWhereClause = "";
            if (targetAssignedVersion != null) {
                versionWhereClause = " and target_assigned_version = :targetAssignedVersion";
            }
            Query query = handle.createQuery("""
                            select enrollee.* from enrollee  
                            left join participant_task 
                            on (enrollee.id = participant_task.enrollee_id 
                                 and participant_task.target_stable_id = :targetStableId
                                 %s
                                 )              
                             where enrollee.study_environment_id = :studyEnvironmentId                         
                             and participant_task.id IS NULL                                                                   
                        """.formatted(versionWhereClause))
                    .bind("targetStableId", targetStableId)
                    .bind("studyEnvironmentId", studyEnvironmentId);
            if (targetAssignedVersion != null) {
                query = query.bind("targetAssignedVersion", targetAssignedVersion);
            }
            return query.mapTo(clazz).list();
        });
    }


    /** returns enrollees who were assigned a tasks with the given target stable id in the past */
    public List<Enrollee> findWithTaskInPast(UUID studyEnvironmentId,
                                              String taskTargetStableId,
                                              Duration minTimeSinceMostRecent) {
        Instant minTimeSinceMostRecentInstant = Instant.now().minus(minTimeSinceMostRecent);
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                        with enrollee_times as (select enrollee_id as task_enrollee_id, MAX(created_at) as most_recent_task_time
                          from participant_task where target_stable_id = :taskTargetStableId group by enrollee_id)
                        select enrollee.* from enrollee 
                        join enrollee_times on enrollee.id = task_enrollee_id
                        where study_environment_id = :studyEnvironmentId
                        and most_recent_task_time < :minTimeSinceCreationInstant
                        """)
                        .bind("studyEnvironmentId", studyEnvironmentId)
                        .bind("taskTargetStableId", taskTargetStableId)
                        .bind("minTimeSinceCreationInstant", minTimeSinceMostRecentInstant)
                        .mapTo(clazz)
                        .list()
        );
    }

}
