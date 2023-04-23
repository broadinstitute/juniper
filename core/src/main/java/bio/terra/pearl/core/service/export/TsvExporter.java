package bio.terra.pearl.core.service.export;

import bio.terra.pearl.core.service.export.instance.ItemExportInfo;
import bio.terra.pearl.core.service.export.instance.ModuleExportInfo;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class TsvExporter {
    public static final String DELIMITER = "\t";

    private final List<ModuleExportInfo> moduleExportInfos;
    private final List<Map<String, String>> enrolleeMaps;

    public TsvExporter(List<ModuleExportInfo> moduleExportInfos, List<Map<String, String>> enrolleeMaps) {
        this.moduleExportInfos = moduleExportInfos;
        this.enrolleeMaps = enrolleeMaps;
    }

    /**
     * writes the data to the given stream. This does not close the stream, so that multi-part streams (e.g. zip files)
     * can be supported
     */
    public void export(OutputStream os) throws IOException {
        PrintWriter printWriter = new PrintWriter(os);
        List<String> headerRowValues = getHeaderRow();
        List<String> subHeaderRowValues = getSubHeaderRow();

        printWriter.println(getRowString(headerRowValues));
        printWriter.println(getRowString(subHeaderRowValues));
        for (Map<String, String> enrolleeMap : enrolleeMaps) {
            List<String> rowValues = getRowValues(enrolleeMap, headerRowValues);
            String rowString = getRowString(rowValues);
            printWriter.println(rowString);
        }
        printWriter.flush();
        // do not close os -- that's the caller's responsibility
    }

    /** gets the header row - uses getColumnHeader from ExportFormatter*/
    protected List<String> getHeaderRow() {
        List<String> headers = new ArrayList<>();
        applyToEveryColumn((moduleExportInfo, itemExportInfo, isOtherDescription) -> {
            headers.add(moduleExportInfo.getFormatter().getColumnHeader(moduleExportInfo, itemExportInfo, isOtherDescription, null));
        });
        return headers;
    }

    /** gets the subheader row -- uses getColumnSubHeader from ExportFormatter */
    protected List<String> getSubHeaderRow() {
        List<String> headers = new ArrayList<>();
        applyToEveryColumn((moduleExportInfo, itemExportInfo, isOtherDescription) -> {
            headers.add(moduleExportInfo.getFormatter().getColumnSubHeader(moduleExportInfo, itemExportInfo, isOtherDescription, null));
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

    protected String getRowString(List<String> rowValues) {
        return String.join(DELIMITER, rowValues);
    }

    /** class for operating iteratively over columns (variables) of an export */
    public interface ColumnProcessor {
        void apply(ModuleExportInfo moduleExportInfo,
                          ItemExportInfo itemExportInfo, boolean isOtherDescription);
    }

    public void applyToEveryColumn(ColumnProcessor columnProcessor) {
        for (ModuleExportInfo moduleExportInfo : moduleExportInfos) {
            for (ItemExportInfo itemExportInfo : moduleExportInfo.getItems()) {
                columnProcessor.apply(moduleExportInfo, itemExportInfo, false);
                if (itemExportInfo.isHasOtherDescription()) {
                    // for questions with free-text other, we add an additional column to capture that value
                    columnProcessor.apply(moduleExportInfo, itemExportInfo, true);
                }
            }
        }
    }


    /**
     * replaces double quotes with single, and then encapsulates any strings which contain newlines or tabs
     * with double quotes
     * @param value the value to sanitize
     * @return the sanitized value, suitable for including in a tsv
     */
    protected String sanitizeValue(String value) {
        if (value == null) {
            value = StringUtils.EMPTY;
        }
        // first replace double quotes with single '
        String sanitizedValue = value.replace("\"", "'");
        // then quote the whole string if needed
        if (sanitizedValue.indexOf("\n") >= 0 || sanitizedValue.indexOf(DELIMITER) >= 0) {
            sanitizedValue = String.format("\"%s\"", sanitizedValue);
        }
        return sanitizedValue;
    }
}
