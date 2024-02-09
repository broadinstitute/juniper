package bio.terra.pearl.core.service.participant;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import bio.terra.pearl.core.dao.participant.EnrolleeRelationDao;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.EnrolleeRelation;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.RelationshipType;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.service.CrudService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnrolleeRelationService extends CrudService<EnrolleeRelation, EnrolleeRelationDao> {
    EnrolleeService enrolleeService;

    public EnrolleeRelationService(EnrolleeRelationDao enrolleeRelationDao, EnrolleeService enrolleeService) {
        super(enrolleeRelationDao);
        this.enrolleeService = enrolleeService;
    }

    public List<EnrolleeRelation> findByParticipantUserId(UUID participantUserId) {
        return dao.findByParticipantUserId(participantUserId);
    }

    public List<EnrolleeRelation> findByEnrolleeIdAndًُRelationType(UUID enrolleeId, RelationshipType relationshipType) {
        return dao.findByEnrolleeIdAndRelationshipType(enrolleeId, relationshipType);
    }

    public List<Enrollee> findGovernedEnrollees(UUID participantUserId) {
        List<EnrolleeRelation> enrolleeRelations=
                findByParticipantUserId(participantUserId).stream().filter(enrolleeRelation -> isProxy(enrolleeRelation))
                        .collect(Collectors.toList());
        return enrolleeService.findAll(enrolleeRelations.stream().map(EnrolleeRelation::getId).toList());
    }

    public boolean isProxy(EnrolleeRelation enrolleeRelation){
        return RelationshipType.PROXY.equals(enrolleeRelation.getRelationshipType());
    }

    @Transactional
    public Enrollee newGovernedEnrolleeCreationRecord(Enrollee enrollee, ParticipantUser proxyUser) {
        EnrolleeRelation enrolleeRelation = EnrolleeRelation.builder()
                .enrolleeId(enrollee.getId())
                .participantUserId(proxyUser.getId())
                .relationshipType(RelationshipType.PROXY)
                .build();

        this.create(enrolleeRelation);
        return  enrollee;
    }
}
