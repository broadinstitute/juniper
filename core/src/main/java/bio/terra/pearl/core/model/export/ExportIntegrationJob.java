package bio.terra.pearl.core.model.export;

import bio.terra.pearl.core.model.BaseEntity;
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
public class ExportIntegrationJob extends BaseEntity {
    private UUID exportIntegrationId;
    private UUID creatingAdminUserId;
    private String systemProcess;
    private ExportIntegrationJob.Status status;
    private Instant startedAt;
    private Instant completedAt;
    private String result;

    public enum Status {
        NEW,
        GENERATING,
        SENDING,
        COMPLETE,
        FAILED
    }
}
