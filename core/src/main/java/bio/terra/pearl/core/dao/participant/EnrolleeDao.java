package bio.terra.pearl.core.dao.participant;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.dao.StudyEnvAttachedDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.Enrollee;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class EnrolleeDao extends BaseMutableJdbiDao<Enrollee> implements StudyEnvAttachedDao<Enrollee> {

    public EnrolleeDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<Enrollee> getClazz() {
        return Enrollee.class;
    }

    public Optional<Enrollee> findOneByShortcode(String shortcode) {
        return findByProperty("shortcode", shortcode);
    }

    public Optional<Enrollee> findOneByResearchId(String researchId) {
        return findByProperty("research_id", researchId);
    }

    public Optional<Enrollee> findOneByResearchIdInStudyEnv(String researchId, UUID studyEnvId) {
        return findByTwoProperties("research_id", researchId, "study_environment_id", studyEnvId);
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

    public void updateResearchId(UUID enrolleeId, String researchId) {
        updateProperty(enrolleeId, "research_id", researchId);
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

    public List<Enrollee> findAssignedToTask(UUID studyEnvironmentId,
                                             String targetStableId) {
        return jdbi.withHandle(handle -> handle.createQuery("""
                            select distinct on (e.id) e.* from enrollee e
                            inner join participant_task pt
                            on (e.id = pt.enrollee_id
                                 and pt.target_stable_id = :targetStableId
                                 and pt.status not in ('REMOVED', 'REJECTED')
                            )
                             where e.study_environment_id = :studyEnvironmentId
                             order by e.id
                        """)
                .bind("targetStableId", targetStableId)
                .bind("studyEnvironmentId", studyEnvironmentId)
                .mapTo(clazz)
                .list());
    }

}
