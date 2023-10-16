package bio.terra.pearl.core.model.kit;

import bio.terra.pearl.core.service.kit.pepper.PepperKitStatus;

import java.util.Set;

/**
 * High-level, Juniper-centric kit status. Only needs to be specific enough for Juniper to decide how to interact with
 * Pepper. This is not intended to mirror all possible states in Pepper. For that, see
 * {@link PepperKitStatus#currentStatus}.
 *
 * CREATED --> IN_PROGRESS --> COMPLETE
 *                         \-> ERRORED
 *
 * ERRORED kits should always have some error detail associated with them. However, the presence of error details does
 * not necessarily imply ERRORED. There may be transient errors while a kit is IN_PROGRESS that are resolved on the way
 * to being COMPLETE.
 *
 * We may add additional states in the future if needed to facilitate more complicated workflows involving intervention
 * by study staff.
 */
public enum KitRequestStatus {
    CREATED, // record created in Juniper database, usually followed almost immediately by IN_PROGRESS
    IN_PROGRESS, // request sent to Pepper
    COMPLETE, // Pepper returned a successful end state
    FAILED; // Pepper returned a terminal error state

    public static final Set<KitRequestStatus> NON_TERMINAL_STATES = Set.of(CREATED, IN_PROGRESS);
    public static final Set<KitRequestStatus> TERMINAL_STATES = Set.of(COMPLETE, FAILED);
}
