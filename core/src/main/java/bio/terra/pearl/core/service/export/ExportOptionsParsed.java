package bio.terra.pearl.core.service.export;

import bio.terra.pearl.core.service.search.EnrolleeSearchExpression;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter @SuperBuilder
public class ExportOptionsParsed extends ExportOptions {
    private final EnrolleeSearchExpression searchExpression;
    public ExportOptionsParsed() {
        this.searchExpression = null;
    }
}
