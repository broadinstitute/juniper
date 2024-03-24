package bio.terra.pearl.core.model.notification;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Getter @Setter @SuperBuilder @NoArgsConstructor
public class NotificationEventDetails {
    private String subject;
    private String toEmail;
    private String fromEmail;
    private String status;
    private int opensCount;
    private int clicksCount;
    private Instant lastEventTime;
}
