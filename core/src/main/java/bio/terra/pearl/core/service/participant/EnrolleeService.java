package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.dao.participant.EnrolleeDao;
import bio.terra.pearl.core.dao.survey.PreEnrollmentResponseDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.consent.ConsentResponse;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.EnrolleeSearchResult;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.SurveyResponse;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.CrudService;
import bio.terra.pearl.core.service.consent.ConsentResponseService;
import bio.terra.pearl.core.service.notification.NotificationService;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.study.PortalStudyService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.survey.SurveyResponseService;
import bio.terra.pearl.core.service.workflow.DataChangeRecordService;
import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnrolleeService extends CrudService<Enrollee, EnrolleeDao> {
    public static final String PARTICIPANT_SHORTCODE_ALLOWED_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final int PARTICIPANT_SHORTCODE_LENGTH = 6;
    private SurveyResponseService surveyResponseService;
    private ParticipantTaskService participantTaskService;
    private StudyEnvironmentService studyEnvironmentService;
    private ConsentResponseService consentResponseService;
    private PreEnrollmentResponseDao preEnrollmentResponseDao;
    private NotificationService notificationService;
    private PortalStudyService portalStudyService;
    private PortalService portalService;
    private DataChangeRecordService dataChangeRecordService;
    private WithdrawnEnrolleeService withdrawnEnrolleeService;
    private ParticipantUserService participantUserService;


    private SecureRandom secureRandom;

    public EnrolleeService(EnrolleeDao enrolleeDao,
                           @Lazy SurveyResponseService surveyResponseService,
                           ParticipantTaskService participantTaskService,
                           @Lazy StudyEnvironmentService studyEnvironmentService,
                           ConsentResponseService consentResponseService,
                           PreEnrollmentResponseDao preEnrollmentResponseDao,
                           NotificationService notificationService, PortalStudyService portalStudyService,
                           @Lazy PortalService portalService,
                           @Lazy DataChangeRecordService dataChangeRecordService,
                           @Lazy WithdrawnEnrolleeService withdrawnEnrolleeService,
                           @Lazy ParticipantUserService participantUserService,
                           SecureRandom secureRandom) {
        super(enrolleeDao);
        this.surveyResponseService = surveyResponseService;
        this.participantTaskService = participantTaskService;
        this.studyEnvironmentService = studyEnvironmentService;
        this.consentResponseService = consentResponseService;
        this.preEnrollmentResponseDao = preEnrollmentResponseDao;
        this.notificationService = notificationService;
        this.portalStudyService = portalStudyService;
        this.portalService = portalService;
        this.dataChangeRecordService = dataChangeRecordService;
        this.withdrawnEnrolleeService = withdrawnEnrolleeService;
        this.participantUserService = participantUserService;
        this.secureRandom = secureRandom;
    }

    public Optional<Enrollee> findOneByShortcode(String shortcode) {
        return dao.findOneByShortcode(shortcode);
    }

    public Optional<Enrollee> findByEnrolleeId(UUID participantUserId, String enrolleeShortcode) {
        return dao.findByEnrolleeId(participantUserId, enrolleeShortcode);
    }
    public List<Enrollee> findByPortalParticipantUser(PortalParticipantUser ppUser) {
        return dao.findByProfileId(ppUser.getProfileId());
    }

    public List<Enrollee> findByStudyEnvironment(UUID studyEnvironmentId) {
        return dao.findByStudyEnvironmentId(studyEnvironmentId);
    }

    public Enrollee loadForAdminView(Enrollee enrollee) {
        return dao.loadForAdminView(enrollee);
    }


    public List<EnrolleeSearchResult> search(String studyShortcode, EnvironmentName envName) {
        StudyEnvironment studyEnv = studyEnvironmentService.findByStudy(studyShortcode, envName).get();
        return dao.searchByStudyEnvironment(studyEnv.getId());
    }

    public Optional<Enrollee> findByPreEnrollResponseId(UUID preEnrollResponseId) {
        return dao.findByPreEnrollResponseId(preEnrollResponseId);
    }

    public int countByStudyEnvironmentId(UUID studyEnvironmentId) {
        return dao.countByStudyEnvironment(studyEnvironmentId);
    }

    @Override
    @Transactional
    public void delete(UUID enrolleeId, Set<CascadeProperty> cascades) {
        Enrollee enrollee = dao.find(enrolleeId).get();
        StudyEnvironment studyEnv = studyEnvironmentService.find(enrollee.getStudyEnvironmentId()).get();
        /**
         * For production environments, we only allow deletion if a withdrawal record has already been preserved
         */
        if (studyEnv.getEnvironmentName().equals(EnvironmentName.live) &&
            !withdrawnEnrolleeService.isWithdrawn(enrollee.getShortcode())) {
            throw new UnsupportedOperationException("Cannot delete live, non-withdrawn participants");
        }
        participantTaskService.deleteByEnrolleeId(enrolleeId);
        for (SurveyResponse surveyResponse : surveyResponseService.findByEnrolleeId(enrolleeId)) {
            surveyResponseService.delete(surveyResponse.getId(), cascades);
        }
        for (ConsentResponse consentResponse : consentResponseService.findByEnrolleeId(enrolleeId)) {
            consentResponseService.delete(consentResponse.getId(), cascades);
        }
        notificationService.deleteByEnrolleeId(enrolleeId);
        dataChangeRecordService.deleteByEnrolleeId(enrolleeId);
        dao.delete(enrolleeId);
        if (enrollee.getPreEnrollmentResponseId() != null) {
            preEnrollmentResponseDao.delete(enrollee.getPreEnrollmentResponseId());
        }
        if (cascades.contains(AllowedCascades.PARTICIPANT_USER)) {
            participantUserService.delete(enrollee.getParticipantUserId(), CascadeProperty.EMPTY_SET);
        }
    }

    @Transactional
    public void deleteByStudyEnvironmentId(UUID studyEnvironmentId, Set<CascadeProperty> cascade) {
        for (Enrollee enrollee : dao.findByStudyEnvironmentId(studyEnvironmentId)) {
            delete(enrollee.getId(), cascade);
        }
    }

    @Transactional
    public Enrollee create(Enrollee enrollee) {
        if (enrollee.getShortcode() == null) {
            enrollee.setShortcode(generateShortcode());
        }
        Enrollee savedEnrollee = dao.create(enrollee);
        logger.info("Enrollee created.  id: {}, shortcode: {}, participantUserId", savedEnrollee.getId(),
                savedEnrollee.getShortcode(), savedEnrollee.getParticipantUserId());
        return savedEnrollee;
    }

    @Transactional
    public void updateConsented(UUID enrolleeId, boolean consented) {
        dao.updateConsented(enrolleeId, consented);
        logger.info("Updated enrollee consent status: enrollee: {}, consented {}", enrolleeId, consented);
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

    public enum AllowedCascades implements CascadeProperty {
        PARTICIPANT_USER
    }
}
