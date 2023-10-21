package bio.terra.pearl.core.service.export;

import bio.terra.pearl.core.model.survey.QuestionChoice;
import bio.terra.pearl.core.service.export.instance.ItemExportInfo;
import bio.terra.pearl.core.service.export.instance.ModuleExportInfo;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class BaseExporter {

    protected final List<ModuleExportInfo> moduleExportInfos;
    protected final List<Map<String, String>> enrolleeMaps;

    public BaseExporter(List<ModuleExportInfo> moduleExportInfos, List<Map<String, String>> enrolleeMaps) {
        this.moduleExportInfos = moduleExportInfos;
        this.enrolleeMaps = enrolleeMaps;
    }

    public abstract void export(OutputStream os) throws IOException;

    protected List<String> getColumnKeys() {
        List<String> columnKeys = new ArrayList<>();
        applyToEveryColumn((moduleExportInfo, itemExportInfo, isOtherDescription, choice) -> {
            columnKeys.add(moduleExportInfo.getFormatter().getColumnKey(moduleExportInfo, itemExportInfo, isOtherDescription, choice));
        });
        return columnKeys;
    }

    /** gets the header row - uses getColumnHeader from ExportFormatter */
    protected List<String> getHeaderRow() {
        List<String> headers = new ArrayList<>();
        applyToEveryColumn((moduleExportInfo, itemExportInfo, isOtherDescription, choice) -> {
            headers.add(moduleExportInfo.getFormatter().getColumnHeader(moduleExportInfo, itemExportInfo, isOtherDescription, choice));
        });
        return headers;
    }

    /** gets the subheader row -- uses getColumnSubHeader from ExportFormatter */
    protected List<String> getSubHeaderRow() {
        List<String> headers = new ArrayList<>();
        applyToEveryColumn((moduleExportInfo, itemExportInfo, isOtherDescription, choice) -> {
            headers.add(moduleExportInfo.getFormatter().getColumnSubHeader(moduleExportInfo, itemExportInfo, isOtherDescription, choice));
        });
        return headers;
    }

    /**
     * Gets the values to render for a row (usually an enrollee -- later we will have rows for proxies).
     * This handles any sanitization of string values (e.g. if commas/newlines/tabs need to be escaped)
     * @param enrolleeMap map of columnName => value
     * @param headerRowValues the ordered list of column keys
     * @return the ordered list of values
     */
    protected List<String> getRowValues(Map<String, String> enrolleeMap, List<String> headerRowValues) {
        List<String> rowValues = new ArrayList(headerRowValues.size());
        for (String header : headerRowValues) {
            String value = enrolleeMap.get(header);
            rowValues.add(sanitizeValue(value));
        }
        return rowValues;
    }

    /** class for operating iteratively over columns (variables) of an export */
    public interface ColumnProcessor {
        void apply(ModuleExportInfo moduleExportInfo,
                   ItemExportInfo itemExportInfo, boolean isOtherDescription, QuestionChoice choice);
    }

    public void applyToEveryColumn(ColumnProcessor columnProcessor) {
        for (ModuleExportInfo moduleExportInfo : moduleExportInfos) {
            for (ItemExportInfo itemExportInfo : moduleExportInfo.getItems()) {
                if (itemExportInfo.isSplitOptionsIntoColumns()) {
                    // add a column for each option
                    for (QuestionChoice choice : itemExportInfo.getChoices()) {
                        columnProcessor.apply(moduleExportInfo, itemExportInfo, false, choice);
                    }
                } else {
                    columnProcessor.apply(moduleExportInfo, itemExportInfo,false, null);
                }
                if (itemExportInfo.isHasOtherDescription()) {
                    // for questions with free-text other, we add an additional column to capture that value
                    columnProcessor.apply(moduleExportInfo, itemExportInfo, true, null);
                }
            }
        }
    }

    protected String sanitizeValue(String value) {
        // default is no-op
        return value;
    }
}
