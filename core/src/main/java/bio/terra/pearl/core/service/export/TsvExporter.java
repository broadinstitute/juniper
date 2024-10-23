package bio.terra.pearl.core.service.export;

import bio.terra.pearl.core.service.exception.internal.IOInternalException;
import bio.terra.pearl.core.service.export.formatters.module.ModuleFormatter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;

public class TsvExporter extends BaseExporter {
    private final ExportFileFormat fileFormat;

    public TsvExporter(List<ModuleFormatter> moduleExportInfos, List<Map<String, String>> enrolleeMaps, ExportFileFormat fileFormat,
                       List<String> columnSorting) {
        super(moduleExportInfos, enrolleeMaps, columnSorting);
        if (!List.of(ExportFileFormat.CSV, ExportFileFormat.TSV).contains(fileFormat)) {
            throw new IllegalArgumentException("Invalid file format for TsvExporter: " + fileFormat);
        }
        this.fileFormat = fileFormat;
    }

    public TsvExporter(List<ModuleFormatter> moduleExportInfos, List<Map<String, String>> enrolleeMaps) {
        this(moduleExportInfos, enrolleeMaps, ExportFileFormat.TSV, null);
    }

    /**
     * writes the data to the given stream. This does not close the stream, so that multi-part streams (e.g. zip files)
     * can be supported
     */
    @Override
    public void export(OutputStream os, boolean includeSubHeaders) {
        try {
            CSVFormat format = fileFormat.equals(ExportFileFormat.TSV) ? CSVFormat.TDF : CSVFormat.DEFAULT;
            CSVPrinter writer = format.builder().setRecordSeparator('\n').build().print(new OutputStreamWriter(os));

            List<String> columnKeys = getColumnKeys();
            List<String> headerRowValues = getHeaderRow();

            writer.printRecord(headerRowValues);
            if (includeSubHeaders) {
                List<String> subHeaderRowValues = getSubHeaderRow();
                writer.printRecord(subHeaderRowValues);
            }

            for (Map<String, String> enrolleeMap : enrolleeMaps) {
                List<String> rowValues = getRowValues(enrolleeMap, columnKeys);
                writer.printRecord(rowValues);
            }

            writer.flush();
            // do not close os -- that's the caller's responsibility
        } catch (IOException e) {
            throw new IOInternalException("Error writing TSV file", e);
        }
    }

}
