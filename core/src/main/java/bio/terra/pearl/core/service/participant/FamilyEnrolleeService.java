package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.dao.participant.FamilyEnrolleeDao;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.participant.FamilyEnrollee;
import bio.terra.pearl.core.service.DataAuditedService;
import bio.terra.pearl.core.service.workflow.DataChangeRecordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FamilyEnrolleeService extends DataAuditedService<FamilyEnrollee, FamilyEnrolleeDao> {

    public FamilyEnrolleeService(FamilyEnrolleeDao familyEnrolleeDao,
                                 DataChangeRecordService dataChangeRecordService,
                                 ObjectMapper objectMapper) {
        super(familyEnrolleeDao, dataChangeRecordService, objectMapper);
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
}
