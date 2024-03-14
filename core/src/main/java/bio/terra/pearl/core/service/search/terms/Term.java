package bio.terra.pearl.core.service.search.terms;

import bio.terra.pearl.core.service.search.Type;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
public class Term {
    private String stringValue = null;
    private Integer integerValue = null;
    private Double doubleValue = null;
    private Instant instantValue = null;
    private LocalDate dateValue = null;
    private Boolean booleanValue = null;

    private final Type type;


    public Term(String stringValue) {
        this.stringValue = stringValue;
        this.type = Type.STRING;
    }

    public Term(Integer integerValue) {
        this.integerValue = integerValue;
        this.type = Type.INTEGER;
    }

    public Term(Double doubleValue) {
        this.doubleValue = doubleValue;
        this.type = Type.DOUBLE;
    }

    public Term(Instant instantValue) {
        this.instantValue = instantValue;
        this.type = Type.INSTANT;
    }

    public Term(LocalDate dateValue) {
        this.dateValue = dateValue;
        this.type = Type.DATE;
    }

    public Term(Boolean booleanValue) {
        this.booleanValue = booleanValue;
        this.type = Type.BOOLEAN;
    }

    public boolean equals(Term right) {
        return switch (this.type) {
            case STRING -> this.stringValue.equals(right.stringValue);
            case INTEGER -> {
                if (right.doubleValue != null)
                    yield Double.valueOf(this.integerValue).equals(right.doubleValue);
                yield this.integerValue.equals(right.integerValue);
            }
            case DOUBLE -> {
                if (right.integerValue != null) {
                    yield this.doubleValue.equals(Double.valueOf(right.integerValue));
                }
                yield this.doubleValue.equals(right.doubleValue);
            }
            case INSTANT -> this.instantValue.equals(right.instantValue);
            case BOOLEAN -> this.booleanValue.equals(right.booleanValue);
            default -> false;
        };
    }

    public boolean greaterThan(Term right) {
        return switch (this.type) {
            case INTEGER -> {
                if (right.doubleValue != null)
                    yield Double.valueOf(this.integerValue) > right.doubleValue;
                yield this.integerValue > right.integerValue;
            }
            case DOUBLE -> {
                if (right.integerValue != null) {
                    yield this.doubleValue > Double.valueOf(right.integerValue);
                }
                yield this.doubleValue > right.doubleValue;
            }
            case INSTANT -> this.instantValue.isAfter(right.instantValue);
            default -> false;
        };
    }

    public boolean greaterThanOrEqualTo(Term right) {
        return switch (this.type) {
            case INTEGER -> {
                if (right.doubleValue != null)
                    yield Double.valueOf(this.integerValue) >= right.doubleValue;
                yield this.integerValue >= right.integerValue;
            }
            case DOUBLE -> {
                if (right.integerValue != null) {
                    yield this.doubleValue >= Double.valueOf(right.integerValue);
                }
                yield this.doubleValue >= right.doubleValue;
            }
            case INSTANT -> this.instantValue.isAfter(right.instantValue) || this.instantValue.equals(right.instantValue);
            default -> false;
        };
    }

}
