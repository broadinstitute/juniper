package bio.terra.pearl.core.service.export;

import bio.terra.pearl.core.model.export.ExportOptions;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpression;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ExportOptionsWithExpression extends ExportOptions {
    private EnrolleeSearchExpression filterExpression;
}
