package bio.terra.pearl.core.service.export.instance;

import bio.terra.pearl.core.service.export.EnrolleeExportData;
import bio.terra.pearl.core.service.export.formatters.ExportFormatter;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Builder
public class ModuleExportInfo {
    private String moduleName;
    private String displayName;
    @Setter
    @Builder.Default
    private int maxNumRepeats = 1;
    private List<ItemExportInfo> items;
    private ExportFormatter formatter;

    public Map<String, String> toStringMap(EnrolleeExportData enrolleeExportData) throws Exception{
        return formatter.toStringMap(enrolleeExportData, this);
    }
}
