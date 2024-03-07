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
        return findAllValidByTwoProperties("enrollee_id", enrolleeId,"relationship_type", type);
    }

    public List<EnrolleeRelation> findByEnrolleeIdsAndRelationshipType(List<UUID> enrolleeIds, RelationshipType type) {
        return findAllValidByTwoProperties("relationship_type", type, "enrollee_id", enrolleeIds);
    }

    public List<EnrolleeRelation> findByTargetEnrolleeId(UUID enrolleeId) {
        return findAllValidByProperty("target_enrollee_id", enrolleeId);
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

    public List<EnrolleeRelation> findByEnrolleeId(UUID enrolleeId) {
        return findAllByProperty("enrollee_id", enrolleeId);
    }
    /**
     * This method works like the original findAllByTwoProperties method, but it only returns relations that there
     * end dates are greater than or equal to the current date.
     * @param column1Name the name of the first column to filter by
     * @param column1Value the value of the first column to filter by
     * @param column2Name the name of the second column to filter by
     * @param column2Value the value of the second column to filter by
     * @return a list of EnrolleeRelation objects that match the given properties and have a valid end date.
     */
    protected List<EnrolleeRelation> findAllValidByTwoProperties(String column1Name, Object column1Value,
                                             String column2Name, Object column2Value) {
        return jdbi.withHandle(handle ->
                handle.createQuery("select * from " + tableName + " where " + column1Name + " = :column1Value"
                                + " and " + column2Name + " = :column2Value and (end_date is null or end_date >= NOW());")
                        .bind("column1Value", column1Value)
                        .bind("column2Value", column2Value)
                        .mapTo(clazz)
                        .list()
        );
    }

    /**
     * This method works like the original findAllByProperty method, but it only returns relations that there
     * end dates are greater than or equal to the current date.
     * @param columnName the name of the column to filter by
     * @param columnValue the value of the column to filter by
     * @return a list of EnrolleeRelation objects that match the given properties and have a valid end date.
     */
    protected List<EnrolleeRelation> findAllValidByProperty(String columnName, Object columnValue) {
        return jdbi.withHandle(handle ->
                handle.createQuery("select * from " + tableName + " where " + columnName + " = :columnValue "
                                + " and (end_date is null or end_date >= NOW());")
                        .bind("columnValue", columnValue)
                        .mapTo(clazz)
                        .list()
        );
    }

}
