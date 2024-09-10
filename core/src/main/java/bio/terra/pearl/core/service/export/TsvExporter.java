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
    public TsvExporter(List<ModuleFormatter> moduleExportInfos, List<Map<String, String>> enrolleeMaps) {
        super(moduleExportInfos, enrolleeMaps);
    }

    /**
     * writes the data to the given stream. This does not close the stream, so that multi-part streams (e.g. zip files)
     * can be supported
     */
    @Override
    public void export(OutputStream os) {
        try {
            CSVPrinter writer = CSVFormat.TDF.builder().setRecordSeparator('\n').build().print(new OutputStreamWriter(os));

            List<String> columnKeys = getColumnKeys();
            List<String> headerRowValues = getHeaderRow();
            List<String> subHeaderRowValues = getSubHeaderRow();

            writer.printRecord(headerRowValues);
            writer.printRecord(subHeaderRowValues);
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
