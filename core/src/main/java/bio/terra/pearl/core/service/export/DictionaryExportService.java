package bio.terra.pearl.core.service.export;

import bio.terra.pearl.core.service.export.instance.ExportOptions;
import bio.terra.pearl.core.service.export.instance.ModuleExportInfo;
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
        List<ModuleExportInfo> moduleInfos = enrolleeExportService.generateModuleInfos(exportOptions, studyEnvironmentId);
        // for now, we only support Excel
        DataDictionaryExcelExporter exporter = new DataDictionaryExcelExporter(moduleInfos, objectMapper);
        exporter.export(os);
    }
}
