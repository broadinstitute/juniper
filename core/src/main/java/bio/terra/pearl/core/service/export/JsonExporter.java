package bio.terra.pearl.core.service.export;

import bio.terra.pearl.core.service.export.instance.ModuleExportInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

public class JsonExporter extends BaseExporter {

    private final ObjectMapper objectMapper;

    public JsonExporter(List<ModuleExportInfo> moduleExportInfos, List<Map<String, String>> enrolleeMaps,
                        ObjectMapper objectMapper) {
        super(moduleExportInfos, enrolleeMaps);
        this.objectMapper = objectMapper;
    }

    public void export(OutputStream os) throws IOException {
        PrintWriter printWriter = new PrintWriter(os);
        List<String> columnKeys = getColumnKeys();
        List<String> headerRowValues = getHeaderRow();
        List<String> subHeaderRowValues = getSubHeaderRow();

        JsonExport jsonExport = new JsonExport(columnKeys, headerRowValues, subHeaderRowValues, enrolleeMaps);
        printWriter.println(objectMapper.writeValueAsString(jsonExport));
        printWriter.flush();
        // do not close os -- that's the caller's responsibility
    }

    public record JsonExport(List<String> columnKeys, List<String> headerRowValues, List<String> subHeaderRowValues,
                             List<Map<String, String>> valueMaps) {}

}
