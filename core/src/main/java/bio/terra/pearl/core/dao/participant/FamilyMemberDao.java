package bio.terra.pearl.core.dao.participant;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.participant.FamilyMember;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class FamilyMemberDao extends BaseMutableJdbiDao<FamilyMember> {

    public FamilyMemberDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<FamilyMember> getClazz() {
        return FamilyMember.class;
    }


    public void deleteByFamilyId(UUID familyId) {
        deleteByProperty("family_id", familyId);
    }

    public void deleteByEnrolleeId(UUID enrolleeId) {
        deleteByProperty("enrollee_id", enrolleeId);
    }

}
