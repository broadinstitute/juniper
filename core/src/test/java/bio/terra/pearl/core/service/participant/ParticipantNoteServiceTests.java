package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.DaoTestUtils;
import bio.terra.pearl.core.factory.admin.AdminUserFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantNote;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ParticipantNoteServiceTests extends BaseSpringBootTest {
  @Autowired
  private ParticipantNoteService participantNoteService;
  @Autowired
  private EnrolleeFactory enrolleeFactory;
  @Autowired
  private AdminUserFactory adminUserFactory;

  @Test
  @Transactional
  public void testBasicNoteCrud(TestInfo info) {
    AdminUser adminUser = adminUserFactory.buildPersisted(getTestName(info));
    Enrollee enrollee = enrolleeFactory.buildPersisted(getTestName(info));
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
