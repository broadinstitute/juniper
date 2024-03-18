package bio.terra.pearl.core.service.search.sql;

import bio.terra.pearl.core.service.search.Type;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

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
    public String generateSql() {
        return "?";
    }

    @Override
    public List<Object> boundObjects() {
        return switch (type) {
            case STRING -> List.of(stringValue);
            case INTEGER -> List.of(integerValue);
            case DOUBLE -> List.of(doubleValue);
            case INSTANT -> List.of(instantValue);
            case DATE -> List.of(dateValue);
            case BOOLEAN -> List.of(booleanValue);
            default -> throw new IllegalArgumentException("Unknown type: " + type);
        };
    }


}
