package bio.terra.pearl.core.model.kit;

import bio.terra.pearl.core.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter @Setter @SuperBuilder
@NoArgsConstructor
public class KitType extends BaseEntity {
    private String name;
    private String displayName;
    private String description;
}
