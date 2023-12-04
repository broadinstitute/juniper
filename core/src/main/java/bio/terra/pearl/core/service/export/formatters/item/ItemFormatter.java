package bio.terra.pearl.core.service.export.formatters.item;

import bio.terra.pearl.core.service.export.BaseExporter;
import bio.terra.pearl.core.service.export.DataValueExportType;

import bio.terra.pearl.core.service.export.formatters.module.ModuleFormatter;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/** An 'item' corresponds to either a survey question or a specific field of a data model */
@Getter @SuperBuilder @NoArgsConstructor
public abstract class ItemFormatter<T> {
    /** maxNumRepeats has a setter so it can be computed dynamically in response to the data */
    @Builder.Default @Setter
    protected int maxNumRepeats = 1;
    @Builder.Default
    protected boolean allowMultiple = false;

    protected String baseColumnKey;
    protected DataValueExportType dataType;

    /**
     * because we need to generate a tsv,
     * we format everything as a string exactly as the characters should appear in the tsv,
     * the metadata will include information on the actual data type
     */
    public abstract String getExportString(T bean);

    /** if this item maps to a single column in the export, this function is trivial.
     * Override this for itemFormatters that map to multiple columns */
    public void applyToEveryColumn(BaseExporter.ColumnProcessor columnProcessor, ModuleFormatter moduleFormatter, int moduleRepeatNum) {
        columnProcessor.apply(moduleFormatter, this, false, null, moduleRepeatNum);
    }

    public String getEmptyValue() {
        return "";
    }
}
