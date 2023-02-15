package bio.terra.pearl.core.model.notification;

import bio.terra.pearl.core.model.BaseEntity;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class EmailTemplate extends BaseEntity {
    private String body;
    private String subject;
    private String name;
    private String stableId;
    private int version;
    private UUID portalId;
}
