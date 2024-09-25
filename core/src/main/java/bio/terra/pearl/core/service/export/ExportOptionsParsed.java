package bio.terra.pearl.core.service.export;

import bio.terra.pearl.core.model.export.ExportOptions;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpression;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class ExportOptionsParsed extends ExportOptions {
    private EnrolleeSearchExpression filterExpression;
    public ExportOptionsParsed() {
        this.filterExpression = null;
    }
}
