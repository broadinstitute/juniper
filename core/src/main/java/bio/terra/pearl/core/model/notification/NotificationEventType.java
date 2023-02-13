package bio.terra.pearl.core.model.notification;

import bio.terra.pearl.core.service.consent.EnrolleeConsentEvent;
import bio.terra.pearl.core.service.survey.EnrolleeSurveyEvent;
import bio.terra.pearl.core.service.workflow.BaseEvent;
import bio.terra.pearl.core.service.workflow.EnrolleeCreationEvent;
import bio.terra.pearl.core.service.workflow.PortalRegistrationEvent;

public enum NotificationEventType {
    PORTAL_REGISTRATION(PortalRegistrationEvent.class),
    SURVEY_RESPONSE(EnrolleeSurveyEvent.class),
    STUDY_ENROLLMENT(EnrolleeCreationEvent.class),
    STUDY_CONSENT(EnrolleeConsentEvent.class);

    public final Class<? extends BaseEvent> eventClass;
    NotificationEventType(Class<? extends BaseEvent> eventClass) {
        this.eventClass = eventClass;
    }
}
