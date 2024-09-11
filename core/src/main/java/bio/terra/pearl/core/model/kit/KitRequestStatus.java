package bio.terra.pearl.core.model.kit;

/**
 * Kit status.  This mostly mimics PepperKitStatus (the statuses stored internally in Pepper), but with the addition of
 * "new" for kits that are recorded in juniper but do not exist in any external system yet, and "UNKNOWN" for the rare case
 * where we receive a status from Pepper that we don't recognize.
 *
 * ERRORED kits should always have some error detail associated with them. However, the presence of error details does
 * not necessarily imply ERRORED. There may be transient errors while a kit is IN_PROGRESS that are resolved on the way
 * to being COMPLETE.
 *
 * We may add additional states in the future if needed to facilitate more complicated workflows involving intervention
 * by study staff.
 */
public enum KitRequestStatus {
    NEW,
    CREATED,
    QUEUED,
    SENT,
    COLLECTED_BY_STAFF, // for a kit that has been handed back to study staff in person. staff will mail it back to GP
    RECEIVED, // for a kit that has been received by GP
    ERRORED,
    DEACTIVATED, // stopped -- no more processing/shipping will be done on this kit
    UNKNOWN // for when we receive a status from Pepper that we don't recognize
}
