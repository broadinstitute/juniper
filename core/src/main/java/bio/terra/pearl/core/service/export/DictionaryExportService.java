package bio.terra.pearl.core.service.export;

import bio.terra.pearl.core.service.export.formatters.module.ModuleFormatter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class DictionaryExportService {
    private EnrolleeExportService enrolleeExportService;
    private ObjectMapper objectMapper;

    public DictionaryExportService(EnrolleeExportService enrolleeExportService) {
        this.enrolleeExportService = enrolleeExportService;
    }

    public void exportDictionary(ExportOptions exportOptions, UUID portalId,
                                 UUID studyEnvironmentId,
                                 OutputStream os) throws Exception {
        List<ModuleFormatter> moduleFormatters = enrolleeExportService.generateModuleInfos(exportOptions, studyEnvironmentId);
        // for now, we only support Excel
        DataDictionaryExcelExporter exporter = new DataDictionaryExcelExporter(moduleFormatters, objectMapper);
        exporter.export(os);
    }
}
