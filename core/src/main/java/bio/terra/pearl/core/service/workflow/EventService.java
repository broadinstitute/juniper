package bio.terra.pearl.core.service.workflow;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.consent.ConsentResponse;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.survey.SurveyResponse;
import bio.terra.pearl.core.model.workflow.HubResponse;
import bio.terra.pearl.core.service.consent.EnrolleeConsentEvent;
import bio.terra.pearl.core.service.kit.KitStatusEvent;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.rule.EnrolleeRuleData;
import bio.terra.pearl.core.service.rule.EnrolleeRuleService;
import bio.terra.pearl.core.service.survey.EnrolleeSurveyEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * All event publishing should be done via method calls in this service, to ensure that the events are constructed
 * properly with appropriate supporting data.
 */

@Service
@Slf4j
public class EventService {
    private final ParticipantTaskService participantTaskService;
    private final EnrolleeRuleService enrolleeRuleService;
    private final EnrolleeService enrolleeService;

    public EventService(ParticipantTaskService participantTaskService,
                        EnrolleeRuleService enrolleeRuleService, EnrolleeService enrolleeService) {
        this.participantTaskService = participantTaskService;
        this.enrolleeRuleService = enrolleeRuleService;
        this.enrolleeService = enrolleeService;
    }

    /** publishes a KitStatusEvent.
     *
     * @param kitRequest the current kit request already updated with the new status
     * @param priorDsmStatus the prior status
     */
    public KitStatusEvent publishKitStatusEvent(KitRequest kitRequest, KitRequestStatus priorStatus,
                                                PortalParticipantUser ppUser) {
        KitStatusEvent event = KitStatusEvent.newInstance(kitRequest, priorStatus);
        event.setEnrollee(enrolleeS);
        event.setPortalParticipantUser(ppUser);
        populateEvent(event);
        log.info("kit status event for enrollee {}, studyEnv {} - status {} => {}",
                enrollee.getShortcode(), enrollee.getStudyEnvironmentId(),
                priorStatus, kitRequest.getExternalRequest());
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
        applicationEventPublisher.publishEvent(event);
        return event;
    }

    public EnrolleeCreationEvent publishEnrolleeCreationEvent(Enrollee enrollee, PortalParticipantUser ppUser) {
        EnrolleeRuleData enrolleeRuleData = enrolleeRuleService.fetchData(enrollee);
        EnrolleeCreationEvent enrolleeEvent = EnrolleeCreationEvent.builder()
                .enrollee(enrollee)
                .portalParticipantUser(ppUser)
                .enrolleeRuleData(enrolleeRuleData)
                .build();
        applicationEventPublisher.publishEvent(enrolleeEvent);
        return enrolleeEvent;
    }

    /** adds ruleData to the event, and also ensures the enrollee task list is refreshed */
    protected void populateEvent(EnrolleeEvent event) {
        Enrollee enrollee = event.getEnrollee();
        event.setEnrolleeRuleData(enrolleeRuleService.fetchData(enrollee));
        enrollee.getParticipantTasks().clear();
        enrollee.getParticipantTasks().addAll(participantTaskService.findByEnrolleeId(enrollee.getId()));
    }

    /** Assembles a HubResponse using the objects attached to the event.  this makes sure that the
     * response reflects the latest task list and profile
     */
    public <T extends BaseEntity> HubResponse<T> buildHubResponse(EnrolleeEvent event, T response) {
        HubResponse hubResponse = HubResponse.builder()
                .response(response)
                .tasks(event.getEnrollee().getParticipantTasks().stream().toList())
                .enrollee(event.getEnrollee())
                .profile(event.getEnrolleeRuleData().profile())
                .build();
        return hubResponse;
    }


    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
}
