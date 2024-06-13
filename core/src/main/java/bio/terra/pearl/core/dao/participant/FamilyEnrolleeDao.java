package bio.terra.pearl.core.dao.participant;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.participant.FamilyEnrollee;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

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


    public void deleteByFamilyId(UUID familyId) {
        deleteByProperty("family_id", familyId);
    }

    public void deleteByEnrolleeId(UUID enrolleeId) {
        deleteByProperty("enrollee_id", enrolleeId);
    }

}
