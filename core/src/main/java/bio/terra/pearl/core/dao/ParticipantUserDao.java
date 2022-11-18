package bio.terra.pearl.core.dao;

import bio.terra.pearl.core.model.ParticipantUser;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class ParticipantUserDao extends BaseJdbiDao<ParticipantUser> {
   private Jdbi jdbi;

   @Override
   public List<String> getInsertColumns() {
      return Arrays.asList("created_at", "last_updated_at", "username", "superuser", "token", "last_login");
   }

   @Override
   public Class<ParticipantUser> getClazz() {
      return ParticipantUser.class;
   }

   @Override
   public String getTableName() {
      return "participant_user";
   }

   public ParticipantUserDao(Jdbi jdbi) {
      super(jdbi);
   }
}
