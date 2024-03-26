package bio.terra.pearl.core.service.workflow;

import bio.terra.pearl.core.dao.workflow.EventDao;
import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.consent.ConsentResponse;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.kit.KitRequestStatus;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.survey.SurveyResponse;
import bio.terra.pearl.core.model.workflow.Event;
import bio.terra.pearl.core.model.workflow.EventClass;
import bio.terra.pearl.core.model.workflow.HubResponse;
import bio.terra.pearl.core.service.ImmutableEntityService;
import bio.terra.pearl.core.service.consent.EnrolleeConsentEvent;
import bio.terra.pearl.core.service.kit.KitStatusEvent;
import bio.terra.pearl.core.service.rule.EnrolleeBundleService;
import bio.terra.pearl.core.service.rule.EnrolleeProfileBundle;
import bio.terra.pearl.core.service.survey.event.EnrolleeSurveyEvent;
import bio.terra.pearl.core.service.survey.event.SurveyPublishedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * All event publishing should be done via method calls in this service to ensure that the
 * events are constructed properly with appropriate supporting data.
 */

@Service
@Slf4j
public class EventService extends ImmutableEntityService<Event, EventDao> {
    private final ParticipantTaskService participantTaskService;
    private final EnrolleeBundleService enrolleeBundleService;

    public EventService(EventDao dao, ParticipantTaskService participantTaskService,
                        EnrolleeBundleService enrolleeBundleService) {
        super(dao);
        this.participantTaskService = participantTaskService;
        this.enrolleeBundleService = enrolleeBundleService;
    }

    /**
     * Publish a KitStatusEvent.
     *
     * @param kitRequest  current kit request already updated with the new status
     * @param priorStatus prior kit status
     */
    public KitStatusEvent publishKitStatusEvent(KitRequest kitRequest, Enrollee enrollee,
                                                PortalParticipantUser portalParticipantUser, KitRequestStatus priorStatus) {
        KitStatusEvent event = KitStatusEvent.newInstance(kitRequest, priorStatus);
        event.setEnrollee(enrollee);
        event.setPortalParticipantUser(portalParticipantUser);
        populateEvent(event);
        log.info("Kit status event for enrollee {}, studyEnv {}: status {} => {}",
                enrollee.getShortcode(), enrollee.getStudyEnvironmentId(),
                priorStatus, kitRequest.getStatus());
        saveEvent(EventClass.KIT_STATUS_EVENT, portalParticipantUser.getPortalEnvironmentId(), enrollee);
        applicationEventPublisher.publishEvent(event);
        return event;
    }

    public EnrolleeConsentEvent publishEnrolleeConsentEvent(Enrollee enrollee, ConsentResponse response,
                                                            PortalParticipantUser ppUser) {
        EnrolleeConsentEvent event = EnrolleeConsentEvent.builder()
                .consentResponse(response)
                .enrollee(enrollee)
                .portalParticipantUser(ppUser)
                .build();
        populateEvent(event);
        log.info("consent event for enrollee {}, studyEnv {} - formId {}, consented {}",
                enrollee.getShortcode(), enrollee.getStudyEnvironmentId(),
                response.getConsentFormId(), response.isConsented());
        saveEvent(EventClass.ENROLLEE_CONSENT_EVENT, ppUser.getPortalEnvironmentId(), enrollee);
        applicationEventPublisher.publishEvent(event);
        return event;
    }

    public EnrolleeSurveyEvent publishEnrolleeSurveyEvent(Enrollee enrollee, SurveyResponse response,
                                                          PortalParticipantUser ppUser) {
        EnrolleeSurveyEvent event = EnrolleeSurveyEvent.builder()
                .surveyResponse(response)
                .enrollee(enrollee)
                .portalParticipantUser(ppUser)
                .build();
        populateEvent(event);
        log.info("survey event for enrollee {}, studyEnv {} - formId {}, completed {}",
                enrollee.getShortcode(), enrollee.getStudyEnvironmentId(),
                response.getSurveyId(), response.isComplete());
        saveEvent(EventClass.ENROLLEE_SURVEY_EVENT, ppUser.getPortalEnvironmentId(), enrollee);
        applicationEventPublisher.publishEvent(event);
        return event;
    }

    public PortalRegistrationEvent publishPortalRegistrationEvent(ParticipantUser user,
                                                                  PortalParticipantUser ppUser,
                                                                  PortalEnvironment portalEnv) {
        PortalRegistrationEvent event = PortalRegistrationEvent.builder()
                .participantUser(user)
                .newPortalUser(ppUser)
                .portalEnvironment(portalEnv)
                .build();
        saveEvent(EventClass.PORTAL_REGISTRATION_EVENT, portalEnv.getId(), null);
        applicationEventPublisher.publishEvent(event);
        return event;
    }

    public EnrolleeCreationEvent publishEnrolleeCreationEvent(Enrollee enrollee, PortalParticipantUser ppUser) {
        EnrolleeProfileBundle enrolleeProfileBundle = enrolleeBundleService.fetchProfile(enrollee);
        EnrolleeCreationEvent enrolleeEvent = EnrolleeCreationEvent.builder()
                .enrollee(enrollee)
                .portalParticipantUser(ppUser)
                .enrolleeProfileBundle(enrolleeProfileBundle)
                .build();
        saveEvent(EventClass.ENROLLEE_CREATION_EVENT, ppUser.getPortalEnvironmentId(), enrollee);
        applicationEventPublisher.publishEvent(enrolleeEvent);
        return enrolleeEvent;
    }

    public SurveyPublishedEvent publishSurveyPublishedEvent(UUID portalEnvId, UUID studyEnvId, Survey survey) {
        SurveyPublishedEvent event = SurveyPublishedEvent.builder()
                        .studyEnvironmentId(studyEnvId)
                .surveyId(survey.getId())
                .survey(survey)
                .portalEnvironmentId(portalEnvId)
                .eventClass(EventClass.SURVEY_PUBLISHED_EVENT)
                .build();

        dao.create(event);
        applicationEventPublisher.publishEvent(event);
        return event;
    }

    /**
     * Saves a record of the event. If the event does not involve a specific enrollee/portalEnv,
     * they can be set to null.
     */
    private void saveEvent(EventClass eventClass,
                                    UUID portalEnvId,
                                    Enrollee enrollee) {
        Event.EventBuilder<?, ?> builder = Event.builder()
                .eventClass(eventClass);

        if (Objects.nonNull(portalEnvId)) {
            builder = builder
                    .portalEnvironmentId(portalEnvId);
        }

        // if enrollee specific, set related fields
        if (Objects.nonNull(enrollee)) {
            builder = builder
                    .enrolleeId(enrollee.getId())
                    .studyEnvironmentId(enrollee.getStudyEnvironmentId());
        }

        dao.create(builder.build());
    }

    public List<Event> findAllEventsByEnrolleeId(UUID enrolleeId) {
        return dao.findAllByEnrolleeId(enrolleeId);
    }

    public List<Event> findAllByStudyEnvAndClass(UUID studyEnvId, EventClass eventClass) { return dao.findAllByStudyEnvAndClass(studyEnvId, eventClass);}

    /**
     * adds ruleData to the event, and also ensures the enrollee task list is refreshed
     */
    protected void populateEvent(EnrolleeEvent event) {
        Enrollee enrollee = event.getEnrollee();
        event.setEnrolleeProfileBundle(enrolleeBundleService.fetchProfile(enrollee));
        enrollee.getParticipantTasks().clear();
        enrollee.getParticipantTasks().addAll(participantTaskService.findByEnrolleeId(enrollee.getId()));
    }

    /**
     * Assembles a HubResponse using the objects attached to the event.  this makes sure that the
     * response reflects the latest task list and profile
     */
    public <T extends BaseEntity> HubResponse<T> buildHubResponse(EnrolleeEvent event, T response) {
        HubResponse hubResponse = HubResponse.builder()
                .response(response)
                .tasks(event.getEnrollee().getParticipantTasks().stream().toList())
                .enrollee(event.getEnrollee())
                .profile(event.getEnrolleeProfileBundle().getProfile())
                .build();
        return hubResponse;
    }


    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
}
