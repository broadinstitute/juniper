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

   public ParticipantUserDao(Jdbi jdbi) {
      super(jdbi);
   }
}
