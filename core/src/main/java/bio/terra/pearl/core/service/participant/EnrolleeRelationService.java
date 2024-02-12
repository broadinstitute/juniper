package bio.terra.pearl.core.service.participant;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import bio.terra.pearl.core.dao.participant.EnrolleeRelationDao;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.EnrolleeRelation;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.RelationshipType;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.CrudService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnrolleeRelationService extends CrudService<EnrolleeRelation, EnrolleeRelationDao> {

    public EnrolleeRelationService(EnrolleeRelationDao enrolleeRelationDao) {
        super(enrolleeRelationDao);
    }

    public List<EnrolleeRelation> findByParticipantUserId(UUID participantUserId) {
        return dao.findByParticipantUserId(participantUserId);
    }

    public List<EnrolleeRelation> findByEnrolleeIdAndًُRelationType(UUID enrolleeId, RelationshipType relationshipType) {
        return dao.findByEnrolleeIdAndRelationshipType(enrolleeId, relationshipType);
    }

    public boolean isProxy(EnrolleeRelation enrolleeRelation) {
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
        return enrollee;
    }

    @Override
    @Transactional
    public void delete(UUID enrolleeRelationId, Set<CascadeProperty> cascades) {
        dao.delete(enrolleeRelationId);
    }

    public void deleteByEnrolleeId(UUID enrolleeId) {
        List<EnrolleeRelation> enrolleeRelations = dao.findByEnrolleeId(enrolleeId);
        for (EnrolleeRelation enrolleeRelation : enrolleeRelations) {
            dao.delete(enrolleeRelation.getId());
        }
    }
}
