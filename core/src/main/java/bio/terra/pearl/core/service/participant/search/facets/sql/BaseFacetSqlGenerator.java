package bio.terra.pearl.core.service.participant.search.facets.sql;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.service.participant.search.facets.FacetValue;
import org.jdbi.v3.core.statement.Query;

import java.util.List;

public abstract class BaseFacetSqlGenerator<T extends FacetValue> implements FacetSqlGenerator<T> {
    @Override
    public abstract String getTableName();

    protected String getColumnName(T facetValue) {
        // note that FacetValueFactory will ensure that keyName matches to a valid, searchable column name.
        return BaseJdbiDao.toSnakeCase(facetValue.getKeyName());
    }

    @Override
    public abstract String getSelectQuery(T facetValue, int facetIndex);

    @Override
    public String getJoinQuery() {
        return null;
    }

    @Override
    public String getWhereClause(T facetValue, int facetIndex) {
        return facetValue.getWhereClause(getTableName(), getColumnName(facetValue),facetIndex);
    }

    @Override
    public String getCombinedWhereClause(List<T> facetValues) {
        return "";
    }

    @Override
    public void bindSqlParameters(T facetValue, int facetIndex, Query query) {
        facetValue.bindSqlParameters(getTableName(), getColumnName(facetValue), facetIndex, query);
    }
}
