package bio.terra.pearl.core.dao.participant;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.DaoTestUtils;
import bio.terra.pearl.core.factory.admin.AdminUserFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantNote;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class ParticipantNoteDaoTests extends BaseSpringBootTest {
  @Autowired
  private ParticipantNoteDao participantNoteDao;
  @Autowired
  private EnrolleeFactory enrolleeFactory;
  @Autowired
  private AdminUserFactory adminUserFactory;

  @Test
  @Transactional
  public void testNoteEnrolleeConstraint() {
    AdminUser adminUser = adminUserFactory.buildPersisted("testNoteAdminUserConstraint");

    ParticipantNote note = ParticipantNote.builder()
        .creatingAdminUserId(adminUser.getId())
        .text("Test note 123")
        .build();
    Assertions.assertThrows(UnableToExecuteStatementException.class, () -> {
      participantNoteDao.create(note);
    });
  }

  @Test
  @Transactional
  public void testNoteAdminUserConstraint() {
    Enrollee enrollee = enrolleeFactory.buildPersisted("testNoteEnrolleeConstraint");

    ParticipantNote note = ParticipantNote.builder()
        .enrolleeId(enrollee.getId())
        .text("Test note 123")
        .build();
    Assertions.assertThrows(UnableToExecuteStatementException.class, () -> {
      participantNoteDao.create(note);
    });
  }

  @Test
  @Transactional
  public void testNoteTextConstraint() {
    AdminUser adminUser = adminUserFactory.buildPersisted("testNoteTextConstraint");
    Enrollee enrollee = enrolleeFactory.buildPersisted("testNoteTextConstraint");

    ParticipantNote note = ParticipantNote.builder()
        .enrolleeId(enrollee.getId())
        .creatingAdminUserId(adminUser.getId())
        .build();
    Assertions.assertThrows(UnableToExecuteStatementException.class, () -> {
      participantNoteDao.create(note);
    });
  }

  @Test
  @Transactional
  public void testNoteCrud() {
    AdminUser adminUser = adminUserFactory.buildPersisted("testNoteCrud");
    Enrollee enrollee = enrolleeFactory.buildPersisted("testNoteCrud");

    ParticipantNote note = ParticipantNote.builder()
        .enrolleeId(enrollee.getId())
        .creatingAdminUserId(adminUser.getId())
        .text("test text 234")
        .build();
    var savedNote = participantNoteDao.create(note);
    DaoTestUtils.assertGeneratedProperties(savedNote);
  }
}
