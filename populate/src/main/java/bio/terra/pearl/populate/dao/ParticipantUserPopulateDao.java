package bio.terra.pearl.populate.dao;

import bio.terra.pearl.core.dao.participant.ParticipantUserDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class ParticipantUserPopulateDao extends ParticipantUserDao {
    public ParticipantUserPopulateDao(Jdbi jdbi) {
        super(jdbi);
    }

    /** finds users with a keyed username from populating.  e.g. dbush+invite-f232@broadinstitute.org */
    public List<ParticipantUser> findUserByPrefix(String usernameKey, EnvironmentName environmentName) {
        return jdbi.withHandle(handle ->
                handle.createQuery("select * from " + tableName + " where username LIKE :usernameKey"
                                + " and environment_name = :environmentName")
                        .bind("usernameKey", "%+" + usernameKey + "-%")
                        .bind("environmentName", environmentName)
                        .mapTo(clazz)
                        .list()
        );

    }
}
