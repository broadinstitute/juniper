package bio.terra.pearl.core.model.export;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.service.export.ExportFileFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@SuperBuilder
@Getter @Setter
@NoArgsConstructor
public class ExportOptions extends BaseEntity {
    @Builder.Default
    private boolean splitOptionsIntoColumns = false;
    @Builder.Default
    private boolean stableIdsForOptions = false;
    @Builder.Default
    private boolean onlyIncludeMostRecent = true;
    private String filterString;
    @Builder.Default
    private ExportFileFormat fileFormat = ExportFileFormat.TSV;

    private Integer rowLimit;
    @Builder.Default
    private boolean includeSubHeaders = true;
    @Builder.Default
    private List<String> excludeModules = new ArrayList<>();
    @Builder.Default
    private List<String> includeFields = new ArrayList<>();
}
