package bio.terra.pearl.core.model.notification;

import bio.terra.pearl.core.model.BaseEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class SendgridEvent extends BaseEntity {
    @JsonProperty("msg_id")
    private String msgId;
    @JsonProperty("subject")
    private String subject;
    @JsonProperty("to_email")
    private String toEmail;
    @JsonProperty("from_email")
    private String fromEmail;
    @JsonProperty("status")
    private String status;
    @JsonProperty("opens_count")
    private Integer opensCount;
    @JsonProperty("clicks_count")
    private Integer clicksCount;
    @JsonProperty("last_event_time")
    private Instant lastEventTime;
    private String apiRequestId;

    @JsonProperty("msg_id")
    public void setMsgId(String msgId) {
        this.msgId = msgId;
        this.apiRequestId = msgId.split("\\.")[0];
    }

    //Nullable because we may not have a notification associated with each Sendgrid event.
    //This is particularly common on local and dev environments, where the SendGrid account is shared.
    private UUID notificationId;
}
