package bio.terra.pearl.core.service.export;

import lombok.Builder;

public record ExportOptions(
        boolean splitOptionsIntoColumns,
        boolean stableIdsForOptions,
        boolean onlyIncludeMostRecent,
        boolean includeProxiesAsRows,
        ExportFileFormat fileFormat,
        Integer limit) {
    public ExportOptions() {
        this(false, false, true, false, ExportFileFormat.TSV, null);
    }

    @Builder
    public ExportOptions {}
}
