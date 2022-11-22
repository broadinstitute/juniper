package bio.terra.pearl.core.dao;

import bio.terra.pearl.core.model.ParticipantUser;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class ParticipantUserDao extends BaseJdbiDao<ParticipantUser> {
   @Override
   public Class<ParticipantUser> getClazz() {
      return ParticipantUser.class;
   }

   protected String getNaturalKeyMatchQuery() {
      return "select * from " + tableName + " where username = :username and environment_name = :environmentName";
   }

   public ParticipantUserDao(Jdbi jdbi) {
      super(jdbi);
   }
}
