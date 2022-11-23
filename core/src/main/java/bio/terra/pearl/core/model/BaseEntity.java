package bio.terra.pearl.core.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

@Getter @Setter @SuperBuilder
@NoArgsConstructor
public abstract class BaseEntity {
    private UUID id;
    @Builder.Default
    private Instant createdAt = Instant.now();
    @Builder.Default
    private Instant lastUpdatedAt = Instant.now();

    @Override
    public boolean equals(Object obj) {
        return obj != null &&
                obj.getClass() == this.getClass() &&
                getId() != null &&
                getId().equals(((BaseEntity) obj).getId());
    }
}
