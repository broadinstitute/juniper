package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.DaoTestUtils;
import bio.terra.pearl.core.factory.admin.AdminUserFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantNote;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class ParticipantNoteServiceTests extends BaseSpringBootTest {
  @Autowired
  private ParticipantNoteService participantNoteService;
  @Autowired
  private EnrolleeFactory enrolleeFactory;
  @Autowired
  private AdminUserFactory adminUserFactory;

  @Test
  @Transactional
  public void testBasicNoteCrud() {
    AdminUser adminUser = adminUserFactory.buildPersisted("testBasicNoteCrud");
    Enrollee enrollee = enrolleeFactory.buildPersisted("testBaseNoteCrud");
    ParticipantNote note = ParticipantNote.builder()
        .enrolleeId(enrollee.getId())
        .creatingAdminUserId(adminUser.getId())
        .text("Test note 123")
        .build();
    ParticipantNote savedNote = participantNoteService.create(note);
    DaoTestUtils.assertGeneratedProperties(savedNote);
    assertThat(savedNote.getText(), equalTo(note.getText()));
  }
}
