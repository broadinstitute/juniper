package bio.terra.pearl.core.model.study;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.portal.Portal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter @Setter @SuperBuilder
@NoArgsConstructor
public class PortalStudy extends BaseEntity {
    private UUID portalId;
    private Portal portal;
    private UUID studyId;
    private Study study;
}
