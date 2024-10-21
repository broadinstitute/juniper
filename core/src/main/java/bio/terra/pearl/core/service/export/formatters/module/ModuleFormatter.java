package bio.terra.pearl.core.service.export.formatters.module;

import bio.terra.pearl.core.model.export.ExportOptions;
import bio.terra.pearl.core.model.survey.QuestionChoice;
import bio.terra.pearl.core.service.export.EnrolleeExportData;
import bio.terra.pearl.core.service.export.formatters.ExportFormatUtils;
import bio.terra.pearl.core.service.export.formatters.item.ItemFormatter;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
public abstract class ModuleFormatter<T, F extends ItemFormatter<T>> {
    protected String moduleName;
    protected String displayName;
    @Setter
    protected int maxNumRepeats = 1;
    protected List<F> itemFormatters = new ArrayList<>();

    protected ModuleFormatter(ExportOptions options, String moduleName, String displayName) {
        this.moduleName = moduleName;
        this.displayName = displayName;
        this.itemFormatters = generateItemFormatters(options);
        filterItemFormatters(options);
    }

    public String getColumnKey(F itemFormatter, boolean isOtherDescription, QuestionChoice choice, int moduleRepeatNum) {
        if (moduleRepeatNum > 1) {
            return "%s%s%s%s%s".formatted(
                    moduleName,
                    ExportFormatUtils.COLUMN_NAME_DELIMITER,
                    moduleRepeatNum,
                    ExportFormatUtils.COLUMN_NAME_DELIMITER,
                    itemFormatter.getBaseColumnKey());
       }
        return "%s%s%s".formatted(moduleName, ExportFormatUtils.COLUMN_NAME_DELIMITER,  itemFormatter.getBaseColumnKey());
    }

    protected abstract List<F> generateItemFormatters(ExportOptions options);
    protected void filterItemFormatters(ExportOptions options) {
        if (options.getIncludeFields() != null && !options.getIncludeFields().isEmpty()) {
            itemFormatters = itemFormatters.stream()
                    .filter(itemFormatter -> ArrayUtils.contains(options.getIncludeFields().toArray(),
                            getColumnKey(itemFormatter, false, null, 1)))
                    .collect(Collectors.toList());
        }
    }

    public String getColumnHeader(F itemFormatter, boolean isOtherDescription, QuestionChoice choice, int moduleRepeatNum) {
        return getColumnKey(itemFormatter, isOtherDescription, choice, moduleRepeatNum);
    }

    public String getColumnSubHeader(F itemFormatter, boolean isOtherDescription, QuestionChoice choice, int moduleRepeatNum) {
        return ExportFormatUtils.camelToWordCase(itemFormatter.getBaseColumnKey());
    }

    public abstract Map<String, String> toStringMap(EnrolleeExportData enrolleeExportData);

    public T fromStringMap(UUID studyEnvironmentId, Map<String, String> enrolleeMap) {
        throw new NotImplementedException();
    };
}
