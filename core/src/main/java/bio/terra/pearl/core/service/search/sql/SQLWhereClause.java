package bio.terra.pearl.core.service.search.sql;

import java.util.List;

public interface SQLWhereClause {
    String generateSql();

    List<Object> boundObjects();
}
