package bio.terra.pearl.core.dao.participant;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.EnrolleeRelation;
import bio.terra.pearl.core.model.participant.RelationshipType;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

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

    public void attachTargetEnrollees(List<EnrolleeRelation> relations) {
        List<Enrollee> enrollees = enrolleeDao.findAllPreserveOrder(relations.stream().map(EnrolleeRelation::getTargetEnrolleeId).toList());
        for (int i = 0; i < relations.size(); i++) {
            relations.get(i).setTargetEnrollee(enrollees.get(i));
        }
    }

}
