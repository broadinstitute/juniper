package bio.terra.pearl.core.model.portal;

import bio.terra.pearl.core.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class PortalEnvironmentConfig extends BaseEntity {
    private boolean passwordProtected = true;

    // This is a very low-security password, intended as a soft barrier if a portal needs to be live but not public (e.g.
    // needs to be publicly accessible for friends-and-family beta, but not exposed to the public.
    // it only keeps the frontend from displaying, and therefore is ok to send in the clear.
    private String password = "broad_institute";

    private boolean acceptingRegistration = true;

    private boolean initialized = false;
}
