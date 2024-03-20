package bio.terra.pearl.core.service.search.terms;

import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Result of a search term (profile.givenName) or user input ("John"). Useful when executing a
 * search expression outside of SQL.
 */
@Getter
public class SearchValue {
    private String stringValue = null;
    private Integer integerValue = null;
    private Double doubleValue = null;
    private Instant instantValue = null;
    private LocalDate dateValue = null;
    private Boolean booleanValue = null;

    private final SearchValueType searchValueType;


    public SearchValue(String stringValue) {
        this.stringValue = stringValue;
        this.searchValueType = SearchValueType.STRING;
    }

    public SearchValue(Integer integerValue) {
        this.integerValue = integerValue;
        this.searchValueType = SearchValueType.INTEGER;
    }

    public SearchValue(Double doubleValue) {
        this.doubleValue = doubleValue;
        this.searchValueType = SearchValueType.DOUBLE;
    }

    public SearchValue(Instant instantValue) {
        this.instantValue = instantValue;
        this.searchValueType = SearchValueType.INSTANT;
    }

    public SearchValue(LocalDate dateValue) {
        this.dateValue = dateValue;
        this.searchValueType = SearchValueType.DATE;
    }

    public SearchValue(Boolean booleanValue) {
        this.booleanValue = booleanValue;
        this.searchValueType = SearchValueType.BOOLEAN;
    }

    public boolean equals(SearchValue right) {
        return switch (this.searchValueType) {
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

    public boolean greaterThan(SearchValue right) {
        return switch (this.searchValueType) {
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

    public boolean greaterThanOrEqualTo(SearchValue right) {
        return switch (this.searchValueType) {
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

    public enum SearchValueType {
        STRING,
        INTEGER,
        DOUBLE,
        INSTANT,
        DATE,
        BOOLEAN
    }
}
