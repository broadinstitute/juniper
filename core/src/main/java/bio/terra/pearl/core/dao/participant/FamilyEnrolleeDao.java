package bio.terra.pearl.core.dao.participant;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.participant.FamilyEnrollee;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class FamilyEnrolleeDao extends BaseMutableJdbiDao<FamilyEnrollee> {

    public FamilyEnrolleeDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<FamilyEnrollee> getClazz() {
        return FamilyEnrollee.class;
    }


    public List<FamilyEnrollee> findByFamilyId(UUID familyId) {
        return findAllByProperty("family_id", familyId);
    }

    public List<FamilyEnrollee> findByEnrolleeId(UUID enrolleeId) {
        return findAllByProperty("enrollee_id", enrolleeId);
    }

    // WARNING: This method is not audited; it should only be used during study population/repopulation
    public void deleteByEnrolleeId(UUID enrolleeId) {
        deleteByProperty("enrollee_id", enrolleeId);
    }

    // WARNING: This method is not audited; it should only be used during study population/repopulation
    public void deleteByStudyEnvironmentId(UUID studyEnvironmentId) {
        jdbi.withHandle(handle -> handle.createUpdate(
                        "DELETE FROM family_enrollee fe " +
                                "WHERE fe.family_id in " +
                                "(SELECT f.id FROM family f WHERE f.study_environment_id = :studyEnvironmentId)")
                .bind("studyEnvironmentId", studyEnvironmentId)
                .execute());
    }
}
