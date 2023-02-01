package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.dao.participant.EnrolleeDao;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.survey.SurveyResponse;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.CrudService;
import bio.terra.pearl.core.service.survey.SurveyResponseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class EnrolleeService extends CrudService<Enrollee, EnrolleeDao> {
    public static final String PARTICIPANT_SHORTCODE_ALLOWED_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final int PARTICIPANT_SHORTCODE_LENGTH = 6;
    private SurveyResponseService surveyResponseService;

    private SecureRandom secureRandom;

    public EnrolleeService(EnrolleeDao enrolleeDao, SurveyResponseService surveyResponseService,
                           SecureRandom secureRandom) {
        super(enrolleeDao);
        this.surveyResponseService = surveyResponseService;
        this.secureRandom = secureRandom;
    }

    public Optional<Enrollee> findOneByShortcode(String shortcode) {
        return dao.findOneByShortcode(shortcode);
    }

    public List<Enrollee> findByParticipantUserId(UUID userId) {
        return dao.findByParticipantUserId(userId);
    }

    @Override
    @Transactional
    public void delete(UUID enrolleeId, Set<CascadeProperty> cascades) {
        for (SurveyResponse surveyResponse : surveyResponseService.findByEnrolleeId(enrolleeId)) {
            surveyResponseService.delete(surveyResponse.getId(), cascades);
        }
        dao.delete(enrolleeId);
    }

    @Transactional
    public void deleteByStudyEnvironmentId(UUID studyEnvironmentId, Set<CascadeProperty> cascade) {
        for (Enrollee enrollee : dao.findByStudyEnvironment(studyEnvironmentId)) {
            delete(enrollee.getId(), cascade);
        }
    }

    public List<Enrollee> findByStudyEnvironment(UUID studyEnvironmentId) {
        return dao.findByStudyEnvironment(studyEnvironmentId);
    }

    public int countByStudyEnvironmentId(UUID studyEnvironmentId) {
        return dao.countByStudyEnvironment(studyEnvironmentId);
    }

    @Transactional
    public Enrollee create(Enrollee enrollee) {
        if (enrollee.getShortcode() == null) {
            enrollee.setShortcode(generateShortcode());
        }
        return super.create(enrollee);
    }

    /** It's possible there are snazzier ways to get postgres to generate this for us,
     * but for now, just keep trying strings until we get a unique one
     * returns null if we couldn't generate one.
     */
    @Transactional
    public String generateShortcode() {
        int MAX_TRIES = 10;
        String shortcode = null;
        for (int tryNum = 0; tryNum < MAX_TRIES; tryNum++) {
            String possibleShortcode = secureRandom
                    .ints(PARTICIPANT_SHORTCODE_LENGTH, 0, PARTICIPANT_SHORTCODE_ALLOWED_CHARS.length())
                    .mapToObj(i -> PARTICIPANT_SHORTCODE_ALLOWED_CHARS.charAt(i))
                    .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();
            if (dao.findOneByShortcode(possibleShortcode).isEmpty()) {
                shortcode = possibleShortcode;
                break;
            }
        }
        if (shortcode == null) {
            throw new RuntimeException("Unable to generate unique shortcode");
        }
        return shortcode;
    }
}
