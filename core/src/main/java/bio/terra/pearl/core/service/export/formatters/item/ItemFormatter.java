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


    /** if this item maps to a single column in the export, this function is trivial.
     * Override this for itemFormatters that map to multiple columns */
    public void applyToEveryColumn(BaseExporter.ColumnProcessor columnProcessor, ModuleFormatter moduleFormatter, int moduleRepeatNum) {
        columnProcessor.apply(moduleFormatter, this, false, null, moduleRepeatNum);
    }

    public String getEmptyValue() {
        return "";
    }

    /**
     * sets the appropriate property or otherwise modifies the given bean with the value from the export String
     * This is used for importing values from strings back to bean properties.
     * */
    public abstract void importValueToBean(T bean, String exportString);
}
