package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.dao.participant.EnrolleeDao;
import bio.terra.pearl.core.dao.survey.PreEnrollmentResponseDao;
import bio.terra.pearl.core.dao.survey.SurveyResponseDao;
import bio.terra.pearl.core.dao.workflow.ParticipantTaskDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.SurveyResponse;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.CrudService;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.file.ParticipantFileService;
import bio.terra.pearl.core.service.kit.KitRequestService;
import bio.terra.pearl.core.service.notification.NotificationService;
import bio.terra.pearl.core.service.portal.MailingListContactService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.survey.SurveyResponseService;
import bio.terra.pearl.core.service.workflow.ParticipantDataChangeService;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class EnrolleeService extends CrudService<Enrollee, EnrolleeDao> {
    private final ParticipantTaskDao participantTaskDao;
    private final SurveyResponseDao surveyResponseDao;
    private final ProfileService profileService;
    private final SurveyResponseService surveyResponseService;
    private final ParticipantTaskService participantTaskService;
    private final StudyEnvironmentService studyEnvironmentService;
    private final PreEnrollmentResponseDao preEnrollmentResponseDao;
    private final NotificationService notificationService;
    private final ParticipantDataChangeService participantDataChangeService;
    private final WithdrawnEnrolleeService withdrawnEnrolleeService;
    private final ParticipantUserService participantUserService;
    private final PortalParticipantUserService portalParticipantUserService;
    private final ParticipantNoteService participantNoteService;
    private final KitRequestService kitRequestService;
    private final SecureRandom secureRandom;
    private final RandomUtilService randomUtilService;
    private final EnrolleeRelationService enrolleeRelationService;
    private final FamilyService familyService;
    private final ShortcodeService shortcodeService;
    private final FamilyEnrolleeService familyEnrolleeService;
    private final ParticipantFileService participantFileService;
    private final MailingListContactService mailingListContactService;

    public EnrolleeService(EnrolleeDao enrolleeDao,
                           SurveyResponseDao surveyResponseDao,
                           ParticipantTaskDao participantTaskDao,
                           ProfileService profileService,
                           @Lazy SurveyResponseService surveyResponseService,
                           ParticipantTaskService participantTaskService,
                           @Lazy StudyEnvironmentService studyEnvironmentService,
                           PreEnrollmentResponseDao preEnrollmentResponseDao,
                           NotificationService notificationService,
                           @Lazy ParticipantDataChangeService participantDataChangeService,
                           @Lazy WithdrawnEnrolleeService withdrawnEnrolleeService,
                           @Lazy ParticipantUserService participantUserService,
                           ParticipantNoteService participantNoteService,
                           KitRequestService kitRequestService,
                           SecureRandom secureRandom,
                           RandomUtilService randomUtilService,
                           EnrolleeRelationService enrolleeRelationService,
                           @Lazy PortalParticipantUserService portalParticipantUserService,
                           FamilyService familyService, ShortcodeService shortcodeService, FamilyEnrolleeService familyEnrolleeService, ParticipantFileService participantFileService, MailingListContactService mailingListContactService) {
        super(enrolleeDao);
        this.surveyResponseDao = surveyResponseDao;
        this.participantTaskDao = participantTaskDao;
        this.profileService = profileService;
        this.surveyResponseService = surveyResponseService;
        this.participantTaskService = participantTaskService;
        this.studyEnvironmentService = studyEnvironmentService;
        this.preEnrollmentResponseDao = preEnrollmentResponseDao;
        this.notificationService = notificationService;
        this.participantDataChangeService = participantDataChangeService;
        this.withdrawnEnrolleeService = withdrawnEnrolleeService;
        this.participantUserService = participantUserService;
        this.participantNoteService = participantNoteService;
        this.kitRequestService = kitRequestService;
        this.secureRandom = secureRandom;
        this.randomUtilService = randomUtilService;
        this.enrolleeRelationService = enrolleeRelationService;
        this.portalParticipantUserService = portalParticipantUserService;
        this.familyService = familyService;
        this.shortcodeService = shortcodeService;
        this.familyEnrolleeService = familyEnrolleeService;
        this.participantFileService = participantFileService;
        this.mailingListContactService = mailingListContactService;
    }

    public Optional<Enrollee> findOneByShortcode(String shortcode) {
        return dao.findOneByShortcode(shortcode);
    }
    public Optional<Enrollee> findByParticipantUserIdAndShortcode(UUID participantUserId, String enrolleeShortcode) {
        return dao.findByParticipantUserIdAndShortcode(participantUserId, enrolleeShortcode);
    }
    public List<Enrollee> findByPortalParticipantUser(PortalParticipantUser ppUser) {
        return dao.findByProfileId(ppUser.getProfileId());
    }

    public List<Enrollee> findAllByShortcodes(List<String> shortcodes) {
        return dao.findAllByShortcodes(shortcodes);
    }

    public List<Enrollee> findByStudyEnvironment(UUID studyEnvironmentId) {
        return dao.findByStudyEnvironmentId(studyEnvironmentId);
    }

    public List<Enrollee> findByStudyEnvironment(UUID studyEnvironmentId, Boolean isSubject, String sortProperty, String sortDir) {
        return dao.findByStudyEnvironmentId(studyEnvironmentId, isSubject, sortProperty, sortDir);
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
        if (enrollee.getPreEnrollmentResponseId() != null) {
            enrollee.setPreEnrollmentResponse(preEnrollmentResponseDao.find(enrollee.getPreEnrollmentResponseId()).orElseThrow());
        }
        enrollee.getParticipantNotes().addAll(participantNoteService.findByEnrollee(enrollee.getId()));
        enrollee.getParticipantTasks().addAll(participantTaskService.findByEnrolleeId(enrollee.getId()));
        enrollee.getKitRequests().addAll(kitRequestService.findByEnrollee(enrollee));
        enrollee.setProfile(profileService.loadWithMailingAddress(enrollee.getProfileId()).orElseThrow(() -> new IllegalStateException("enrollee does not have a profile")));
        enrollee.setFamilyEnrollees(familyEnrolleeService.findByEnrolleeId(enrollee.getId()));
        enrollee.setRelations(enrolleeRelationService.findAllByEnrolleeOrTargetId(enrollee.getId()));
        enrollee.setFiles(participantFileService.findByEnrolleeId(enrollee.getId()));
        return enrollee;
    }

    /**
     * Load enrollee tasks, profile, and kit requests
     * (See loadForAdminView description for performance information)
     */
    public Enrollee loadForParticipantDashboard(Enrollee enrollee) {
        enrollee.getParticipantTasks().addAll(participantTaskService.findByEnrolleeId(enrollee.getId()));
        enrollee.getKitRequests().addAll(kitRequestService.findByEnrollee(enrollee));
        enrollee.setProfile(profileService.loadWithMailingAddress(enrollee.getProfileId()).orElse(null));
        return enrollee;
    }

    public Optional<Enrollee> findByPreEnrollResponseId(UUID preEnrollResponseId) {
        return dao.findByPreEnrollResponseId(preEnrollResponseId);
    }

    public int countByStudyEnvironmentId(UUID studyEnvironmentId) {
        return dao.countByStudyEnvironmentId(studyEnvironmentId);
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

        participantFileService.deleteByEnrolleeId(enrolleeId);

        for (SurveyResponse surveyResponse : surveyResponseService.findByEnrolleeId(enrolleeId)) {
            surveyResponseService.delete(surveyResponse.getId(), cascades);
        }
        participantNoteService.deleteByEnrollee(enrolleeId);
        kitRequestService.deleteByEnrolleeId(enrolleeId, cascades);

        notificationService.deleteByEnrolleeId(enrolleeId);
        participantDataChangeService.deleteByEnrolleeId(enrolleeId);

        enrolleeRelationService.deleteAllByEnrolleeIdOrTargetId(enrolleeId);
        dao.delete(enrolleeId);
        if (enrollee.getPreEnrollmentResponseId() != null) {
            preEnrollmentResponseDao.delete(enrollee.getPreEnrollmentResponseId());
        }
        if (cascades.contains(AllowedCascades.PARTICIPANT_USER)) {
            try {
                mailingListContactService.deleteByParticipantUserId(enrollee.getParticipantUserId());
                portalParticipantUserService.deleteByParticipantUserId(enrollee.getParticipantUserId());
                participantUserService.delete(enrollee.getParticipantUserId(), CascadeProperty.EMPTY_SET);
            } catch (Exception e) {
                logger.error("Failed to cascade delete participant user {} -- possibly they are in multiple studies", enrollee.getParticipantUserId(), e);
            }
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
            enrollee.setShortcode(shortcodeService.generateShortcode(null, dao::findOneByShortcode));
        }
        Enrollee savedEnrollee = dao.create(enrollee);
        logger.info("Enrollee created.  id: {}, shortcode: {}, participantUserId: {}", savedEnrollee.getId(),
                savedEnrollee.getShortcode(), savedEnrollee.getParticipantUserId());
        return savedEnrollee;
    }

    @Transactional
    public void updateConsented(UUID enrolleeId, boolean consented) {
        dao.updateConsented(enrolleeId, consented);
        logger.info("Updated enrollee consent status: enrollee: {}, consented {}", enrolleeId, consented);
    }

    public List<Enrollee> findUnassignedToTask(UUID studyEnvironmentId,
                                     String targetStableId,
                                     Integer targetAssignedVersion) {
        return dao.findUnassignedToTask(studyEnvironmentId, targetStableId, targetAssignedVersion);
    }

    public List<Enrollee> findAssignedToTask(UUID studyEnvironmentId,
                                             String targetStableId) {
        return dao.findAssignedToTask(studyEnvironmentId, targetStableId);
    }

    public Optional<Enrollee> findByParticipantUserIdAndStudyEnvId(UUID participantUserId, UUID studyEnvId) {
        return dao.findByParticipantUserIdAndStudyEnvId(participantUserId, studyEnvId);
    }

    public Optional<Enrollee> findByParticipantUserIdAndStudyEnv(UUID participantUserId, String studyShortcode, EnvironmentName envName) {
        StudyEnvironment studyEnv = studyEnvironmentService.findByStudy(studyShortcode, envName)
                .orElseThrow(() -> new NotFoundException("Study environment %s %s not found".formatted(studyShortcode, envName)));
        return findByParticipantUserIdAndStudyEnvId(participantUserId, studyEnv.getId());
    }

    public Optional<Enrollee> findByShortcodeAndStudyEnv(String enrolleeShortcode, String studyShortcode, EnvironmentName envName) {
        Optional<StudyEnvironment> studyEnvironment =
                studyEnvironmentService
                        .findByStudy(studyShortcode, envName);

        return studyEnvironment.flatMap(
                environment -> {
                    return findOneByShortcode(enrolleeShortcode)
                            .filter(enrollee -> enrollee.getStudyEnvironmentId().equals(environment.getId()));
                });

    }

    public Optional<Enrollee> findByShortcodeAndStudyEnvId(String enrolleeShortcode, UUID studyEnvId) {
        return dao.findByShortcodeAndStudyEnvId(enrolleeShortcode, studyEnvId);
    }

    public Enrollee attachProfile(Enrollee enrollee) {
        Optional<Profile> profiles = profileService.find(enrollee.getProfileId());

        profiles.ifPresent(enrollee::setProfile);

        return enrollee;
    }


    public List<Enrollee> attachProfiles(List<Enrollee> enrollees) {
        List<Profile> profiles = profileService.findAllPreserveOrder(enrollees.stream().map(Enrollee::getProfileId).toList());

        for (int i = 0; i < enrollees.size(); i++) {
            enrollees.get(i).setProfile(profiles.get(i));
        }

        return enrollees;
    }

    public List<Enrollee> findAllByFamilyId(UUID id) {
        return dao.findAllByFamilyId(id);
    }

    public List<Enrollee> findAllByPortalEnv(UUID portalId, EnvironmentName environmentName) {
        return dao.findAllByPortalEnv(portalId, environmentName);
    }

    public enum AllowedCascades implements CascadeProperty {
        PARTICIPANT_USER
    }
}
