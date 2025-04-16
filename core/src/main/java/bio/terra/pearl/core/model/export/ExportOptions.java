package bio.terra.pearl.core.model.export;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.service.export.ExportFileFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@SuperBuilder
@Getter @Setter
@NoArgsConstructor
public class ExportOptions extends BaseEntity {
    @Builder.Default
    private boolean splitOptionsIntoColumns = false;
    @Builder.Default
    private boolean stableIdsForOptions = false;
    @Builder.Default
    private boolean onlyIncludeMostRecent = true;
    @Builder.Default
    private boolean onlyIncludeCompleted = false;
    private String filterString;
    @Builder.Default
    private ExportFileFormat fileFormat = ExportFileFormat.TSV;
    private Integer rowLimit;
    @Builder.Default
    private boolean includeSubHeaders = true;
    @Builder.Default
    private List<String> excludeModules = new ArrayList<>();
    @Builder.Default
    private List<String> includeFields = new ArrayList<>();
    private String timeZone; // defaults to null since the default is to use the study home time zone

    @JsonIgnore
    public ZoneId getZoneId() {
        return timeZone == null ? null : ZoneId.of(timeZone);
    }
}
