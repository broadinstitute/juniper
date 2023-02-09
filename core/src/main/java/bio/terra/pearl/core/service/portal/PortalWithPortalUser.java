package bio.terra.pearl.core.service.portal;

import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.portal.Portal;

public record PortalWithPortalUser(Portal portal, PortalParticipantUser ppUser) {}
