package bio.terra.pearl.core.service.workflow;

import bio.terra.pearl.core.model.consent.ConsentResponse;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.service.consent.EnrolleeConsentEvent;
import bio.terra.pearl.core.service.participant.ParticipantTaskService;
import bio.terra.pearl.core.service.rule.EnrolleeRuleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class EnrolleeEventService {
    private static final Logger logger = LoggerFactory.getLogger(EnrolleeEventService.class);
    private ParticipantTaskService participantTaskService;
    private EnrolleeRuleService enrolleeRuleService;

    public EnrolleeEventService(ParticipantTaskService participantTaskService,
                                EnrolleeRuleService enrolleeRuleService) {
        this.participantTaskService = participantTaskService;
        this.enrolleeRuleService = enrolleeRuleService;
    }

    public EnrolleeConsentEvent publishEnrolleeConsentEvent(Enrollee enrollee, ConsentResponse response,
                                                         PortalParticipantUser ppUser) {
        EnrolleeConsentEvent enrolleeConsentEvent = EnrolleeConsentEvent.builder()
                .consentResponse(response)
                .enrollee(enrollee)
                .portalParticipantUser(ppUser)
                .build();
        populateEvent(enrolleeConsentEvent);
        logger.info("consent event for enrollee {}, studyEnv {} - formId {}, consented {}",
                enrollee.getShortcode(), enrollee.getStudyEnvironmentId(),
                response.getConsentFormId(), response.isConsented());
        applicationEventPublisher.publishEvent(enrolleeConsentEvent);
        return enrolleeConsentEvent;
    }

    /** adds ruleData to the event, and also ensures the enrollee task list is refreshed */
    protected void populateEvent(EnrolleeEvent event) {
        Enrollee enrollee = event.getEnrollee();
        event.setEnrolleeRuleData(enrolleeRuleService.fetchData(enrollee));
        enrollee.getParticipantTasks().clear();
        enrollee.getParticipantTasks().addAll(participantTaskService.findByEnrolleeId(enrollee.getId()));
    }


    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
}
