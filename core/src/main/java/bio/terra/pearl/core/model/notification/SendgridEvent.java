package bio.terra.pearl.core.model.notification;

import bio.terra.pearl.core.model.BaseEntity;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class SendgridEvent extends BaseEntity {
    //The aliases are necessary because the SendGrid API uses snake_case. This allows
    //us to re-use the same model for serialization and deserialization, without having
    //to introduce snake_case into other parts of our codebase.
    @JsonAlias("msg_id")
    private String msgId;
    @JsonAlias("subject")
    private String subject;
    @JsonAlias("to_email")
    private String toEmail;
    @JsonAlias("from_email")
    private String fromEmail;
    @JsonAlias("status")
    private String status;
    @JsonAlias("opens_count")
    private Integer opensCount;
    @JsonAlias("clicks_count")
    private Integer clicksCount;
    @JsonAlias("last_event_time")
    private Instant lastEventTime;
    private String apiRequestId;

    @JsonAlias("msg_id")
    public void setMsgId(String msgId) {
        this.msgId = msgId;
        this.apiRequestId = msgId.split("\\.")[0];
    }

    //Nullable because we may not have a notification associated with each Sendgrid event.
    //This is particularly common on local and dev environments, where the SendGrid account is shared.
    private UUID notificationId;
}
