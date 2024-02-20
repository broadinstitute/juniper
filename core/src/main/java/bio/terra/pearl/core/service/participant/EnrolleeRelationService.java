package bio.terra.pearl.core.service.participant;

import java.util.List;
import java.util.UUID;

import bio.terra.pearl.core.dao.participant.EnrolleeRelationDao;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.participant.EnrolleeRelation;
import bio.terra.pearl.core.model.participant.RelationshipType;
import bio.terra.pearl.core.service.DataAuditedService;
import bio.terra.pearl.core.service.workflow.DataChangeRecordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class EnrolleeRelationService extends DataAuditedService<EnrolleeRelation, EnrolleeRelationDao> {
    EnrolleeService enrolleeService;

    public EnrolleeRelationService(EnrolleeRelationDao enrolleeRelationDao, EnrolleeService enrolleeService,
                                    DataChangeRecordService dataChangeRecordService, ObjectMapper objectMapper) {
        super(enrolleeRelationDao, dataChangeRecordService, objectMapper);
        this.enrolleeService = enrolleeService;
    }

    public List<EnrolleeRelation> findByEnrolleeIdAndRelationType(UUID enrolleeId, RelationshipType relationshipType) {
        return dao.findByEnrolleeIdAndRelationshipType(enrolleeId, relationshipType);
    }

    public List<EnrolleeRelation> findByEnrolleeIdsAndRelationType(List<UUID> enrolleeIds, RelationshipType relationshipType) {
        return dao.findByEnrolleeIdsAndRelationshipType(enrolleeIds, relationshipType);
    }

    public List<EnrolleeRelation> findByTargetEnrolleeId(UUID enrolleeId) {
        return dao.findByTargetEnrolleeId(enrolleeId);
    }

    public void attachTargetEnrollees(List<EnrolleeRelation> relations) {
        dao.attachTargetEnrollees(relations);
    }

    public void deleteAllByEnrolleeIdOrTargetId(UUID enrolleeId) {
        List<EnrolleeRelation> enrolleeRelations = dao.findByEnrolleeId(enrolleeId);
        enrolleeRelations.addAll(dao.findByTargetEnrolleeId(enrolleeId));
        bulkDelete(enrolleeRelations, DataAuditInfo.builder().systemProcess(DataAuditInfo.systemProcessName(getClass(),
                "deleteAllByEnrolleeIdOrTargetId")).build());
    }

}
