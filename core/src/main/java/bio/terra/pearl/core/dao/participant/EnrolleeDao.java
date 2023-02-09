package bio.terra.pearl.core.dao.participant;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.dao.consent.ConsentResponseDao;
import bio.terra.pearl.core.dao.survey.PreEnrollmentResponseDao;
import bio.terra.pearl.core.dao.survey.SurveyResponseDao;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.EnrolleeSearchResult;
import bio.terra.pearl.core.model.participant.Profile;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class EnrolleeDao extends BaseMutableJdbiDao<Enrollee> {
    private ProfileDao profileDao;
    private SurveyResponseDao surveyResponseDao;
    private ConsentResponseDao consentResponseDao;
    private ParticipantTaskDao participantTaskDao;
    private PreEnrollmentResponseDao preEnrollmentResponseDao;

    public EnrolleeDao(Jdbi jdbi, ProfileDao profileDao, SurveyResponseDao surveyResponseDao,
                       ConsentResponseDao consentResponseDao, ParticipantTaskDao participantTaskDao,
                       PreEnrollmentResponseDao preEnrollmentResponseDao) {
        super(jdbi);
        this.profileDao = profileDao;
        this.surveyResponseDao = surveyResponseDao;
        this.consentResponseDao = consentResponseDao;
        this.participantTaskDao = participantTaskDao;
        this.preEnrollmentResponseDao = preEnrollmentResponseDao;
    }

    @Override
    protected Class<Enrollee> getClazz() {
        return Enrollee.class;
    }

    public Optional<Enrollee> findOneByShortcode(String shortcode) {
        return findByProperty("shortcode", shortcode);
    }

    public List<Enrollee> findByStudyEnvironmentAdminLoad(UUID studyEnvironmentId) {
        return findAllByProperty("study_environment_id", studyEnvironmentId);
    }


    public List<Enrollee> findByParticipantUserId(UUID userId) {
        return findAllByProperty("participant_user_id", userId);
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

    public Optional<Enrollee> findByStudyEnvironmentAdminLoad(UUID studyEnvironmentId, String shortcode) {
        Optional<Enrollee> enrolleeOpt = findByTwoProperties("study_environment_id", studyEnvironmentId,
                "shortcode", shortcode);
        enrolleeOpt.ifPresent(enrollee -> {
            enrollee.getSurveyResponses().addAll(surveyResponseDao.findByEnrolleeIdWithLastSnapshot(enrollee.getId()));
            enrollee.getConsentResponses().addAll(consentResponseDao.findByEnrolleeId(enrollee.getId()));
            enrollee.setProfile(profileDao.find(enrollee.getProfileId()).get());
            enrollee.getParticipantTasks().addAll(participantTaskDao.findByEnrolleeId(enrollee.getId()));
            if (enrollee.getPreEnrollmentResponseId() != null) {
                enrollee.setPreEnrollmentResponse(preEnrollmentResponseDao.find(enrollee.getPreEnrollmentResponseId()).get());
            }
        });
        return enrolleeOpt;
    }

    public List<EnrolleeSearchResult> searchByStudyEnvironment(UUID studyEnvironmentId) {
        List<Enrollee> enrollees = findByStudyEnvironmentAdminLoad(studyEnvironmentId);
        List<UUID> profileIds = enrollees.stream().map(enrollee -> enrollee.getProfileId()).toList();
        List<Profile> profiles = profileDao.findAll(profileIds);
        return enrollees.stream().map(enrollee -> EnrolleeSearchResult.builder().enrollee(enrollee)
                .profile(profiles.stream().filter(profile -> profile.getId().equals(enrollee.getProfileId()))
                        .findFirst().orElse(null))
                .build()).toList();
    }

    public int countByStudyEnvironment(UUID studyEnvironmentId) {
        return countByProperty("study_environment_id", studyEnvironmentId);
    }
    public void deleteByStudyEnvironmentId(UUID studyEnvironmentId) {
        deleteByUuidProperty("study_environment_id", studyEnvironmentId);
    }

    /** updates the global consent status of the enrollee */
    public void updateConsented(UUID enrolleeId, boolean consented) {
        updateProperty(enrolleeId, "consented", consented);
    }
}
