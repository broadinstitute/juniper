package bio.terra.pearl.core.service.workflow;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.model.consent.ConsentResponse;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.kit.KitRequestStatus;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.survey.SurveyResponse;
import bio.terra.pearl.core.model.workflow.Event;
import bio.terra.pearl.core.model.workflow.EventClass;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class EventServiceTests extends BaseSpringBootTest {
    @Autowired
    private PortalEnvironmentFactory portalEnvironmentFactory;
    @Autowired
    private EventService eventService;
    @Autowired
    private EnrolleeFactory enrolleeFactory;

    @Test
    @Transactional
    public void testPersistsEnrolleeConsentEvent() {

        EnrolleeFactory.EnrolleeBundle bundle = enrolleeFactory.buildWithPortalUser("testPersistsEnrolleeConsentEvent");
        Assertions.assertEquals(0, eventService.findAll().size());
        eventService.publishEnrolleeConsentEvent(
                bundle.enrollee(),
                ConsentResponse.builder().build(),
                bundle.portalParticipantUser());

        List<Event> createdEvents = eventService.findAll();
        Assertions.assertEquals(1, createdEvents.size());

        Event createdEvent = createdEvents.get(0);
        Assertions.assertNotNull(createdEvent.getCreatedAt());
        Assertions.assertEquals(EventClass.ENROLLEE_CONSENT_EVENT, createdEvent.getEventClass());
        Assertions.assertEquals(bundle.enrollee().getId(), createdEvent.getEnrolleeId());
        Assertions.assertEquals(bundle.portalParticipantUser().getPortalEnvironmentId(), createdEvent.getPortalEnvironmentId());
    }

    @Test
    @Transactional
    public void testPersistsEnrolleeCreationEvent() {

        EnrolleeFactory.EnrolleeBundle bundle = enrolleeFactory.buildWithPortalUser("testPersistsEnrolleeCreationEvent");
        Assertions.assertEquals(0, eventService.findAll().size());
        eventService.publishEnrolleeCreationEvent(
                bundle.enrollee(),
                bundle.portalParticipantUser());

        List<Event> createdEvents = eventService.findAll();
        Assertions.assertEquals(1, createdEvents.size());

        Event createdEvent = createdEvents.get(0);
        Assertions.assertNotNull(createdEvent.getCreatedAt());
        Assertions.assertEquals(EventClass.ENROLLEE_CREATION_EVENT, createdEvent.getEventClass());
        Assertions.assertEquals(bundle.enrollee().getId(), createdEvent.getEnrolleeId());
        Assertions.assertEquals(bundle.portalParticipantUser().getPortalEnvironmentId(), createdEvent.getPortalEnvironmentId());
    }

    @Test
    @Transactional
    public void testPersistsEnrolleeSurveyEvent() {

        EnrolleeFactory.EnrolleeBundle bundle = enrolleeFactory.buildWithPortalUser("testPersistsEnrolleeSurveyEvent");
        Assertions.assertEquals(0, eventService.findAll().size());
        eventService.publishEnrolleeSurveyEvent(
                bundle.enrollee(),
                SurveyResponse.builder().build(),
                bundle.portalParticipantUser());

        List<Event> createdEvents = eventService.findAll();
        Assertions.assertEquals(1, createdEvents.size());

        Event createdEvent = createdEvents.get(0);
        Assertions.assertNotNull(createdEvent.getCreatedAt());
        Assertions.assertEquals(EventClass.ENROLLEE_SURVEY_EVENT, createdEvent.getEventClass());
        Assertions.assertEquals(bundle.enrollee().getId(), createdEvent.getEnrolleeId());
        Assertions.assertEquals(bundle.portalParticipantUser().getPortalEnvironmentId(), createdEvent.getPortalEnvironmentId());
    }

    @Test
    @Transactional
    public void testPersistsKitStatusEvent() {

        EnrolleeFactory.EnrolleeBundle bundle = enrolleeFactory.buildWithPortalUser("testPersistsKitStatusEvent");
        Assertions.assertEquals(0, eventService.findAll().size());
        eventService.publishKitStatusEvent(
                KitRequest.builder().build(),
                bundle.enrollee(),
                bundle.portalParticipantUser(),
                KitRequestStatus.CREATED);

        List<Event> createdEvents = eventService.findAll();
        Assertions.assertEquals(1, createdEvents.size());

        Event createdEvent = createdEvents.get(0);
        Assertions.assertNotNull(createdEvent.getCreatedAt());
        Assertions.assertEquals(EventClass.KIT_STATUS_EVENT, createdEvent.getEventClass());
        Assertions.assertEquals(bundle.enrollee().getId(), createdEvent.getEnrolleeId());
        Assertions.assertEquals(bundle.portalParticipantUser().getPortalEnvironmentId(), createdEvent.getPortalEnvironmentId());
    }

    @Test
    @Transactional
    public void testPersistsPublishPortalRegistrationEvent() {

        EnrolleeFactory.EnrolleeBundle bundle = enrolleeFactory.buildWithPortalUser("testPersistsPublishPortalRegistrationEvent");
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted("testPersistsPublishPortalRegistrationEvent");
        Assertions.assertEquals(0, eventService.findAll().size());

        eventService.publishPortalRegistrationEvent(
                bundle.portalParticipantUser().getParticipantUser(),
                bundle.portalParticipantUser(),
                portalEnv);

        List<Event> createdEvents = eventService.findAll();
        Assertions.assertEquals(1, createdEvents.size());

        Event createdEvent = createdEvents.get(0);
        Assertions.assertNotNull(createdEvent.getCreatedAt());
        Assertions.assertEquals(EventClass.PORTAL_REGISTRATION_EVENT, createdEvent.getEventClass());
        Assertions.assertNull(createdEvent.getEnrolleeId());
        Assertions.assertEquals(portalEnv.getId(), createdEvent.getPortalEnvironmentId());
    }
}
