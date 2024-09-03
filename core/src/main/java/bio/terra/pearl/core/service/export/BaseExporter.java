package bio.terra.pearl.core.service.export;

import bio.terra.pearl.core.model.survey.QuestionChoice;
import bio.terra.pearl.core.service.export.formatters.item.ItemFormatter;
import bio.terra.pearl.core.service.export.formatters.module.ModuleFormatter;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseExporter {

    protected final List<ModuleFormatter> moduleFormatters;
    protected final List<Map<String, String>> enrolleeMaps;
    /**
     * map of column keys to the value that should be exported if the value for an enrollee is nullish.
     * This saves us from having to include "0" for every option possibility in multiple choice questions
     * exported in the analysis-friendly format, which for some Pepper datasets reduced the memory taken by
     * the enrolleeMaps by >50%
     */
    protected final Map<String, String> columnEmptyValueMap;
    public final String DEFAULT_EMPTY_STRING_VALUE = "";

    public BaseExporter(List<ModuleFormatter> moduleFormatters, List<Map<String, String>> enrolleeMaps) {
        this.moduleFormatters = moduleFormatters;
        this.enrolleeMaps = enrolleeMaps;
        this.columnEmptyValueMap = makeEmptyValueMap();
    }

    public abstract void export(OutputStream os);

    protected List<String> getColumnKeys() {
        List<String> columnKeys = new ArrayList<>();
        applyToEveryColumn((moduleFormatter, itemExportInfo, isOtherDescription, choice, moduleRepeatNum) -> {
            columnKeys.add(moduleFormatter.getColumnKey(itemExportInfo, isOtherDescription, choice, moduleRepeatNum));
        });
        return columnKeys;
    }

    /** gets the header row - uses getColumnHeader from ExportFormatter */
    protected List<String> getHeaderRow() {
        List<String> headers = new ArrayList<>();
        applyToEveryColumn((moduleFormatter, itemExportInfo, isOtherDescription, choice, moduleRepeatNum) -> {
            headers.add(
                    sanitizeValue(moduleFormatter.getColumnHeader(itemExportInfo, isOtherDescription, choice, moduleRepeatNum), DEFAULT_EMPTY_STRING_VALUE)
            );
        });
        return headers;
    }

    /** gets the subheader row -- uses getColumnSubHeader from ExportFormatter */
    protected List<String> getSubHeaderRow() {
        List<String> headers = new ArrayList<>();
        applyToEveryColumn((moduleFormatter, itemExportInfo, isOtherDescription, choice, moduleRepeatNum) -> {
            headers.add(
                    sanitizeValue(moduleFormatter.getColumnSubHeader(itemExportInfo, isOtherDescription, choice, moduleRepeatNum), DEFAULT_EMPTY_STRING_VALUE)
            );
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
            rowValues.add(sanitizeValue(value, columnEmptyValueMap.getOrDefault(header, DEFAULT_EMPTY_STRING_VALUE)));
        }
        return rowValues;
    }

    /** class for operating iteratively over columns (variables) of an export */
    public interface ColumnProcessor {
        void apply(ModuleFormatter moduleExportInfo,
                   ItemFormatter itemFormatter, boolean isOtherDescription, QuestionChoice choice, int moduleRepeatNum);
    }

    public void applyToEveryColumn(ColumnProcessor columnProcessor) {
        for (ModuleFormatter moduleFormatter : moduleFormatters) {
            for (int moduleRepeatNum = 1; moduleRepeatNum <= moduleFormatter.getMaxNumRepeats(); moduleRepeatNum++) {
                for (ItemFormatter itemFormatter : (List<ItemFormatter>) moduleFormatter.getItemFormatters()) {
                    itemFormatter.applyToEveryColumn(columnProcessor, moduleFormatter, moduleRepeatNum);
                }
            }
        }
    }

    protected Map<String, String> makeEmptyValueMap() {
        Map<String, String> emptyValueMap = new HashMap<>();
        applyToEveryColumn((moduleFormatter, itemFormatter, isOtherDescription, choice, moduleRepeatNum) -> {
            String columnKey = moduleFormatter.getColumnKey(itemFormatter, isOtherDescription, choice, moduleRepeatNum);
            emptyValueMap.put(columnKey, itemFormatter.getEmptyValue());
        });
        return emptyValueMap;
    }

    /**
     * Take a string value and sanitize it for export. E.g. For a TSV exporter, we need to escape double quotes.
     */
    protected String sanitizeValue(String value, String nullValueString) {
        // default is to just replace nulls with the nullValueString
        if (value == null) {
            value = nullValueString;
        }
        return value;
    }
}
