package bio.terra.pearl.core.service.export;

import bio.terra.pearl.core.service.export.instance.ModuleExportInfo;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class TsvExporter extends BaseExporter {
    public static final String DELIMITER = "\t";

    public TsvExporter(List<ModuleExportInfo> moduleExportInfos, List<Map<String, String>> enrolleeMaps) {
        super(moduleExportInfos, enrolleeMaps);
    }

    /**
     * writes the data to the given stream. This does not close the stream, so that multi-part streams (e.g. zip files)
     * can be supported
     */
    @Override
    public void export(OutputStream os) throws IOException {
        PrintWriter printWriter = new PrintWriter(os);
        List<String> columnKeys = getColumnKeys();
        List<String> headerRowValues = getHeaderRow();
        List<String> subHeaderRowValues = getSubHeaderRow();

        printWriter.println(getRowString(headerRowValues));
        printWriter.println(getRowString(subHeaderRowValues));
        for (Map<String, String> enrolleeMap : enrolleeMaps) {
            List<String> rowValues = getRowValues(enrolleeMap, columnKeys);
            String rowString = getRowString(rowValues);
            printWriter.println(rowString);
        }
        printWriter.flush();
        // do not close os -- that's the caller's responsibility
    }

    protected String getRowString(List<String> rowValues) {
        return String.join(DELIMITER, rowValues);
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
