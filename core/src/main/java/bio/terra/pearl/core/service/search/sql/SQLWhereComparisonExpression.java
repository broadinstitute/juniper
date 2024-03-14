package bio.terra.pearl.core.service.search.sql;

import bio.terra.pearl.core.service.search.ComparisonOperator;
import org.jdbi.v3.core.statement.Query;

public class SQLWhereComparisonExpression implements SQLWhereClause {
    SQLWhereClause left;
    SQLWhereClause right;
    ComparisonOperator operator;

    public SQLWhereComparisonExpression(SQLWhereClause left, SQLWhereClause right, ComparisonOperator operator) {
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    @Override
    public String generateSql(SQLContext context) {
        return String.format("(%s %s %s)", left.generateSql(context), operator.getOperator(), right.generateSql(context));
    }

    @Override
    public void bindSqlParams(Query query) {
        this.left.bindSqlParams(query);
        this.right.bindSqlParams(query);
    }
}
