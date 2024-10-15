package bio.terra.pearl.core.dao.participant;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class ParticipantUserDao extends BaseMutableJdbiDao<ParticipantUser> {

   @Override
   public Class<ParticipantUser> getClazz() {
      return ParticipantUser.class;
   }


   public Optional<ParticipantUser> findOne(String username, EnvironmentName environmentName) {
      return findByTwoProperties("username", username, "environment_name", environmentName);
   }

   public Optional<ParticipantUser> findOneByShortcode(String shortcode) {
      return findByProperty("shortcode", shortcode);
   }

   public Optional<ParticipantUser> findByEnrolleeId(UUID enrolleeId) {
      return jdbi.withHandle(handle -> handle.createQuery("""
                      SELECT pu.* FROM participant_user pu
                      JOIN enrollee e ON pu.id = e.participant_user_id
                      WHERE e.id = :enrolleeId
                      """)
              .bind("enrolleeId", enrolleeId)
              .mapTo(clazz)
              .findOne());
   }

   public Optional<ParticipantUser> findByToken(String token) {
      return findByProperty("token", token);
   }

   public ParticipantUserDao(Jdbi jdbi) {
      super(jdbi);
   }
}
