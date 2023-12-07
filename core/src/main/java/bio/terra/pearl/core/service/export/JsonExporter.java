package bio.terra.pearl.core.service.export;

import bio.terra.pearl.core.service.export.formatters.module.ModuleFormatter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

public class JsonExporter extends BaseExporter {

    private final ObjectMapper objectMapper;

    public JsonExporter(List<ModuleFormatter> moduleFormatters, List<Map<String, String>> enrolleeMaps,
                        ObjectMapper objectMapper) {
        super(moduleFormatters, enrolleeMaps);
        this.objectMapper = objectMapper;
    }

    public void export(OutputStream os) {
        PrintWriter printWriter = new PrintWriter(os);
        List<String> columnKeys = getColumnKeys();
        List<String> headerRowValues = getHeaderRow();
        List<String> subHeaderRowValues = getSubHeaderRow();

        JsonExport jsonExport = new JsonExport(columnKeys, headerRowValues, subHeaderRowValues, enrolleeMaps);
        try {
            printWriter.println(objectMapper.writeValueAsString(jsonExport));
            printWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException("Error writing json to stream", e);
        }
        // do not close os -- that's the caller's responsibility
    }

    public record JsonExport(List<String> columnKeys, List<String> headerRowValues, List<String> subHeaderRowValues,
                             List<Map<String, String>> valueMaps) {}

}
