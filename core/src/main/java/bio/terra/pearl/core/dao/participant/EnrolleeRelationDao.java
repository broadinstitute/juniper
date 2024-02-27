package bio.terra.pearl.core.dao.participant;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.EnrolleeRelation;
import bio.terra.pearl.core.model.participant.RelationshipType;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class EnrolleeRelationDao extends BaseMutableJdbiDao<EnrolleeRelation> {
    private EnrolleeDao enrolleeDao;

    public EnrolleeRelationDao(Jdbi jdbi, EnrolleeDao enrolleeDao) {
        super(jdbi);
        this.enrolleeDao = enrolleeDao;
    }
    @Override
    protected Class<EnrolleeRelation> getClazz() {
        return EnrolleeRelation.class;
    }


    public List<EnrolleeRelation> findByEnrolleeIdAndRelationshipType(UUID enrolleeId, RelationshipType type) {
        return findAllByTwoProperties("enrollee_id", enrolleeId,"relationship_type", type);
    }

    public List<EnrolleeRelation> findByEnrolleeIdsAndRelationshipType(List<UUID> enrolleeIds, RelationshipType type) {
        return findAllByTwoProperties("relationship_type", type, "enrollee_id", enrolleeIds);
    }

    public List<EnrolleeRelation> findByTargetEnrolleeId(UUID enrolleeId) {
        return findAllByProperty("target_enrollee_id", enrolleeId);
    }

    public List<EnrolleeRelation> findEnrolleeRelationsByProxyParticipantUser(UUID participantUserId, List<UUID> targetEnrolleeIds) {
        return jdbi.withHandle(handle ->
                handle.createQuery("select relation.* from enrollee proxy " +
                                "inner join enrollee_relation relation on (relation.enrollee_id = proxy.id) " +
                                "where relation.relationship_type = 'PROXY' " +
                                "and relation.target_enrollee_id IN (<targetEnrolleeIds>) " +
                                "and proxy.participant_user_id = :participantUserId ")
                        .bindList("targetEnrolleeIds", targetEnrolleeIds)
                        .bind("participantUserId", participantUserId)
                        .mapTo(clazz)
                        .list()
        );
    }

    public void attachTargetEnrollees(List<EnrolleeRelation> relations) {
        List<Enrollee> enrollees = enrolleeDao.findAllPreserveOrder(relations.stream().map(EnrolleeRelation::getTargetEnrolleeId).toList());
        for (int i = 0; i < relations.size(); i++) {
            relations.get(i).setTargetEnrollee(enrollees.get(i));
        }
    }

}
