package bio.terra.pearl.core.dao.workflow;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.DaoTestUtils;
import bio.terra.pearl.core.factory.participant.EnrolleeBundle;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.workflow.Event;
import bio.terra.pearl.core.model.workflow.EventClass;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EventDaoTests extends BaseSpringBootTest {
    @Autowired
    private EventDao eventDao;

    @Autowired
    private EnrolleeFactory enrolleeFactory;

    @Test
    @Transactional
    public void testBasicCrud() {
        Event event = Event
                .builder()
                .eventClass(EventClass.ENROLLEE_CREATION_EVENT)
                .build();
        Event basicEvent = eventDao.create(event);
        DaoTestUtils.assertGeneratedProperties(basicEvent);

        assertThat(
                basicEvent.getEventClass(),
                equalTo(EventClass.ENROLLEE_CREATION_EVENT));

        eventDao.delete(basicEvent.getId());

        assertThat(eventDao.find(basicEvent.getId()).isEmpty(), equalTo(true));
    }

    @Test
    @Transactional
    public void testForeignKeyConstraints() {
        Event eventRandomEnrolleeId = Event
                .builder()
                .eventClass(EventClass.ENROLLEE_SURVEY_EVENT)
                .enrolleeId(UUID.randomUUID())
                .build();
        Event eventRandomPortalEnvId = Event
                .builder()
                .eventClass(EventClass.ENROLLEE_SURVEY_EVENT)
                .portalEnvironmentId(UUID.randomUUID())
                .build();
        Event eventRandomStudyEnvId = Event
                .builder()
                .eventClass(EventClass.ENROLLEE_SURVEY_EVENT)
                .studyEnvironmentId(UUID.randomUUID())
                .build();

        assertThrows(Exception.class,
                () -> eventDao.create(eventRandomEnrolleeId));
        assertThrows(Exception.class,
                () -> eventDao.create(eventRandomPortalEnvId));
        assertThrows(Exception.class,
                () -> eventDao.create(eventRandomStudyEnvId));
    }

    @Test
    @Transactional
    public void testFullEventCreation(TestInfo info) {
        EnrolleeBundle bundle = enrolleeFactory.buildWithPortalUser(getTestName(info));

        Event event = Event
                .builder()
                .eventClass(EventClass.ENROLLEE_SURVEY_EVENT)
                .studyEnvironmentId(bundle.enrollee().getStudyEnvironmentId())
                .portalEnvironmentId(bundle.portalParticipantUser().getPortalEnvironmentId())
                .enrolleeId(bundle.enrollee().getId())
                .build();

        assertThat(event.getEnrolleeId(), notNullValue());
        assertThat(event.getPortalEnvironmentId(), notNullValue());
        assertThat(event.getStudyEnvironmentId(), notNullValue());

        event = eventDao.create(event);

        DaoTestUtils.assertGeneratedProperties(event);

        assertThat(
                event.getEventClass(),
                equalTo(EventClass.ENROLLEE_SURVEY_EVENT)
        );
        assertThat(
                event.getPortalEnvironmentId(),
                equalTo(bundle.portalParticipantUser().getPortalEnvironmentId())
        );
        assertThat(
                event.getStudyEnvironmentId(),
                equalTo(bundle.enrollee().getStudyEnvironmentId())
        );
        assertThat(
                event.getEnrolleeId(),
                equalTo(bundle.enrollee().getId())
        );
    }
}
