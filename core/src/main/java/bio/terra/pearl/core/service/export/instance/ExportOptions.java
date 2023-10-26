package bio.terra.pearl.core.service.export.instance;

import bio.terra.pearl.core.service.export.ExportFileFormat;
import lombok.Builder;

public record ExportOptions (boolean splitOptionsIntoColumns, boolean stableIdsForOptions, boolean onlyIncludeMostRecent,
                             ExportFileFormat fileFormat,
                             Integer limit) {
    public ExportOptions() {
        this(false, false, true, ExportFileFormat.TSV, null);
    }

    @Builder
    public ExportOptions {}
}
