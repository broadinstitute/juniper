package bio.terra.pearl.core.dao.participant;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.dao.consent.ConsentResponseDao;
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
public class EnrolleeDao extends BaseJdbiDao<Enrollee> {
    private ProfileDao profileDao;
    private SurveyResponseDao surveyResponseDao;
    private ConsentResponseDao consentResponseDao;
    private ParticipantTaskDao participantTaskDao;

    public EnrolleeDao(Jdbi jdbi, ProfileDao profileDao, SurveyResponseDao surveyResponseDao,
                       ConsentResponseDao consentResponseDao, ParticipantTaskDao participantTaskDao) {
        super(jdbi);
        this.profileDao = profileDao;
        this.surveyResponseDao = surveyResponseDao;
        this.consentResponseDao = consentResponseDao;
        this.participantTaskDao = participantTaskDao;
    }

    @Override
    protected Class<Enrollee> getClazz() {
        return Enrollee.class;
    }

    public Optional<Enrollee> findOneByShortcode(String shortcode) {
        return findByProperty("shortcode", shortcode);
    }

    public List<Enrollee> findByStudyEnvironment(UUID studyEnvironmentId) {
        return findAllByProperty("study_environment_id", studyEnvironmentId);
    }


    public List<Enrollee> findByParticipantUserId(UUID userId) {
        return findAllByProperty("participant_user_id", userId);
    }

    public Optional<Enrollee> findByParticipantUserId(UUID userId, UUID studyEnvironmentId) {
        return findByTwoProperties("participant_user_id", userId,
                "study_environment_id", studyEnvironmentId);
    }

    public Optional<Enrollee> findByStudyEnvironment(UUID studyEnvironmentId, String shortcode) {
        Optional<Enrollee> enrolleeOpt = findByTwoProperties("study_environment_id", studyEnvironmentId,
                "shortcode", shortcode);
        enrolleeOpt.ifPresent(enrollee -> {
            enrollee.getSurveyResponses().addAll(surveyResponseDao.findByEnrolleeIdWithLastSnapshot(enrollee.getId()));
            enrollee.getConsentResponses().addAll(consentResponseDao.findByEnrolleeId(enrollee.getId()));
            enrollee.setProfile(profileDao.find(enrollee.getProfileId()).get());
            enrollee.getParticipantTasks().addAll(participantTaskDao.findByEnrolleeId(enrollee.getId()));
        });
        return enrolleeOpt;
    }

    public List<EnrolleeSearchResult> searchByStudyEnvironment(UUID studyEnvironmentId) {
        List<Enrollee> enrollees = findByStudyEnvironment(studyEnvironmentId);
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
}
