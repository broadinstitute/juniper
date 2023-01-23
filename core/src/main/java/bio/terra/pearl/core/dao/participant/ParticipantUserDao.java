package bio.terra.pearl.core.dao.participant;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import java.util.Optional;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class ParticipantUserDao extends BaseMutableJdbiDao<ParticipantUser> {

   @Override
   public Class<ParticipantUser> getClazz() {
      return ParticipantUser.class;
   }


   public Optional<ParticipantUser> findOne(String username, EnvironmentName environmentName) {
      return jdbi.withHandle(handle ->
              handle.createQuery("select * from " + tableName
                              + " where username = :username and environment_name = :environmentName")
                      .bind("username", username)
                      .bind("environmentName", environmentName)
                      .mapTo(clazz)
                      .findOne()
      );
   }

   public Optional<ParticipantUser> findByToken(String token) {
      return findByProperty("token", token);
   }

   public ParticipantUserDao(Jdbi jdbi) {
      super(jdbi);
   }
}
