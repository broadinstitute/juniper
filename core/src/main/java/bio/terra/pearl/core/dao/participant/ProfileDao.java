package bio.terra.pearl.core.dao.participant;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.participant.Profile;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class ProfileDao extends BaseJdbiDao<Profile> {
    public ProfileDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<Profile> getClazz() {
        return Profile.class;
    }
}
