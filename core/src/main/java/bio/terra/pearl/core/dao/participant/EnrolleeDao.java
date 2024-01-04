package bio.terra.pearl.core.dao.participant;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.dao.consent.ConsentResponseDao;
import bio.terra.pearl.core.dao.kit.KitRequestDao;
import bio.terra.pearl.core.dao.kit.KitTypeDao;
import bio.terra.pearl.core.dao.survey.PreEnrollmentResponseDao;
import bio.terra.pearl.core.dao.survey.SurveyResponseDao;
import bio.terra.pearl.core.dao.workflow.ParticipantTaskDao;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.participant.Enrollee;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jdbi.v3.core.Jdbi;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class EnrolleeDao extends BaseMutableJdbiDao<Enrollee> {
    private final ConsentResponseDao consentResponseDao;
    private final KitRequestDao kitRequestDao;
    private final KitTypeDao kitTypeDao;
    private final ParticipantTaskDao participantTaskDao;
    private final PreEnrollmentResponseDao preEnrollmentResponseDao;
    private final ProfileDao profileDao;
    private final SurveyResponseDao surveyResponseDao;
    private final ParticipantNoteDao participantNoteDao;

    public EnrolleeDao(Jdbi jdbi,
                       ConsentResponseDao consentResponseDao,
                       KitRequestDao kitRequestDao,
                       KitTypeDao kitTypeDao,
                       ParticipantTaskDao participantTaskDao,
                       PreEnrollmentResponseDao preEnrollmentResponseDao,
                       ProfileDao profileDao,
                       SurveyResponseDao surveyResponseDao,
                       ParticipantNoteDao participantNoteDao) {
        super(jdbi);
        this.consentResponseDao = consentResponseDao;
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

    public List<Enrollee> findByStudyEnvironmentId(UUID studyEnvironmentId) {
        return findAllByProperty("study_environment_id", studyEnvironmentId);
    }

    public List<Enrollee> findByStudyEnvironmentId(UUID studyEnvironmentId, String sortProperty, String sortDir) {
        return findAllByPropertySorted("study_environment_id", studyEnvironmentId,
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

    public Optional<Enrollee> findByEnrolleeId(UUID userId, String enrolleeShortcode) {
        return findByTwoProperties("participant_user_id", userId, "shortcode", enrolleeShortcode);
    }

    public Optional<Enrollee> findByPreEnrollResponseId(UUID preEnrollResponseId) {
        return findByProperty("pre_enrollment_response_id", preEnrollResponseId);
    }

    /**
     * loads child relationships including survey responses, profile, etc...
     * This currently makes ~10 separate DB queries, all of which should be individually quite quick.
     * If this grows much more, it might be worth parallelizing or batching the DB fetches.
     *
     * because the individual queries are returning relatively small amounts of data (likely <10KB each) splitting these
     * into separate API calls and incurring the overhead of separate DB auth queries for each request
     * would probably result in much worse performance.
     *
     * That said, if this grows too much larger, it might make sense to group it into two different
     * batches.
     * */
    public Enrollee loadForAdminView(Enrollee enrollee) {
        enrollee.getSurveyResponses().addAll(surveyResponseDao.findByEnrolleeIdWithAnswers(enrollee.getId()));
        enrollee.getConsentResponses().addAll(consentResponseDao.findByEnrolleeId(enrollee.getId()));
        if (enrollee.getPreEnrollmentResponseId() != null) {
            enrollee.setPreEnrollmentResponse(preEnrollmentResponseDao.find(enrollee.getPreEnrollmentResponseId()).get());
        }
        enrollee.getParticipantNotes().addAll(participantNoteDao.findByEnrollee(enrollee.getId()));
        return loadForParticipantDashboard(enrollee);
    }

    /**
     * Load enrollee tasks, profile, and kit requests
     * (See loadForAdminView description for performance information)
     */
    public Enrollee loadForParticipantDashboard(Enrollee enrollee) {
        enrollee.getParticipantTasks().addAll(participantTaskDao.findByEnrolleeId(enrollee.getId()));
        enrollee.setProfile(profileDao.loadWithMailingAddress(enrollee.getProfileId()).orElse(null));
        enrollee.getKitRequests().addAll(kitRequestDao.findByEnrollee(enrollee.getId()));
        var allKitTypes = kitTypeDao.findAll();
        for (KitRequest kitRequest : enrollee.getKitRequests()) {
            var kitType = allKitTypes.stream().filter((t -> t.getId().equals(kitRequest.getKitTypeId()))).findFirst()
                    .orElseThrow(() -> new IllegalStateException("Invalid kit type ID for KitRequest " + kitRequest.getKitTypeId()));
            kitRequest.setKitType(kitType);
        }
        return enrollee;
    }

    /**
     * Fetches enrollees, loading all details needed for the kit management view -- currently tasks and kits.
     * Reduces database round-trips by fetching entities from each table and performing in-memory joins.
     * Uses Streams to reduce the number of iterations over collections of entities:
     *  - Streams enrollees into two lists: enrollees and enrollee IDs
     *    - avoids separately collecting IDs from entities
     *    - retains order of results (not otherwise guaranteed when using something like Collectors.toMap())
     *  - Streams tasks and kits into maps grouped by enrollee ID
     *    - avoids separate iteration to build these maps
     *  All that remains is a single traversal through the enrollee list to attach their tasks and kits.
     */
    @Transactional
    public List<Enrollee> findForKitManagement(UUID studyEnvironmentId) {
        var enrolleesAndIds = streamByStudyEnvironmentId(studyEnvironmentId).collect(Collectors.teeing(
                Collectors.toList(),
                Collectors.mapping(Enrollee::getId, Collectors.toList()),
                Pair::of
        ));

        var enrollees = enrolleesAndIds.getFirst();
        var enrolleeIds = enrolleesAndIds.getSecond();

        var tasksByEnrolleeId = participantTaskDao.findByEnrolleeIds(enrolleeIds);
        var kitsByEnrolleeId = kitRequestDao.findByEnrolleeIds(enrolleeIds);

        enrollees.forEach(enrollee -> {
            // Be sure to set empty collections to indicate that they are empty instead of not initialized
            enrollee.setParticipantTasks(tasksByEnrolleeId.getOrDefault(enrollee.getId(), Collections.emptySet()));
            enrollee.setKitRequests(kitsByEnrolleeId.getOrDefault(enrollee.getId(), Collections.emptyList()));
        });

        return enrollees;
    }

    public int countByStudyEnvironment(UUID studyEnvironmentId) {
        return countByProperty("study_environment_id", studyEnvironmentId);
    }
    public void deleteByStudyEnvironmentId(UUID studyEnvironmentId) {
        deleteByProperty("study_environment_id", studyEnvironmentId);
    }

    /** updates the global consent status of the enrollee */
    public void updateConsented(UUID enrolleeId, boolean consented) {
        updateProperty(enrolleeId, "consented", consented);
    }
}
