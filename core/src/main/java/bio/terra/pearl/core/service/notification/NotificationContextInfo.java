package bio.terra.pearl.core.service.notification;


import bio.terra.pearl.core.model.notification.EmailTemplate;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.Study;


/** wrapper class for all the non-participant-specific context info we need to know to send a notification */
public record NotificationContextInfo(Portal portal, PortalEnvironment portalEnv, Study study, EmailTemplate template) { }
