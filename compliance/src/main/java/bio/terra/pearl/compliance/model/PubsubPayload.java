package bio.terra.pearl.compliance.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Payload coming into the app from pubsub-based trigger
 */
@Getter
@Setter
@SuperBuilder
public class PubsubPayload {

    // set this to true to override any state management
    // in the cloud function environment
    private boolean force;

    private String pubsubMessageId;

    public PubsubPayload(boolean force) {
        this.force = force;
    }
}
