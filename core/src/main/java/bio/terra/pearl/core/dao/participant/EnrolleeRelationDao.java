package bio.terra.pearl.core.dao.participant;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.participant.EnrolleeRelation;
import bio.terra.pearl.core.model.participant.RelationshipType;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class EnrolleeRelationDao extends BaseMutableJdbiDao<EnrolleeRelation> {

    public EnrolleeRelationDao(Jdbi jdbi) {
        super(jdbi);
    }
    @Override
    protected Class<EnrolleeRelation> getClazz() {
        return EnrolleeRelation.class;
    }

    public List<EnrolleeRelation> findByParticipantUserIdAndPortalId(UUID participantUserId, UUID portalId){
        return findAllByTwoProperties("participant_user_id", participantUserId, "portal_id", portalId);
    }

    public List<EnrolleeRelation> findByEnrolleeIdAndPortalId(UUID enrolleeId, UUID portalId){
        return findAllByTwoProperties("enrollee_id", enrolleeId, "portal_id", portalId);
    }

    public Optional<EnrolleeRelation> findByParticipantUserIdAndRelationshipType(UUID participantUserId, RelationshipType type) {
        return findByTwoProperties("participant_user_id", participantUserId, "relationship_type", type);
    }

    public List<EnrolleeRelation> findByEnrolleeIdAndRelationshipType(UUID enrolleeId, RelationshipType type) {
        return findAllByTwoProperties("enrollee_id", enrolleeId,"relationship_type", type);
    }
}