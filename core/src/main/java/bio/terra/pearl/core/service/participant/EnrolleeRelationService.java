package bio.terra.pearl.core.service.participant;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import bio.terra.pearl.core.dao.participant.EnrolleeRelationDao;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.EnrolleeRelation;
import bio.terra.pearl.core.model.participant.RelationshipType;
import bio.terra.pearl.core.service.CrudService;
import org.springframework.stereotype.Service;

@Service
public class EnrolleeRelationService extends CrudService<EnrolleeRelation, EnrolleeRelationDao> {
    EnrolleeService enrolleeService;

    public EnrolleeRelationService(EnrolleeRelationDao enrolleeRelationDao, EnrolleeService enrolleeService) {
        super(enrolleeRelationDao);
        this.enrolleeService = enrolleeService;
    }

    public List<EnrolleeRelation> findByParticipantUserIdAndPortalId(UUID participantUserId, UUID portalId) {
        return dao.findByParticipantUserIdAndPortalId(participantUserId, portalId);
    }

    public List<EnrolleeRelation> findByEnrolleeIdAndPortalId(UUID enrolleeId, UUID portalId) {
        return dao.findByEnrolleeIdAndPortalId(enrolleeId, portalId);
    }

    public List<Enrollee> findGovernedEnrollees(UUID participantUserId, UUID portalId) {
        List<EnrolleeRelation> enrolleeRelations=
                findByParticipantUserIdAndPortalId(participantUserId, portalId).stream().filter(enrolleeRelation -> isProxy(enrolleeRelation))
                        .collect(Collectors.toList());
        return enrolleeService.findAll(enrolleeRelations.stream().map(EnrolleeRelation::getId).toList());
    }

    public boolean isProxy(EnrolleeRelation enrolleeRelation){
        return RelationshipType.PROXY.equals(enrolleeRelation.getRelationshipType());
    }
}
