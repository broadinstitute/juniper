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

    public List<ParticipantUser> findUserByPrefix(String usernamePrefix, EnvironmentName environmentName) {
        return jdbi.withHandle(handle ->
                handle.createQuery("select * from " + tableName + " where username LIKE :usernamePrefix"
                                + " and environment_name = :environmentName")
                        .bind("usernamePrefix", usernamePrefix + "%")
                        .bind("environmentName", environmentName)
                        .mapTo(clazz)
                        .list()
        );

    }
}
