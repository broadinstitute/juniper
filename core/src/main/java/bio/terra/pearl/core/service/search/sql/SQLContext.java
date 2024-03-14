package bio.terra.pearl.core.service.search.sql;

public class SQLContext {
    private int paramIndex = 0;

    public int incrementParamIndex() {
        return paramIndex++;
    }
}
