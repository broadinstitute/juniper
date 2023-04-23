package bio.terra.pearl.core.service.export.instance;

import bio.terra.pearl.core.service.export.ExportFileFormat;

public record ExportOptions (boolean splitOptionsIntoColumns, boolean stableIdsForOptions,
                             ExportFileFormat fileFormat, boolean onlyIncludeMostRecent) {
}
