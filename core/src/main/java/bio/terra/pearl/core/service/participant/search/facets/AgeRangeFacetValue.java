package bio.terra.pearl.core.service.participant.search.facets;

public class AgeRangeFacetValue extends IntRangeFacetValue {
    public AgeRangeFacetValue(String keyName, Integer min, Integer max) {
        super(keyName, min, max);
    }
    @Override
    public String getWhereClause(String tableName, String columnName, int facetIndex) {
        int maxValue = getMax() != null ? getMax() : 150;
        int minValue = getMin() != null ? getMin() : 0;
        // CAREFUL: this is putting user params directly in sql, this is only safe since it's only allowing integers.
        return """
         AGE(%s.%s) > (interval '%d years') and AGE(%s.%s) < (interval '%d years')
        """.formatted(tableName, columnName, minValue, tableName, columnName, maxValue);
    }
}
