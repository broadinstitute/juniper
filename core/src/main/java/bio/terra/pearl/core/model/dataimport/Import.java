package bio.terra.pearl.core.model.dataimport;

import bio.terra.pearl.core.model.BaseEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter @Setter @SuperBuilder
@NoArgsConstructor
public class Import extends BaseEntity {
    private UUID responsibleUserId;
    private UUID studyEnvironmentId;
    private ImportType importType;
    private ImportStatus status;
    @Builder.Default
    private List<ImportItem> importItems = new ArrayList<>();
}
