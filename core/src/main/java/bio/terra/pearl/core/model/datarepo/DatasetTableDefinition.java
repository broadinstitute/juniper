package bio.terra.pearl.core.model.datarepo;

import bio.terra.datarepo.model.TableDataType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
public class DatasetTableDefinition {
    private String tableName;
    private String primaryKey;
    private Map<String, TableDataType> columns;
}
