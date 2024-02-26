package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.dao.participant.EnrolleeRelationDao;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.EnrolleeRelation;
import bio.terra.pearl.core.model.participant.RelationshipType;
import bio.terra.pearl.core.service.DataAuditedService;
import bio.terra.pearl.core.service.workflow.DataChangeRecordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

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

    public boolean isUserProxyForAnyOf(UUID participantUserId, List<Enrollee> enrolleeList) {
        List<UUID> enrolleeIds = enrolleeList.stream().map(Enrollee::getId).toList();
        return !dao.findEnrolleeRelationsByProxyParticipantUser(participantUserId, enrolleeIds).isEmpty();
    }

    public void attachTargetEnrollees(List<EnrolleeRelation> relations) {
        dao.attachTargetEnrollees(relations);
    }

}
