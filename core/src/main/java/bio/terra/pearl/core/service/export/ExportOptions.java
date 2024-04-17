package bio.terra.pearl.core.service.export;

import lombok.Getter;
import lombok.experimental.SuperBuilder;


@SuperBuilder
@Getter
public final class ExportOptions {
    private final boolean splitOptionsIntoColumns;
    private final boolean stableIdsForOptions;
    private final boolean onlyIncludeMostRecent;
    private final boolean includeProxiesAsRows;
    private final ExportFileFormat fileFormat;
    private final Integer limit;

    public ExportOptions() {
        this.splitOptionsIntoColumns = false;
        this.stableIdsForOptions = false;
        this.onlyIncludeMostRecent = true;
        this.includeProxiesAsRows = false;
        this.fileFormat = ExportFileFormat.TSV;
        this.limit = null;
    }
}
