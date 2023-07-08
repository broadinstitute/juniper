package bio.terra.pearl.core.dao.participant;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.participant.ParticipantNote;
import java.util.List;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class ParticipantNoteDao extends BaseMutableJdbiDao<ParticipantNote> {
  public ParticipantNoteDao(Jdbi jdbi) {
    super(jdbi);
  }

  public List<ParticipantNote> findByEnrollee(UUID enrolleeId) {
    return findAllByProperty("enrollee_id", enrolleeId);
  }
  public void deleteByEnrollee(UUID enrolleeId) {
    deleteByProperty("enrollee_id", enrolleeId);
  }
  @Override
  protected Class<ParticipantNote> getClazz() {
    return ParticipantNote.class;
  }
}
