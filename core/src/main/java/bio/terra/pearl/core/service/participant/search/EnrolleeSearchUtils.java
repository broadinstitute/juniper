package bio.terra.pearl.core.service.participant.search;

import bio.terra.pearl.core.service.participant.search.facets.StringFacetValue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.Getter;
import org.jdbi.v3.core.statement.Query;

@Getter
public abstract class EnrolleeSearchUtils {
  public static String getSqlParamName(String tableName, String columnName, int facetIndex) {
    return "%s_%s%d".formatted(tableName, columnName, facetIndex);
  }

  public static String getSimpleWhereClause(StringFacetValue facetValue, String columnName, String tableName) {
    if (facetValue.getValues().isEmpty()) {
      return " 1 = 1";
    }
    return IntStream.range(0, facetValue.getValues().size())
        .mapToObj(index -> " %s.%s = :%s".formatted(tableName, columnName,
            EnrolleeSearchUtils.getSqlParamName(tableName, facetValue.getKeyName(), index)))
        .collect(Collectors.joining(" OR"));
  }

  public static void bindSqlParameters(StringFacetValue facetValue, String tableName, Query query) {
    for(int i = 0; i < facetValue.getValues().size(); i++) {
      query.bind(EnrolleeSearchUtils.getSqlParamName(tableName, facetValue.getKeyName(), i), facetValue.getValues().get(i));
    }
  }
}
