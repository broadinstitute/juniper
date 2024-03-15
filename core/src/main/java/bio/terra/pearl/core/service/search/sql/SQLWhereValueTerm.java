package bio.terra.pearl.core.service.search.sql;

import bio.terra.pearl.core.service.search.Type;
import org.jdbi.v3.core.statement.Query;

import java.time.Instant;
import java.time.LocalDate;

public class SQLWhereValueTerm implements SQLWhereClause {
    private String stringValue = null;
    private Integer integerValue = null;
    private Double doubleValue = null;
    private Instant instantValue = null;
    private LocalDate dateValue = null;
    private Boolean booleanValue = null;
    private Integer boundIndex;
    private final Type type;

    public SQLWhereValueTerm(String stringValue) {
        this.stringValue = stringValue;
        this.type = Type.STRING;
    }

    public SQLWhereValueTerm(Integer integerValue) {
        this.integerValue = integerValue;
        this.type = Type.INTEGER;
    }

    public SQLWhereValueTerm(Double doubleValue) {
        this.doubleValue = doubleValue;
        this.type = Type.DOUBLE;
    }

    public SQLWhereValueTerm(Instant instantValue) {
        this.instantValue = instantValue;
        this.type = Type.INSTANT;
    }

    public SQLWhereValueTerm(LocalDate dateValue) {
        this.dateValue = dateValue;
        this.type = Type.DATE;
    }

    public SQLWhereValueTerm(Boolean booleanValue) {
        this.booleanValue = booleanValue;
        this.type = Type.BOOLEAN;
    }

    @Override
    public String generateSql(SQLContext context) {
        this.boundIndex = context.incrementParamIndex();
        return ":" + boundIndex;
    }

    @Override
    public void bindSqlParams(Query query) {
        if (boundIndex == null) {
            throw new IllegalStateException("bindSqlParams called before generateSql");
        }

        switch (type) {
            case STRING -> query.bind(boundIndex.toString(), stringValue);
            case INTEGER -> query.bind(boundIndex.toString(), integerValue);
            case DOUBLE -> query.bind(boundIndex.toString(), doubleValue);
            case INSTANT -> query.bind(boundIndex.toString(), instantValue);
            case DATE -> query.bind(boundIndex.toString(), dateValue);
            case BOOLEAN -> query.bind(boundIndex.toString(), booleanValue);
        }
    }


}
