package bio.terra.pearl.core.service.export;

import bio.terra.pearl.core.service.search.EnrolleeSearchExpression;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;


@SuperBuilder
@Getter
public class ExportOptions {
    private final boolean splitOptionsIntoColumns;
    private final boolean stableIdsForOptions;
    private final boolean onlyIncludeMostRecent;
    private final String filter;
    private final ExportFileFormat fileFormat;
    private final Integer limit;
    private final boolean includeSubHeaders;
    @Builder.Default
    private List<String> excludeModules = new ArrayList<>();

    public ExportOptions() {
        this.splitOptionsIntoColumns = false;
        this.stableIdsForOptions = false;
        this.onlyIncludeMostRecent = true;
        this.filter = null;
        this.fileFormat = ExportFileFormat.TSV;
        this.limit = null;
        this.includeSubHeaders = false;
        this.excludeModules = new ArrayList<>();
    }
}
