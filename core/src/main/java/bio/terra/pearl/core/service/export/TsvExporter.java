package bio.terra.pearl.core.service.export;

import bio.terra.pearl.core.service.export.formatters.module.ModuleFormatter;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;

public class TsvExporter extends BaseExporter {
    public static final Character DELIMITER = '\t';

    public TsvExporter(List<ModuleFormatter> moduleExportInfos, List<Map<String, String>> enrolleeMaps) {
        super(moduleExportInfos, enrolleeMaps);
    }

    /**
     * writes the data to the given stream. This does not close the stream, so that multi-part streams (e.g. zip files)
     * can be supported
     */
    @Override
    public void export(OutputStream os) throws IOException {
        try (ICSVWriter writer = new CSVWriterBuilder(new OutputStreamWriter(os))
                .withSeparator(DELIMITER)
                .build()) {
            List<String> columnKeys = getColumnKeys();
            List<String> headerRowValues = getHeaderRow();
            List<String> subHeaderRowValues = getSubHeaderRow();

            writer.writeNext(headerRowValues.toArray(String[]::new));
            writer.writeNext(subHeaderRowValues.toArray(String[]::new));
            for (Map<String, String> enrolleeMap : enrolleeMaps) {
                List<String> rowValues = getRowValues(enrolleeMap, columnKeys);
                writer.writeNext(rowValues.toArray(String[]::new));
            }
            writer.flush();
        }
        // do not close os -- that's the caller's responsibility
    }
    
}
