package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.dao.participant.FamilyEnrolleeDao;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.audit.ParticipantDataChange;
import bio.terra.pearl.core.model.participant.FamilyEnrollee;
import bio.terra.pearl.core.service.DataAuditedService;
import bio.terra.pearl.core.service.ParticipantDataAuditedService;
import bio.terra.pearl.core.service.workflow.ParticipantDataChangeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FamilyEnrolleeService extends ParticipantDataAuditedService<FamilyEnrollee, FamilyEnrolleeDao> {

    private final EnrolleeRelationService enrolleeRelationService;

    public FamilyEnrolleeService(FamilyEnrolleeDao familyEnrolleeDao,
                                 ParticipantDataChangeService participantDataChangeService,
                                 ObjectMapper objectMapper,
                                 @Lazy EnrolleeRelationService enrolleeRelationService) {
        super(familyEnrolleeDao, participantDataChangeService, objectMapper);
        this.enrolleeRelationService = enrolleeRelationService;
    }

    @Transactional
    public void deleteByFamilyId(UUID familyId, DataAuditInfo info) {
        List<FamilyEnrollee> objs = dao.findByFamilyId(familyId);

        bulkDelete(objs, info);
    }

    // WARNING: This method is not audited; it should only be used during study population/repopulation
    @Transactional
    public void deleteByStudyEnvironmentId(UUID studyEnvironmentId) {
        dao.deleteByStudyEnvironmentId(studyEnvironmentId);
    }

    // WARNING: This method is not audited; it should only be used during study population/repopulation
    @Transactional
    public void deleteByEnrolleeId(UUID id) {
        dao.deleteByEnrolleeId(id);
    }

    public Optional<FamilyEnrollee> findByFamilyIdAndEnrolleeId(UUID familyId, UUID enrolleeId) {
        return dao.findByFamilyIdAndEnrolleeId(familyId, enrolleeId);
    }

    public List<FamilyEnrollee> findByFamilyId(UUID familyId) {
        return dao.findByFamilyId(familyId);
    }

    @Transactional
    public void deleteFamilyEnrolleeAndAllRelationships(UUID enrolleeId, UUID familyId, DataAuditInfo info) {
        Optional<FamilyEnrollee> familyEnrollee = dao.findByFamilyIdAndEnrolleeId(familyId, enrolleeId);
        familyEnrollee.ifPresentOrElse(fe -> {
                    this.delete(fe.getId(), info);
                    enrolleeRelationService.deleteAllFamilyRelationshipsByEitherEnrollee(
                            enrolleeId, familyId, info);

                },
                () -> {
                    throw new IllegalArgumentException("Family enrollee not found");
                });
    }

    @Transactional
    public FamilyEnrollee getOrCreate(
            UUID enrolleeId,
            UUID familyId,
            DataAuditInfo auditInfo
    ) {
        return findByFamilyIdAndEnrolleeId(familyId, enrolleeId)
                .orElseGet(() -> create(
                        FamilyEnrollee
                                .builder()
                                .familyId(familyId)
                                .enrolleeId(enrolleeId)
                                .build(),
                        auditInfo));
    }

    public List<FamilyEnrollee> findByEnrolleeId(UUID id) {
        return dao.findByEnrolleeId(id);
    }

    @Override
    protected ParticipantDataChange makeCreationChangeRecord(FamilyEnrollee obj, DataAuditInfo auditInfo) {
        ParticipantDataChange participantDataChange = super.makeCreationChangeRecord(obj, auditInfo);

        participantDataChange.setFamilyId(obj.getFamilyId());
        participantDataChange.setEnrolleeId(obj.getEnrolleeId());

        return participantDataChange;
    }

    @Override
    protected ParticipantDataChange makeDeletionChangeRecord(FamilyEnrollee obj, DataAuditInfo auditInfo) {
        ParticipantDataChange participantDataChange = super.makeDeletionChangeRecord(obj, auditInfo);

        participantDataChange.setFamilyId(obj.getFamilyId());
        participantDataChange.setEnrolleeId(obj.getEnrolleeId());

        return participantDataChange;
    }
}
