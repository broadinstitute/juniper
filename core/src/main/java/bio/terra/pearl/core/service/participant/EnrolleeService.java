package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.dao.participant.EnrolleeDao;
import bio.terra.pearl.core.dao.survey.PreEnrollmentResponseDao;
import bio.terra.pearl.core.dao.survey.SurveyResponseDao;
import bio.terra.pearl.core.dao.workflow.ParticipantTaskDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.SurveyResponse;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.CrudService;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.kit.KitRequestDto;
import bio.terra.pearl.core.service.kit.KitRequestService;
import bio.terra.pearl.core.service.notification.NotificationService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.survey.SurveyResponseService;
import bio.terra.pearl.core.service.workflow.AdminTaskService;
import bio.terra.pearl.core.service.workflow.DataChangeRecordService;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

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
    private final DataChangeRecordService dataChangeRecordService;
    private final WithdrawnEnrolleeService withdrawnEnrolleeService;
    private final ParticipantUserService participantUserService;
    private final PortalParticipantUserService portalParticipantUserService;
    private final ParticipantNoteService participantNoteService;
    private final KitRequestService kitRequestService;
    private final AdminTaskService adminTaskService;
    private final SecureRandom secureRandom;
    private final RandomUtilService randomUtilService;
    private final EnrolleeRelationService enrolleeRelationService;
    private final FamilyService familyService;
    private final ShortcodeService shortcodeService;

    public EnrolleeService(EnrolleeDao enrolleeDao,
                           SurveyResponseDao surveyResponseDao,
                           ParticipantTaskDao participantTaskDao,
                           ProfileService profileService,
                           @Lazy SurveyResponseService surveyResponseService,
                           ParticipantTaskService participantTaskService,
                           @Lazy StudyEnvironmentService studyEnvironmentService,
                           PreEnrollmentResponseDao preEnrollmentResponseDao,
                           NotificationService notificationService,
                           @Lazy DataChangeRecordService dataChangeRecordService,
                           @Lazy WithdrawnEnrolleeService withdrawnEnrolleeService,
                           @Lazy ParticipantUserService participantUserService,
                           ParticipantNoteService participantNoteService,
                           KitRequestService kitRequestService,
                           AdminTaskService adminTaskService, SecureRandom secureRandom,
                           RandomUtilService randomUtilService,
                           EnrolleeRelationService enrolleeRelationService,
                           PortalParticipantUserService portalParticipantUserService,
                           FamilyService familyService, ShortcodeService shortcodeService) {
        super(enrolleeDao);
        this.surveyResponseDao = surveyResponseDao;
        this.participantTaskDao = participantTaskDao;
        this.profileService = profileService;
        this.surveyResponseService = surveyResponseService;
        this.participantTaskService = participantTaskService;
        this.studyEnvironmentService = studyEnvironmentService;
        this.preEnrollmentResponseDao = preEnrollmentResponseDao;
        this.notificationService = notificationService;
        this.dataChangeRecordService = dataChangeRecordService;
        this.withdrawnEnrolleeService = withdrawnEnrolleeService;
        this.participantUserService = participantUserService;
        this.participantNoteService = participantNoteService;
        this.kitRequestService = kitRequestService;
        this.adminTaskService = adminTaskService;
        this.secureRandom = secureRandom;
        this.randomUtilService = randomUtilService;
        this.enrolleeRelationService = enrolleeRelationService;
        this.portalParticipantUserService = portalParticipantUserService;
        this.familyService = familyService;
        this.shortcodeService = shortcodeService;
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
        if (enrollee.getFamilyId() != null) {
            enrollee.setFamily(familyService.find(enrollee.getFamilyId()).orElseThrow(() -> new IllegalStateException("family not found for enrollee")));
        }
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
    public List<Enrollee> findForKitManagement(String studyShortcode, EnvironmentName envName) {
        StudyEnvironment studyEnvironment = studyEnvironmentService.verifyStudy(studyShortcode, envName);
        Pair<List<Enrollee>, List<UUID>> enrolleesAndIds = dao.streamByStudyEnvironmentId(studyEnvironment.getId())
                .collect(Collectors.teeing(Collectors.toList(),
                        Collectors.mapping(Enrollee::getId, Collectors.toList()),
                Pair::of
        ));

        List<Enrollee> enrollees = enrolleesAndIds.getFirst();
        List<UUID> enrolleeIds = enrolleesAndIds.getSecond();

        Map<UUID, List<KitRequestDto>> kitsByEnrolleeId = kitRequestService.findByEnrollees(enrollees);
        Map<UUID, List<ParticipantTask>> tasksByEnrolleeId = participantTaskDao.findByEnrolleeIds(enrolleeIds);

        enrollees.forEach(enrollee -> {
            // Be sure to set empty collections to indicate that they are empty instead of not initialized
            enrollee.setParticipantTasks(tasksByEnrolleeId.getOrDefault(enrollee.getId(), Collections.emptyList()));
            enrollee.setKitRequests(kitsByEnrolleeId.getOrDefault(enrollee.getId(), Collections.emptyList()));
        });
        return enrollees;
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
        adminTaskService.deleteByEnrolleId(enrolleeId, null);
        participantNoteService.deleteByEnrollee(enrolleeId);
        kitRequestService.deleteByEnrolleeId(enrolleeId, cascades);

        notificationService.deleteByEnrolleeId(enrolleeId);
        dataChangeRecordService.deleteByEnrolleeId(enrolleeId);

        enrolleeRelationService.deleteAllByEnrolleeIdOrTargetId(enrolleeId);
        dao.delete(enrolleeId);
        if (enrollee.getPreEnrollmentResponseId() != null) {
            preEnrollmentResponseDao.delete(enrollee.getPreEnrollmentResponseId());
        }
        if (cascades.contains(AllowedCascades.PARTICIPANT_USER)) {
            portalParticipantUserService.deleteByParticipantUserId(enrollee.getParticipantUserId());
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

    public List<Enrollee> findAllByFamilyId(UUID id) {
        return dao.findAllByFamilyId(id);
    }

    public enum AllowedCascades implements CascadeProperty {
        PARTICIPANT_USER
    }
}
