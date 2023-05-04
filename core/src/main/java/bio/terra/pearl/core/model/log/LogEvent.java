package bio.terra.pearl.core.model.log;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class LogEvent {
  private UUID id;
  @Builder.Default
  private Instant createdAt = Instant.now();
  private LogEventType eventType;
  private String eventName;
  private LogEventSource eventSource;
  private String portalShortcode;
  private String studyShortcode;
  private String environmentName;
  private String enrolleeShortcode;
  private UUID operatorId;
  private String eventDetail;
  private String stackTrace;
}
