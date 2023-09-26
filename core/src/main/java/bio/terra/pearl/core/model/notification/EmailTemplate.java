package bio.terra.pearl.core.model.notification;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.Versioned;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class EmailTemplate extends BaseEntity implements Versioned {
    private String body;
    private String subject;
    private String name;
    private String stableId;
    private int version;
    private Integer publishedVersion;
    private UUID portalId;
}
