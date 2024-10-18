package bio.terra.pearl.core.service.export;

import bio.terra.pearl.core.service.exception.internal.IOInternalException;
import bio.terra.pearl.core.service.export.formatters.module.ModuleFormatter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

public class JsonExporter extends BaseExporter {

    private final ObjectMapper objectMapper;

    public JsonExporter(List<ModuleFormatter> moduleFormatters, List<Map<String, String>> enrolleeMaps, List<String> columnSorting,
                        ObjectMapper objectMapper) {
        super(moduleFormatters, enrolleeMaps, columnSorting);
        this.objectMapper = objectMapper;
    }

    /** the 'includeSubheaders' parameter is ignored for JSON export -- subheaders are always available in the returned JSON object */
    public void export(OutputStream os, boolean includeSubHeaders) {
        PrintWriter printWriter = new PrintWriter(os);
        List<String> columnKeys = getColumnKeys();
        List<String> headerRowValues = getHeaderRow();
        List<String> subHeaderRowValues = getSubHeaderRow();

        JsonExport jsonExport = new JsonExport(columnKeys, headerRowValues, subHeaderRowValues, enrolleeMaps);
        try {
            printWriter.println(objectMapper.writeValueAsString(jsonExport));
            printWriter.flush();
        } catch (IOException e) {
            throw new IOInternalException("Error writing json to stream", e);
        }
        // do not close os -- that's the caller's responsibility
    }

    public record JsonExport(List<String> columnKeys, List<String> headerRowValues, List<String> subHeaderRowValues,
                             List<Map<String, String>> valueMaps) {}

}
