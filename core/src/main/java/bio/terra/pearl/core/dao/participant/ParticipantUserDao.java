package bio.terra.pearl.core.dao.participant;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class ParticipantUserDao extends BaseJdbiDao<ParticipantUser> {
   private ProfileDao profileDao;

   @Override
   public Class<ParticipantUser> getClazz() {
      return ParticipantUser.class;
   }

   /**
    * loads the user along with their profile
    * */
   public Optional<ParticipantUser> getWithProfile(UUID participantUserId) {
      return findWithChild(participantUserId, "profileId", "profile", profileDao);
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



   public ParticipantUserDao(Jdbi jdbi, ProfileDao profileDao) {
      super(jdbi);
      this.profileDao = profileDao;
   }
}
