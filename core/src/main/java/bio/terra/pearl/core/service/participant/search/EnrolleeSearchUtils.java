package bio.terra.pearl.core.service.participant.search;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.Getter;
import org.jdbi.v3.core.statement.Query;

@Getter
public abstract class EnrolleeSearchUtils {
  public static final String FILTER_VALUE_SEPARATOR = ",";
  public static final String FACET_FILTER_SEPARATOR = "=";

  public static String getSqlParamName(String tableName, String columnName, int filterIndex) {
    return "%s_%s%d".formatted(tableName, columnName, filterIndex);
  }

  public static String getSimpleWhereClause(StringFacetValue facetValue, String columnName, String tableName) {
    if (facetValue.getFilterValues().isEmpty()) {
      return " 1 = 1";
    }
    return IntStream.range(0, facetValue.getFilterValues().size())
        .mapToObj(index -> " %s.%s = :%s".formatted(tableName, columnName,
            EnrolleeSearchUtils.getSqlParamName(tableName, facetValue.getKeyName(), index)))
        .collect(Collectors.joining(" OR"));
  }

  public static void bindSqlParameters(StringFacetValue facetValue, String tableName, Query query) {
    for(int i = 0; i < facetValue.getFilterValues().size(); i++) {
      query.bind(EnrolleeSearchUtils.getSqlParamName(tableName, facetValue.getKeyName(), i), facetValue.getFilterValues().get(i));
    }
  }
}
