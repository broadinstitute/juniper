package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.dao.participant.ParticipantNoteDao;
import bio.terra.pearl.core.model.participant.ParticipantNote;
import bio.terra.pearl.core.service.CrudService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ParticipantNoteService extends CrudService<ParticipantNote, ParticipantNoteDao> {
  public ParticipantNoteService(ParticipantNoteDao dao) {
    super(dao);
  }

  @Transactional
  public void deleteByEnrollee(UUID enrolleeId) {
    dao.deleteByEnrollee(enrolleeId);
  }

  public List<ParticipantNote> findByEnrollee(UUID enrolleeId) {
    return dao.findByEnrollee(enrolleeId);
  }
}
