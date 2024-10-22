package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.dao.participant.WithdrawnEnrolleeDao;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.participant.*;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.ImmutableEntityService;
import bio.terra.pearl.core.service.exception.internal.InternalServerException;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.study.StudyService;
import bio.terra.pearl.core.service.workflow.EnrollmentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class WithdrawnEnrolleeService extends ImmutableEntityService<WithdrawnEnrollee, WithdrawnEnrolleeDao> {
    private final EnrolleeService enrolleeService;
    private final EnrolleeRelationService enrolleeRelationService;
    private final ObjectMapper objectMapper;
    private final PortalParticipantUserService portalParticipantUserService;
    private final ParticipantUserService participantUserService;
    private final EnrollmentService enrollmentService;
    private final StudyEnvironmentService studyEnvironmentService;
    private final StudyService studyService;
    private final FamilyEnrolleeService familyEnrolleeService;

    public WithdrawnEnrolleeService(WithdrawnEnrolleeDao dao, EnrolleeService enrolleeService, ObjectMapper objectMapper,
                                    PortalParticipantUserService portalParticipantUserService, ParticipantUserService participantUserService,
                                    EnrolleeRelationService enrolleeRelationService, EnrollmentService enrollmentService, StudyEnvironmentService studyEnvironmentService, StudyService studyService, FamilyEnrolleeService familyEnrolleeService) {
        super(dao);
        this.enrolleeService = enrolleeService;
        this.objectMapper = objectMapper;
        this.portalParticipantUserService = portalParticipantUserService;
        this.participantUserService = participantUserService;
        this.enrolleeRelationService = enrolleeRelationService;
        this.enrollmentService = enrollmentService;
        this.studyEnvironmentService = studyEnvironmentService;
        this.studyService = studyService;
        this.familyEnrolleeService = familyEnrolleeService;
    }

    public void deleteByStudyEnvironmentId(UUID studyEnvironmentId) {
        dao.deleteByStudyEnvironmentId(studyEnvironmentId);
    }

    /** Returns a list of WithdrawnEnrollees for the given study environment, but without the enrollee data. */
    public List<WithdrawnEnrollee> findByStudyEnvironmentIdNoData(UUID studyEnvironmentId) {
        return dao.findByStudyEnvironmentIdNoData(studyEnvironmentId);
    }

    public int countByStudyEnvironmentId(UUID studyEnvironmentId) {
        return dao.countByStudyEnvironmentId(studyEnvironmentId);
    }

    public boolean isWithdrawn(String shortcode) {
        return dao.isWithdrawn(shortcode);
    }

    @Transactional
    public WithdrawnEnrollee withdrawEnrollee(Enrollee enrollee, EnrolleeWithdrawalReason reason, DataAuditInfo dataAuditInfo) {
        return withdrawEnrollee(enrollee, reason, null, dataAuditInfo);
    }

    /**
     * creates a WithdrawnEnrollee for the passed-in enrollee, and DELETES THE ENROLLEE.
     * Although the WithdrawnEnrollee record may contain much of the enrollee's data, this should be assumed to be
     * an irreversible operation.
     */
    @Transactional
    public WithdrawnEnrollee withdrawEnrollee(Enrollee enrollee, EnrolleeWithdrawalReason reason, String note, DataAuditInfo dataAuditInfo) {
        dao.loadForWithdrawalPreservation(enrollee);
        ParticipantUser user = participantUserService.find(enrollee.getParticipantUserId()).get();
        try {
            WithdrawnEnrollee withdrawnEnrollee = WithdrawnEnrollee.builder()
                    .shortcode(enrollee.getShortcode())
                    .studyEnvironmentId(enrollee.getStudyEnvironmentId())
                    .enrolleeData(objectMapper.writeValueAsString(enrollee))
                    .userData(objectMapper.writeValueAsString(user))
                    .note(note)
                    .reason(reason)
                    .build();
            withdrawnEnrollee = create(withdrawnEnrollee);
            // if a governed user is being withdrawn, we should withdraw the proxies that are only proxying this user.
            List<Enrollee> proxiesOnlyProxyingForThisUser = enrolleeRelationService.findExclusiveProxiesForTargetEnrollee(enrollee.getId());

            // EDGE CASE: if an enrollee is withdrawing themselves but are also a proxy for someone else,
            // we need to withdraw them then recreate a new, non-subject enrollee for them.
            List<EnrolleeRelation> relations = enrolleeRelationService.findByEnrolleeIdAndRelationType(enrollee.getId(), RelationshipType.PROXY);

            enrolleeRelationService.deleteAllByEnrolleeIdOrTargetId(enrollee.getId());
            enrolleeService.delete(enrollee.getId(), CascadeProperty.EMPTY_SET);
            familyEnrolleeService.deleteByEnrolleeId(enrollee.getId()); // delete all family relationships

            //now withdraw all the proxied users
            for (Enrollee proxy : proxiesOnlyProxyingForThisUser) {
                if (proxy.isSubject()) {
                    continue; // don't withdraw proxies that are also subjects; if they want to withdraw, they should do so separately.
                }
                withdrawEnrollee(proxy, reason, dataAuditInfo);
            }

            if (!relations.isEmpty()) {
                recreateEnrolleeAsProxy(user, enrollee, relations, dataAuditInfo);
            }

            return withdrawnEnrollee;
        } catch (JsonProcessingException e) {
            throw new InternalServerException("Error serializing enrollee or user data", e);
        }
    }

    private void recreateEnrolleeAsProxy(ParticipantUser user, Enrollee withdrawnEnrollee, List<EnrolleeRelation> relations, DataAuditInfo dataAuditInfo) {
        StudyEnvironment studyEnvironment = studyEnvironmentService
                .find(withdrawnEnrollee.getStudyEnvironmentId())
                .orElseThrow(() -> new IllegalStateException("Study environment not found for enrollee"));
        Study study = studyService.find(studyEnvironment.getStudyId()).orElseThrow(() -> new IllegalStateException("Study not found for study environment"));
        PortalParticipantUser ppUser = portalParticipantUserService.findByParticipantUserId(user.getId()).get(0);

        Enrollee newProxy = this.enrollmentService.enroll(
                ppUser,
                studyEnvironment.getEnvironmentName(),
                study.getShortcode(),
                user,
                ppUser,
                null,
                false
        ).getResponse();

        for (EnrolleeRelation relation : relations) {
            Enrollee governedEnrollee = enrolleeService
                    .find(relation.getTargetEnrolleeId())
                    .orElseThrow(() -> new IllegalStateException("Enrollee not found for relation"));

            this.enrolleeRelationService.create(
                    EnrolleeRelation.builder()
                            .enrolleeId(newProxy.getId())
                            .targetEnrolleeId(governedEnrollee.getId())
                            .relationshipType(RelationshipType.PROXY)
                            .beginDate(relation.getBeginDate())
                            .build(),
                    dataAuditInfo
            );
        }
    }
}
