package bio.terra.pearl.core.service.portal.exception;

import java.util.function.Supplier;

/** throw if the invariant that all portals must have a config object is violated */
public class PortalConfigMissing extends RuntimeException {
        public static final Supplier<PortalConfigMissing> SUPPLIER = PortalConfigMissing::new;
}
