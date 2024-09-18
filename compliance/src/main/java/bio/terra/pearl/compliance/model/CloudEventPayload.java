package bio.terra.pearl.compliance.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@ToString
public class CloudEventPayload {

    private CloudEventMetadata message;

    public String getMessageId() {
        if (message != null) {
            return message.getMessageId();
        } else {
            return null;
        }
    }
}
