package bio.terra.pearl.core.factory.participant;

import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.portal.PortalEnvironment;

public record EnrolleeAndProxy(Enrollee governedEnrollee, Enrollee proxy, PortalParticipantUser proxyPpUser,
                               PortalEnvironment portalEnv) {
}
